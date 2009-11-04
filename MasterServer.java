//TODO: decide on package hierarchy

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

import java.net.ServerSocket;
import java.net.Socket;


public class MasterServer
{
	private final int CORE_POOL_SIZE = 10;
	private final int MAX_POOL_SIZE = 100;
	private final long KEEP_ALIVE_TIME = 100;
	private final int DEFAULT_PORT = 2009;
	
	private ServerSocket m_socket;
	private ExecutorService m_exec;
	
	public static void main(String [] args)
	{
		//TODO: decide what args to send in to the main() method
		//TODO: check the length of the args
		
		
		new MasterServer(DEFAULT_PORT);
	}
	
	public MasterServer(int port)
	{
		m_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
		
		//TODO: Set up ClientHandler, StorageHandler, KeepAliveHandler
		
		/*try {
			m_socket = new ServerSocket(port);
			//TODO: decide on timeout and reuseAddress
		}
		catch (IOException ex) {
			//TODO: Log
			return;
		}
		
		int counter = 0;
		while (true) //daemon thread
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
				m_exec.submit(new ClientTask(sock, counter++));
			}
		}*/
	}
}