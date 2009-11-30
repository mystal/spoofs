package cs5204.fs.master;

import cs5204.fs.lib.StringUtil;

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
	
	public static void main(String[] args)
	{
		//TODO: decide what args to send in to the main() method
        //      possibly pass in blockSize,
		//TODO: set up
		
		_rootDir = new Directory(null, "");
		_storMap = new ConcurrentHashMap<Integer, StorageNode>();
		_clientMap = new ConcurrentHashMap<Integer, ClientNode>();
		
		_storIdCount = new AtomicInteger(0);
		_clientIdCount = new AtomicInteger(0);
		_currStorId = 0;
		_currStorIdLock = new ReentrantLock();
		
		Thread storageHandler = new Thread(new StorageHandler(DEFAULT_STORAGE_PORT));
		Thread clientHandler = new Thread(new ClientHandler(DEFAULT_CLIENT_PORT));
		
		storageHandler.start();
		clientHandler.start();
	}

    //TODO: write a file, read a file, other public methods

    public static int addStorageNode(String ipAddr, int port)
    {
		int id = _storIdCount.getAndIncrement();
        _storMap.put(id, new StorageNode(ipAddr, port));
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
	
	public static File createFile(String filePath)
	{
		ArrayList<String> dirs = StringUtil.explodeString(filePath);
		Directory currDir = _rootDir;
		for (int i = 0 ; i < dirs.size()-1 ; i++)
			if ((currDir = currDir.getDirectory(dirs.get(i))) == null)
				return null;
		
		return currDir.addFile(dirs.get(dirs.size()-1));
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
		return _storMap.get(storId).getAddress();
	}
	
	public static int getStorPort(int storId)
	{
		return _storMap.get(storId).getPort();
	}

	private static class StorageNode
	{
		private String m_addr;
		private int m_port;

		public StorageNode(String addr, int port)
		{
			m_addr = addr;
            m_port = port;
		}
		
		public String getAddress()
		{
			return m_addr;
		}
		
		public int getPort()
		{
			return m_port;
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
