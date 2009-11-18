package cs5204.fs.master;

import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.CMRequest;
import cs5204.fs.rpc.CMResponse;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.Protocol;

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

public class ClientHandler implements Runnable
{
	private final int CORE_POOL_SIZE = 10;
	private final int MAX_POOL_SIZE = 100;
	private final long KEEP_ALIVE_TIME = 100;

	private int m_port;
	private ServerSocket m_serverSocket;
	private ExecutorService m_exec;
	
	public ClientHandler(int port)
	{
		m_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
		
		m_port = port;
	}
	
	public void run()
	{
		try {
			m_serverSocket = new ServerSocket(m_port);
			//TODO: Decide on timeout and reuse
		}
		catch (IOException ex) {
			//TODO: Log
			return;
		}	
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
				m_exec.submit(new ClientHandlerTask(sock, counter++));
			}
		}
	}
	
	private class ClientHandlerTask implements Runnable
	{
		private int m_id;
		private Socket m_clientSocket;
		
		public ClientHandlerTask(Socket socket, int id)
		{
			m_id = id;
			m_clientSocket = socket;
		}
		
		public void run()
		{
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			Communication comm = null;
			CMRequest req = null;
			CMResponse resp = null;
			
			try {
				ois = new ObjectInputStream(m_clientSocket.getInputStream());
				comm = (Communication)ois.readObject();
				req = (CMRequest)comm.getPayload();
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
				oos = new ObjectOutputStream(m_clientSocket.getOutputStream());
				comm = new Communication(Protocol.CM_RESPONSE, resp);
				oos.writeObject(comm);
				oos.close();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			
			try {
				m_clientSocket.close();
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
		}
		
		private CMResponse processRequest(CMRequest req)
		{
			CMResponse resp = null;
			
			/**
			 *	Use the master server to handle all the different operations.
			 *  Only use master server as needed, as synchronization will be
			 *  required for some operations.
			 */
			switch (req.getFileOperation())
			{
				case CREATE:
					//TODO
					break;
				case MKDIR:
					//TODO
					break;
				case OPEN:
					//TODO
					break;
				case CLOSE:
					//TODO
					break;
				case READ:
				case WRITE:
				case APPEND:
					//TODO
				case REMOVE:
					//TODO
					break;
				case RMDIR:
					//TODO
					break;
				case NO_OP:
				default:
					//TODO: Log no operation
			}
			
			return resp;
		}
	}
}
