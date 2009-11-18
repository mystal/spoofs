package cs5204.fs.master;

import cs5204.fs.rpc.MSRequest;
import cs5204.fs.rpc.MSResponse;
import cs5204.fs.common.StatusCode;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class StorageHandler implements Runnable
{
	private final int CORE_POOL_SIZE = 10;
	private final int MAX_POOL_SIZE = 100;
	private final long KEEP_ALIVE_TIME = 100;

	private int m_port;
	private ServerSocket m_serverSocket;
	private ExecutorService m_exec;
	
	public StorageHandler(int port)
	{
		m_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
		
		m_port = port;
	}
	
	public void run()
	{
		System.out.println("Attempting to start StorageHandler on port " + m_port + "...");
		try {
			m_serverSocket = new ServerSocket(m_port);
			//TODO: Decide on timeout and reuse
		}
		catch (IOException ex) {
			//TODO: Log
			return;
		}	
		
		System.out.println("StorageHandler successfully started!");
		
		int counter = 0;
		while (true)
		{
			Socket sock = null;
			try {
				sock = m_serverSocket.accept();
				//TODO: set timeout
			}
			catch (IOException ex) {
				//TODO: Log
			}
			
			if (sock != null)
			{
				//TODO: Log an accepted connection
				m_exec.submit(new StorageHandlerTask(sock, counter++));
			}
		}
	}
	
	private class StorageHandlerTask implements Runnable
	{
		private int m_id;
		private Socket m_storageSocket;
		
		public StorageHandlerTask(Socket socket, int id)
		{
			m_id = id;
			m_storageSocket = socket;
		}
		
		public void run()
		{
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			MSRequest req = null;
			MSResponse resp = null;
			
			try {
				ois = new ObjectInputStream(m_storageSocket.getInputStream());
				req = (MSRequest)ois.readObject();
				ois.close();
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
			catch (ClassNotFoundException ex) {
				//TODO: Log/fail
			}
			
			resp = processRequest(req);
				
			try {
				oos = new ObjectOutputStream(m_storageSocket.getOutputStream());
				oos.writeObject(resp);
				oos.close();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			
			try {
				m_storageSocket.close();
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
		}
		
		private MSResponse processRequest(MSRequest req)
		{
			MSResponse resp = null;
			StatusCode status = DENIED;
			int id = -1;
			
			String addr = resp.getIpAddr();
			int port = resp.getPort();
			
			if (MasterServer.addStorageNode(resp.getIpAddr(), resp.getPort()))
			{
				status = OK;
				id = MasterServer.lookupStorageNode(resp.getIpAddr(), resp.getPort());
			}
			
			resp = new MSResponse(status, id);
			
			return resp;
		}
	}
}
