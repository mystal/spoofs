package cs5204.fs.lib;

import cs5204.fs.rpc.Communication;

import java.io.Serializable;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class OneWayWorker
{
	private static final int CORE_POOL_SIZE = 10;
	private static final int MAX_POOL_SIZE = 100;
	private static final long KEEP_ALIVE_TIME = 100;
	
	private ExecutorService m_exec;
	private AtomicInteger m_counter;
	
	public OneWayWorker()
	{
		m_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
		m_counter = new AtomicInteger(0);
	}
	
	public void submitRequest(Communication req, String addr, int port)
	{
		int count = m_counter.getAndIncrement();
		OneWayWorkerTask task = new OneWayWorkerTask(req, new InetSocketAddress(addr, port), count);
		m_exec.submit(task);
	}
	
	private class OneWayWorkerTask implements Runnable
	{
		private Communication m_req;
		private SocketAddress m_socketAddress;
		private int m_id;
		
		public OneWayWorkerTask(Communication req, SocketAddress socketAddress, int id)
		{
			m_req = req;
			m_socketAddress = socketAddress;
			m_id = id;
		}
		
		public void run()
		{
			ObjectOutputStream oos = null;
			Socket sock = null;
			
			try {
				sock = new Socket();
				sock.connect(m_socketAddress);
				
				oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject(m_req);
				oos.flush();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			finally {
				try {
					if (oos != null)
						oos.close();
					sock.close();
				}
				catch (IOException ex) {
					//TODO: Log/fail
				}
			}
		}
	}
}