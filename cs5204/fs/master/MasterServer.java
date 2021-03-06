package cs5204.fs.master;

import cs5204.fs.lib.StringUtil;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.OneWayWorker;
import cs5204.fs.lib.Node;
import cs5204.fs.lib.BackupObject;
import cs5204.fs.rpc.MSCommitRequest;
import cs5204.fs.rpc.MSCommitResponse;
import cs5204.fs.rpc.MSRecoveryRequest;
import cs5204.fs.rpc.MSRecoveryResponse;
import cs5204.fs.rpc.MBBackupRequest;
import cs5204.fs.rpc.CMRecoveryRequest;
import cs5204.fs.rpc.CMRecoveryResponse;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.Communication;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.NodeType;
import cs5204.fs.common.BackupOperation;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class MasterServer
{
    public static final int BLOCK_SIZE = 64; //MB
	private static final int DEFAULT_MAIN_PORT = 2009;
	/*private static final int DEFAULT_STORAGE_PORT = 2009;
	private static final int DEFAULT_CLIENT_PORT = 2010;
	private static final int DEFAULT_BACKUP_PORT = 2011;*/
	private static final int DEFAULT_KEEPALIVE_PORT = 2012;
    private static final int DEFAULT_STORAGE_LIFE = 4;
    private static final int DEFAULT_BACKUP_LIFE = 4;
	private static final int DEFAULT_CLIENT_LIFE = 5;
    private static final int DEFAULT_KEEPALIVE_INTERVAL = 5000;

	private static String _ipAddr;
    private static AtomicInteger _storIdCount;
	private static AtomicInteger _clientIdCount;
	private static int _currStorId;
	private static Lock _currStorIdLock;

	private static Directory _rootDir;
	private static ConcurrentHashMap<Integer, StorageNode> _storMap;
	private static ConcurrentHashMap<Integer, ClientNode> _clientMap;
    private static BackupNode _backup;
	
	private static ConcurrentLinkedQueue<Integer> _tempRecoveryList;
	private static LinkedList<BackupObject> _backupObjectList;
	private static ReentrantLock _backupObjectListLock;
	
	private static BackupWorker _backupWorker;
	private static Worker _worker;
	private static AtomicBoolean _performKA;

    private static Logger _log;
	
    public static void initialize()
    {
		setupLogging();

        _log.info("Setting up master...");
		
		try {
            _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }

		_rootDir = new Directory(null, "");
		_storMap = new ConcurrentHashMap<Integer, StorageNode>();
		_clientMap = new ConcurrentHashMap<Integer, ClientNode>();
        _backup = null;
		_performKA = new AtomicBoolean(true);
		
		_tempRecoveryList = new ConcurrentLinkedQueue<Integer>();
		_backupObjectList = new LinkedList<BackupObject>();
		_backupObjectListLock = new ReentrantLock();
		
		/* NOTE - The storId needs to increment independent of clientId
					because it is assigned in a round-robin fashion to hold files */
		_storIdCount = new AtomicInteger(0);
		_clientIdCount = new AtomicInteger(0);
		_currStorId = 0;
		_currStorIdLock = new ReentrantLock();
		
		_worker = new Worker();
		
		Thread mainHandler = new Thread(new MainHandler(DEFAULT_MAIN_PORT));
        Thread kaHandler = new Thread(new KeepAliveHandler(DEFAULT_KEEPALIVE_PORT));
        Thread gcDaemon = new Thread(new GCDaemon());

		_log.info("...done. Starting handlers...");
		
		mainHandler.start();
		kaHandler.start();
		gcDaemon.start();

		_log.info("Ready to accept requests...\n");
    }
	
	private static void submitBackup(BackupOperation operation, Node node)
	{
		_backupObjectListLock.lock();
		_backupObjectList.add(new BackupObject(operation, node));
		_backupObjectListLock.unlock();
	}
	
	public static BackupObject [] getBackupObjects()
	{
		_backupObjectListLock.lock();
		BackupObject[] objects = new BackupObject[_backupObjectList.size()];
		_log.info("Size of backup objects " + objects.length + "       " + (_backup==null));
		_backupObjectList.toArray(objects);
		_backupObjectList.clear();
		_backupObjectListLock.unlock();
		return objects;
	}

    public static int addStorageNode(String ipAddr, int port)
    {
		int id = _storIdCount.getAndIncrement();
		StorageNode storageNode = new StorageNode(id, ipAddr, port);
        _storMap.put(id, storageNode);

        //Perform backup if a backup server registered
        if (_backup != null)
            submitBackup(BackupOperation.ADD, new Node(storageNode.getId(), NodeType.STORAGE, storageNode.getAddress(), storageNode.getPort()));

        return id;
    }
	
	public static int addClientNode(String ipAddr, int port)
	{
		int id = _clientIdCount.getAndIncrement();
		ClientNode clientNode = new ClientNode(id, ipAddr, port);
		_clientMap.put(id, clientNode);
		
        //Perform backup if a backup server registered
        if (_backup != null)
            submitBackup(BackupOperation.ADD, new Node(clientNode.getId(), NodeType.CLIENT, clientNode.getAddress(), clientNode.getPort()));
		
        return id;
	}

	public static boolean addBackupNode(String ipAddr, int port)
	{
        if (_backup == null)
        {
            _backup = new BackupNode(0, ipAddr, port);
			_backupWorker = new BackupWorker(ipAddr, port);
			new Thread(_backupWorker).start();
            return true;
        }
        return false;
	}
	
	public static ArrayList<Node> getCurrentNodes()
	{
		ArrayList<Node> nodes = new ArrayList<Node>();
		Set<Map.Entry<Integer, ClientNode>> clients = _clientMap.entrySet();
		Set<Map.Entry<Integer, StorageNode>> storages = _storMap.entrySet();
		for (Map.Entry<Integer, ClientNode> entry : clients)
			nodes.add(entry.getValue());
		for (Map.Entry<Integer, StorageNode> entry : storages)
			nodes.add(entry.getValue());
		return nodes;
	}
	
	public static boolean removeClientNode(int id)
	{
		ClientNode clientNode = _clientMap.remove(id);
		
		if (clientNode == null)
			return false;

        //Perform backup if a backup server registered
        if (_backup != null)
			submitBackup(BackupOperation.REMOVE, new Node(clientNode.getId(), NodeType.CLIENT, clientNode.getAddress(), clientNode.getPort()));

        return true;
	}

    public static boolean removeStorageNode(int id)
    {
		StorageNode storageNode = _storMap.remove(id);
		
		if (storageNode == null)
			return false;

        //Perform backup if a backup server registered
        if (_backup != null)
			submitBackup(BackupOperation.REMOVE, new Node(storageNode.getId(), NodeType.STORAGE, storageNode.getAddress(), storageNode.getPort()));

        return true;
    }
	
    public static boolean removeBackupNode()
    {
        if (_backup != null)
        {
            _backup = null;
            return true;
        }
        return false;
    }
	
	public static Directory makeDirectory(String dirName)
	{
		ArrayList<String> dirs = StringUtil.explodeString(dirName);
		Directory currDir = _rootDir;
		for (int i = 0 ; i < dirs.size()-1 ; i++)
			if ((currDir = currDir.getDirectory(dirs.get(i))) == null)
				return null;
		
		return currDir.addDirectory(dirs.get(dirs.size()-1));
	}
	
	public static boolean removeDirectory(String dirPath)
	{
		ArrayList<String> dirs = StringUtil.explodeString(dirPath);
		Directory currDir = _rootDir;
		for (int i = 0 ; i < dirs.size()-1 ; i++)
			if ((currDir = currDir.getDirectory(dirs.get(i))) == null)
				return false;
		
		return currDir.removeDirectory(dirs.get(dirs.size()-1));
	}
	
	public static File createFile(String filePath)
	{
		ArrayList<String> dirs = StringUtil.explodeString(filePath);
		Directory currDir = _rootDir;
		for (int i = 0 ; i < dirs.size()-1 ; i++)
			if ((currDir = currDir.getDirectory(dirs.get(i))) == null)
				return null;
		
		int storId = getNextStorId();
		MSCommitResponse msResp = sendStorageCommitRequest(storId, new MSCommitRequest(FileOperation.CREATE, filePath));
		
		switch (msResp.getStatus())
		{
			case OK:
				//Nothing
				break;
			case DENIED:
			default:
				//TODO: Log/fail
		}
		
		return currDir.addFile(dirs.get(dirs.size()-1), storId);
	}
	
	public static boolean removeFile(String filePath)
	{
		ArrayList<String> dirs = StringUtil.explodeString(filePath);
		Directory currDir = _rootDir;
		for (int i = 0 ; i < dirs.size()-1 ; i++)
			if ((currDir = currDir.getDirectory(dirs.get(i))) == null)
				return false;
		
		return currDir.removeFile(dirs.get(dirs.size()-1));
	}
	
	public static File getFile(String filePath)
	{
		ArrayList<String> dirs = StringUtil.explodeString(filePath);
		Directory currDir = _rootDir;
		for (int i = 0 ; i < dirs.size()-1 ; i++)
			if ((currDir = currDir.getDirectory(dirs.get(i))) == null)
				return null;
		
		return currDir.getFile(dirs.get(dirs.size()-1));
	}
	
	/** Round-robin assignment of stor servers **/
	public static int getNextStorId()
	{
		int id = -1;
		while (id == -1)
		{
			//complicated critical section requiring Lock
			_currStorIdLock.lock();
			if (_currStorId > _storIdCount.get())
				_currStorId = 0;
			if (_storMap.get(_currStorId) != null)
				id = _currStorId;
			_currStorId++;
			_currStorIdLock.unlock();
		}
		return id;
	}
	
	public static String getStorIPAddress(int storId)
	{
		StorageNode stor = _storMap.get(storId);
		if (stor == null) return null;
		return stor.getAddress();
	}
	
	public static int getStorPort(int storId)
	{
		StorageNode stor = _storMap.get(storId);
		if (stor == null) return -1;
		return stor.getPort();
	}
	
	public static int getKAPort()
	{
		return DEFAULT_KEEPALIVE_PORT;
	}
	
	public static void info(String msg)
	{
		_log.info(msg);
	}
	
	public static void warning(String msg)
	{
		_log.warning(msg);
	}
	
	private static MSCommitResponse sendStorageCommitRequest(int storId, MSCommitRequest req)
	{
		Communication comm = null;
		
		String addr = getStorIPAddress(storId);
		int port = getStorPort(storId);
		if (addr == null || addr.length() == 0 || port < 0 || port > 65536)
			return null;
		
		comm = _worker.submitRequest(new Communication(Protocol.MS_COMMIT_REQUEST, req), addr, port);
		
		if (comm == null) 
			return null;
		
		return (MSCommitResponse)comm.getPayload();
	}

    private static void setupLogging()
    {
		_log = Logger.getLogger("cs5204.fs.master");
    }
	
	public static boolean processKA(NodeType type, int id)
	{
		switch (type)
		{
			case STORAGE:
			{
				StorageNode stor = _storMap.get(id);
				if (stor == null)
					return false;
				stor.setLife(DEFAULT_STORAGE_LIFE);
			} break;
			
			case CLIENT:
			{
				ClientNode cli = _clientMap.get(id);
				if (cli == null)
					return false;
				cli.setLife(DEFAULT_STORAGE_LIFE);
			} break;
			
			case BACKUP:
				if (_backup == null)
					return false;
				_backup.setLife(DEFAULT_BACKUP_LIFE);
				break;
			
			default:
				return false;
		}
		return true;
	}

    private static boolean nodeCleanup()
    {
		if (_performKA.get())
		{
            //TODO: fix removing while iterating
            ArrayList<Integer> toRemove = new ArrayList<Integer>();
			Set<Map.Entry<Integer,StorageNode>> storEntries = _storMap.entrySet();
			Set<Map.Entry<Integer,ClientNode>> clientEntries = _clientMap.entrySet();

            //Determine which storage nodes to remove
			for (Map.Entry<Integer,StorageNode> entry: storEntries)
			{
				if (entry.getValue().decrementAndGetLife() == 0)
                    toRemove.add(entry.getKey());
			}
            //Perform removal
            for (int id: toRemove)
                MasterServer.removeStorageNode(id);

            toRemove = new ArrayList<Integer>();
            //Determine which storage nodes to remove
			for (Map.Entry<Integer,ClientNode> entry: clientEntries)
			{
				if (entry.getValue().decrementAndGetLife() == 0)
                    toRemove.add(entry.getKey());
			}
            //Perform removal
            for (int id: toRemove)
                MasterServer.removeClientNode(id);

			if (_backup != null)
				if (_backup.decrementAndGetLife() == 0)
					MasterServer.removeBackupNode();
		}
        return true;
    }

	private static class StorageNode extends Node
	{
        private transient AtomicInteger m_life;

		public StorageNode(int id, String addr, int port)
		{
			super(id, NodeType.STORAGE, addr, port);
            m_life = new AtomicInteger(DEFAULT_STORAGE_LIFE);
		}
		
		public int getLife()
		{
			return m_life.get();
		}

		public int decrementAndGetLife()
		{
			return m_life.decrementAndGet();
		}

        public void setLife(int newLife)
        {
            m_life.set(newLife);
        }
	}
	
	private static class ClientNode extends Node 
	{
		private transient AtomicInteger m_life;
		
		public ClientNode(int id, String addr, int port)
		{
			super(id, NodeType.CLIENT, addr, port);
			m_life = new AtomicInteger(DEFAULT_CLIENT_LIFE);
		}
		
		public int getLife()
		{
			return m_life.get();
		}

		public int decrementAndGetLife()
		{
			return m_life.decrementAndGet();
		}

        public void setLife(int newLife)
        {
            m_life.set(newLife);
        }
	}

	private static class BackupNode extends Node
	{
        private transient AtomicInteger m_life;

		public BackupNode(int id, String addr, int port)
		{
			super(id, NodeType.BACKUP, addr, port);
            m_life = new AtomicInteger(DEFAULT_BACKUP_LIFE);
		}
		
		public int getLife()
		{
			return m_life.get();
		}

		public int decrementAndGetLife()
		{
			return m_life.decrementAndGet();
		}

        public void setLife(int newLife)
        {
            m_life.set(newLife);
        }
	}

    private static class GCDaemon implements Runnable
    {
        public GCDaemon()
        {
        }

        public void run()
        {
            while (MasterServer.nodeCleanup())
            {
                try {
                    Thread.sleep(DEFAULT_KEEPALIVE_INTERVAL);
                }
                catch (InterruptedException ex) {
                    //TODO: Log/Fail
                }
            }
        }
    }
	
	public static void BACKUP_suspendKA()
	{
		_performKA.set(false);
	}
	
	public static void BACKUP_addClientNode(Node node)
	{
		_clientMap.put(node.getId(), new ClientNode(node.getId(), node.getAddress(), node.getPort()));
	}
	
	public static void BACKUP_addStorageNode(Node node)
	{
		_storMap.put(node.getId(), new StorageNode(node.getId(), node.getAddress(), node.getPort()));
	}
	
	public static void BACKUP_broadcastToStorage()
	{
		//Initialize our recovery list
		_tempRecoveryList = new ConcurrentLinkedQueue<Integer>();;
		
		//Create one-way worker to handle outbound request creation
		//OneWayWorker worker = new OneWayWorker();
		
		_log.info("Broadcasting to storage nodes, a total of "+ _storMap.size());
		
		//Go through all storage nodes
		for (Integer id : _storMap.keySet())
		{
			StorageNode stor = _storMap.get(id);
			_log.info("Id: " + stor.getId() + "\nAddress: " + stor.getAddress() + "\nPort: " + stor.getPort());
			//Submit request to worker to broadcast MSRecoveryRequest
			Communication resp = _worker.submitRequest(
									new Communication(
										Protocol.MS_RECOVERY_REQUEST,
										new MSRecoveryRequest(
											new Node(id, NodeType.STORAGE, stor.getAddress(), stor.getPort()),
											_ipAddr,
											DEFAULT_MAIN_PORT,
											DEFAULT_KEEPALIVE_PORT)),
									stor.getAddress(),
									stor.getPort());
			_log.info("Storage Node " + id + " is back online!");
			MSRecoveryResponse msResp = (MSRecoveryResponse)resp.getPayload();
			MasterServer.submitRecovery(msResp.getId(), msResp.getFilenames());
		}
	}
	
	public static void submitRecovery(int id, String [] filenames)
	{
		//Goes through the reconstruction process
		for (String name : filenames)
		{
			ArrayList<String> tokens = StringUtil.explodeString(name);
			Directory currDir = _rootDir;
			for (int i = 0 ; i < tokens.size()-1 ; i++)
			{
				Directory tempDir = currDir.getDirectory(tokens.get(i));
				if (tempDir == null)
					currDir = currDir.addDirectory(tokens.get(i));
				else
					currDir = tempDir;
			}
			if (currDir.getFile(tokens.get(tokens.size()-1)) == null)
				currDir.addFile(tokens.get(tokens.size()-1), id);
		}
		
		_tempRecoveryList.add(id);
	}
	
	public static void BACKUP_reconstructFilesystem()
	{
		//Make sure that all storage nodes have "reported back"
		while (_tempRecoveryList.size() != _storMap.size())
		{
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException ex) {
				continue;
			}
		}
		//Note - this is a terrible implementation because we need to make sure that stors are not dead,
		//or that the data we got was not stale...nevertheless this will have to suffice
	}
	
	public static void BACKUP_broadcastToClient()
	{
		//Go through all clients
		for (Integer id : _clientMap.keySet())
		{
			ClientNode cli = _clientMap.get(id);
			Communication resp = _worker.submitRequest(
											new Communication(
												Protocol.CM_RECOVERY_REQUEST,
												new CMRecoveryRequest(
													new Node(id, NodeType.CLIENT, cli.getAddress(), cli.getPort()),
													_ipAddr,
													DEFAULT_MAIN_PORT)),
											cli.getAddress(),
											cli.getPort());
			//TODO: handle					
		}
		
	}
	
	public static void BACKUP_resumeKA()
	{
		_performKA.set(true);
	}
}
