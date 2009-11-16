package cs5204.fs.storage;

import java.net.InetSocketAddress;
import java.net.Socket;

public class StorageServer
{
	private static InetSocketAddress _masterAddr;
	private static Socket _socket;
	
	public static void main(String [] args)
	{
		if (args.length < 2)
		{
			System.out.println("Master IP address and port needed");
			System.exit(0);
		}
		
		//First initiate the contact with the master
		
		
	}
	
	public static void initiateContact()
	{
		establishSocketConnection();
		
		
	}
	
	public static void establishSocketConnection()
	{
		_socket = new Socket();
		_socket.connect(_masterAddr);
	}
}