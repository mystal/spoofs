package cs5204.fs.master;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable
{
	private final int CORE_POOL_SIZE = 10;
	private final int MAX_POOL_SIZE = 100;
	private final long KEEP_ALIVE_TIME = 100;
	private final int DEFAULT_PORT = 2009;

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
	
	public void go()
	{
		try {
			m_serverSocket = new ServerSocket(port);
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
			BufferedReader in = new BufferedReader(new InputStream(m_clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(m_clientSocket.getOutputStream());
			
			//TODO: process
			
			out.close();
			in.close();
			m_clientSocket.close();
		}
	}
}
