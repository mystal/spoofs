package cs5204.fs.master;

import cs5204.fs.lib.StringUtil;
import cs5204.fs.lib.Worker;

import cs5204.fs.rpc.MSCommitRequest;
import cs5204.fs.rpc.MSCommitResponse;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.Communication;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.FileOperation;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MasterServer
{
    public static final int BLOCK_SIZE = 64; //MB
	private static final int DEFAULT_STORAGE_PORT = 2009;
	private static final int DEFAULT_CLIENT_PORT = 2010;

    private static AtomicInteger _storIdCount;
	private static AtomicInteger _clientIdCount;
	private static int _currStorId;
	private static Lock _currStorIdLock;

	private static Directory _rootDir;
	private static ConcurrentHashMap<Integer, StorageNode> _storMap;
	private static ConcurrentHashMap<Integer, ClientNode> _clientMap;
	
	private static Worker _worker;
	
	public static void main(String[] args)
	{
		_rootDir = new Directory(null, "");
		_storMap = new ConcurrentHashMap<Integer, StorageNode>();
		_clientMap = new ConcurrentHashMap<Integer, ClientNode>();
		
		_storIdCount = new AtomicInteger(0);
		_clientIdCount = new AtomicInteger(0);
		_currStorId = 0;
		_currStorIdLock = new ReentrantLock();
		
		_worker = new Worker();
		
		Thread storageHandler = new Thread(new StorageHandler(DEFAULT_STORAGE_PORT));
		Thread clientHandler = new Thread(new ClientHandler(DEFAULT_CLIENT_PORT));
		
		storageHandler.start();
		clientHandler.start();
	}

    public static int addStorageNode(String ipAddr, int clientPort, int masterPort)
    {
		int id = _storIdCount.getAndIncrement();
        _storMap.put(id, new StorageNode(ipAddr, clientPort, masterPort));
        return id;
    }
	
	public static int addClientNode(String ipAddr, int port)
	{
		int id = _clientIdCount.getAndIncrement();
		_clientMap.put(id, new ClientNode(ipAddr, port));
        return id;
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
	
	public static int getStorClientPort(int storId)
	{
		StorageNode stor = _storMap.get(storId);
		if (stor == null) return -1;
		return stor.getClientPort();
	}
	
	public static int getStorMasterPort(int storId)
	{
		StorageNode stor = _storMap.get(storId);
		if (stor == null) return -1;
		return stor.getMasterPort();
	}
	
	private static MSCommitResponse sendStorageCommitRequest(int storId, MSCommitRequest req)
	{
		Communication comm = null;
		
		String addr = getStorIPAddress(storId);
		int port = getStorMasterPort(storId);
		if (addr == null || addr.length() == 0 || port < 0 || port > 65536)
			return null;
		
		comm = _worker.submitRequest(new Communication(Protocol.MS_COMMIT_REQUEST, req), addr, port);
		
		if (comm == null) 
			return null;
		
		return (MSCommitResponse)comm.getPayload();
	}

	private static class StorageNode
	{
		private String m_addr;
		private int m_clientPort;
		private int m_masterPort;

		public StorageNode(String addr, int clientPort, int masterPort)
		{
			m_addr = addr;
            m_clientPort = clientPort;
			m_masterPort = masterPort;
		}
		
		public String getAddress()
		{
			return m_addr;
		}
		
		public int getClientPort()
		{
			return m_clientPort;
		}
		
		public int getMasterPort()
		{
			return m_masterPort;
		}
	}
	
	private static class ClientNode
	{
		private String m_addr;
		private int m_port;

		public ClientNode(String addr, int port)
		{
			m_addr = addr;
            m_port = port;
		}
	}
}
