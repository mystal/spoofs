//TODO: Decide on hierarchy
import java.net.Socket;
import java.net.ServerSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable
{
	private int m_port;
	private ServerSocket m_socket;
	private ExecutorService m_exec;
	
	public ClassHandler(int port)
	{
		m_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
		
		m_port = port;		
	}
	
	public void go()
	{
		try {
			m_socket = new ServerSocket(port);
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
				sock = m_socket.accept();
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
		
		public ClientHandlerTask(int id)
		{
			m_id = id;
		}
		
		public void go()
		{
		}
	}
}