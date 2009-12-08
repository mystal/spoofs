package cs5204.fs.master;

import cs5204.fs.lib.StringUtil;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.Node;
import cs5204.fs.rpc.MSCommitRequest;
import cs5204.fs.rpc.MSCommitResponse;
import cs5204.fs.rpc.MBBackupRequest;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.Communication;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.NodeType;
import cs5204.fs.common.BackupOperation;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final int DEFAULT_STORAGE_LIFE = 2;
    private static final int DEFAULT_BACKUP_LIFE = 2;

    private static AtomicInteger _storIdCount;
	private static AtomicInteger _clientIdCount;
	private static int _currStorId;
	private static Lock _currStorIdLock;

	private static Directory _rootDir;
	private static ConcurrentHashMap<Integer, StorageNode> _storMap;
	private static ConcurrentHashMap<Integer, ClientNode> _clientMap;
    private static BackupNode _backup;
	
	private static BackupWorker _backupWorker;
	private static Worker _worker;

    private static Logger _log;
	
    public static void initialize()
    {
		setupLogging();

        _log.info("Setting up master...");

		_rootDir = new Directory(null, "");
		_storMap = new ConcurrentHashMap<Integer, StorageNode>();
		_clientMap = new ConcurrentHashMap<Integer, ClientNode>();
        _backup = null;
		
		/* NOTE - The storId needs to increment independent of clientId
					because it is assigned in a round-robin fashion to hold files */
		_storIdCount = new AtomicInteger(0);
		_clientIdCount = new AtomicInteger(0);
		_currStorId = 0;
		_currStorIdLock = new ReentrantLock();
		
		_worker = new Worker();
		
		Thread mainHandler = new Thread(new MainHandler(DEFAULT_MAIN_PORT));
        Thread kaHandler = new Thread(new KeepAliveHandler(DEFAULT_KEEPALIVE_PORT));

		_log.info("...done. Starting handlers...");
		
		mainHandler.start();
		kaHandler.start();

		_log.info("Ready to accept requests...\n");
    }

    public static int addStorageNode(String ipAddr, int port)
    {
		int id = _storIdCount.getAndIncrement();
		StorageNode storageNode = new StorageNode(id, ipAddr, port);
        _storMap.put(id, storageNode);

        //Perform backup if a backup server registered
        if (_backup != null)
            _backupWorker.submit(BackupOperation.ADD, storageNode);

        return id;
    }
	
	public static int addClientNode(String ipAddr, int port)
	{
		int id = _clientIdCount.getAndIncrement();
		ClientNode clientNode = new ClientNode(id, ipAddr, port);
		_clientMap.put(id, clientNode);
		
        //Perform backup if a backup server registered
        if (_backup != null)
            _backupWorker.submit(BackupOperation.ADD, clientNode);
		
        return id;
	}

	public static boolean addBackupNode(String ipAddr, int port)
	{
        if (_backup == null)
        {
            _backup = new BackupNode(0, ipAddr, port);
			_backupWorker = new BackupWorker(ipAddr, port);
			_backupWorker.start();
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

	private static class StorageNode extends Node
	{
        private AtomicInteger m_life;

		public StorageNode(int id, String addr, int port)
		{
			super(id, NodeType.STORAGE, addr, port);
            m_life = new AtomicInteger(DEFAULT_STORAGE_LIFE);
		}
		
		public int getLife()
		{
			return m_life.get();
		}

        public void setLife(int newLife)
        {
            m_life.set(newLife);
        }
	}
	
	private static class ClientNode extends Node 
	{
		public ClientNode(int id, String addr, int port)
		{
			super(id, NodeType.CLIENT, addr, port);
		}
	}

	private static class BackupNode extends Node
	{
        private AtomicInteger m_life;

		public BackupNode(int id, String addr, int port)
		{
			super(id, NodeType.BACKUP, addr, port);
            m_life = new AtomicInteger(DEFAULT_BACKUP_LIFE);
		}
		
		public int getLife()
		{
			return m_life.get();
		}

        public void setLife(int newLife)
        {
            m_life.set(newLife);
        }
	}
}
