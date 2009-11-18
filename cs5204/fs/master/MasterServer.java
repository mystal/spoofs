package cs5204.fs.master;

import java.net.InetAddress;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class MasterServer
{
    private static final int blockSize = 64; //MB
	private static final int DEFAULT_STORAGE_PORT = 2009;
	private static final int DEFAULT_CLIENT_PORT = 2010;

    private static int storIdCount = 0;

	private static Directory _rootDir;
	private static HashMap<Integer, StorageNode> _storMap;
	private static ReentrantLock _storMapLock;
	
	public static void main(String[] args)
	{
		//TODO: decide what args to send in to the main() method
        //      possibly pass in blockSize,
		//TODO: set up
		
		_rootDir = new Directory(null, "");
		_storMap = new HashMap<Integer, StorageNode>();
		_storMapLock = new ReentrantLock();
		
		Thread storageHandler = new Thread(new StorageHandler(DEFAULT_STORAGE_PORT));
		Thread clientHandler = new Thread(new ClientHandler(DEFAULT_CLIENT_PORT));
		
		storageHandler.start();
	}

    //TODO: addStorageNode(), write a file, read a file, other public methods

    public static int addStorageNode(String ipAddr, int port)
    {
        _storMap.put(storIdCount, new StorageNode(ipAddr, port));
        return storIdCount++;
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
	}
}
