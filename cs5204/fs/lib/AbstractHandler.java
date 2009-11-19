package cs5204.fs.lib;

import cs5204.fs.rpc.Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.net.ServerSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractHandler implements Runnable
{
	protected final int CORE_POOL_SIZE = 10;
	protected final int MAX_POOL_SIZE = 100;
	protected final long KEEP_ALIVE_TIME = 100;

	protected int m_port;
	protected ServerSocket m_serverSocket;
	protected ExecutorService m_exec;
	
	public AbstractHandler(int port)
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
				m_exec.submit(createHandlerTask(sock, counter++));
			}
		}
	}
	
    protected abstract AbstractHandlerTask createHandlerTask(Socket socket, int id);

    protected abstract class AbstractHandlerTask implements Runnable
    {
		protected int m_id;
		protected Socket m_mySocket;

        public AbstractHandlerTask(Socket socket, int id)
        {
			m_id = id;
			m_mySocket = socket;
        }

        public void run()
		{
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			Communication req = null;
			Communication resp = null;
			
			try {
				ois = new ObjectInputStream(m_mySocket.getInputStream());
				req = (Communication)ois.readObject();
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
				oos = new ObjectOutputStream(m_mySocket.getOutputStream());
				oos.writeObject(resp);
				oos.flush();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			
			try {
				ois.close();
				oos.close();
				m_mySocket.close();
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
		}
		
		protected abstract Communication processRequest(Communication req);
    }
}

