package cs5204.fs.master;

import java.net.InetAddress;

import java.util.HashMap;

public class MasterServer
{
    private static final int blockSize = 64; //MB

	private static Directory rootDir;
	private static HashMap<Integer, StorageNode> storMap;
	
	public static void main(String[] args)
	{
		//TODO: decide what args to send in to the main() method
        //      possibly pass in blockSize,
		//TODO: set up
		
		rootDir = new Directory(null, "");
		storMap = new HashMap<Integer, StorageServer>();
		
		Thread storageHandler = new Thread(new StorageHandler());
		Thread clientHandler = new Thread(new ClientHandler());
	}
	
	private static class StorageNode
	{
		private InetAddress m_addr;
		public StorageNode(InetAddress addr)
		{
			m_addr = addr;
		}
	}

    //TODO: addStorageNode(), write a file, read a file, other public methods
}
