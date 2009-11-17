package cs5204.fs.storage;

import cs5204.fs.rpc.MSRequest;
import cs5204.fs.rpc.MSResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class StorageServer
{
	private static SocketAddress _masterAddr;
	private static Socket _socket;
	private static int _id;
	private static String _ipAddr;
	private static final int DEFAULT_PORT = 2059;
	private static final int MAX_ATTEMPTS = 10;
	
	public static void main(String [] args)
	{
		if (args.length < 2)
		{
			System.out.println("Master IP address and port needed");
			System.exit(0);
		}
		
		//First initiate the contact with the master
		String addr = args[0];
		int port = args[1];
		
		_masterAddr = new InetSocketAddress(addr, port);
		_socket = new Socket();
		_ipAddr = InetAddress.getLocalHost().getHostAddress();
		
		int attempts = 0;
		while (!initiateContact())
		{
			//TODO: Log failure
			if(++attempts > MAX_ATTEMPTS)
			{
				//TODO: Log max rety reached
			}
		}
		
		//TODO: Log successful connection
		
		Thread clientHandler = new Thread(new ClientHandler(DEFAULT_PORT));
	}
	
	public static bool initiateContact()
	{
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		MSRequest req = null;
		MSResponse resp = null;
		boolean success = false;
		
		establishSocketConnection();
		
		req = new MSRequest(_ipAddr, DEFAULT_PORT);
		
		try {
			oos = new ObjectOutputStream(_socket.getOutputStream());
			oos.writeObject(req);
			oos.close();			
		}
		catch (IOException ex) {
			//TODO: Log/fail
		}
		
		try {
			ois = new ObjectInputStream(_socket.getInputStream());
			resp = (MSResponse)ois.readObject();
			ois.close();
		}
		catch (IOException ex) {
			//TODO: log/fail
		}
		catch (ClassNotFoundException ex) {
			//TODO: log/fail
		}
		
		switch (resp.getStatus())
		{
			case OK:
				m_id = resp.getId();
				success = true;
				break;
			case DENIED:
			default:
				success = false;
				break;
		}
		
		return success;
	}
	
	public static void establishSocketConnection()
	{
		_socket = new Socket();
		_socket.connect(_masterAddr);
	}
}