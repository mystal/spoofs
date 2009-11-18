package cs5204.fs.storage;

import cs5204.fs.lib.AbstractHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.net.ServerSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler extends AbstractHandler
{
	private final int CORE_POOL_SIZE = 10;
	private final int MAX_POOL_SIZE = 100;
	private final long KEEP_ALIVE_TIME = 100;

	private int m_port;
	private ServerSocket m_serverSocket;
	private ExecutorService m_exec;
	
	public ClientHandler(int port)
	{
		m_port = port;
		
		m_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
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
		}
	}
}
