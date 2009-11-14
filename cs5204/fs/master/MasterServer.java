package cs5204.fs.master;

import java.util.HashMap;

public class MasterServer
{
	private static Directory rootDir;
	private static HashMap<Integer, StorageServer> storMap;
	
	public static void main(String [] args)
	{
		//TODO: decide what args to send in to the main() method
		//TODO: set up 
		
		rootDir = new Directory(null, "");
		storMap = new HashMap<Integer, StorageServer>();
		
		Thread clientHandler = new Thread(new ClientHandler());
		Thread storageHandler = new Thread(new StorageHandler());
	}
}