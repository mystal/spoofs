package cs5204.fs.lib;

import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;

import java.io.Serializable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
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

public class Worker
{
	private static final int CORE_POOL_SIZE = 10;
	private static final int MAX_POOL_SIZE = 100;
	private static final long KEEP_ALIVE_TIME = 100;
	
	private ExecutorService m_exec;
	private AtomicInteger m_counter;
	
	public Worker()
	{
		m_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
		m_counter = new AtomicInteger(0);
	}
	
	public Communication submitRequest(Communication req, String addr, int port)
	{
		int count = m_counter.getAndIncrement();
		WorkerTask task = new WorkerTask(req, new InetSocketAddress(addr, port), count);
		try {
			m_exec.submit(task).get();
		}
		catch (InterruptedException ex) {
			//TODO: Log
			return null;
		}
		catch (ExecutionException ex) {
			//TODO: Log
			return null;
		}
		return task.getResponse();
	}
	
	private class WorkerTask implements Runnable
	{
		private Communication m_req;
		private Communication m_resp;
		private SocketAddress m_socketAddress;
		private int m_id;
		
		public WorkerTask(Communication req, SocketAddress socketAddress, int id)
		{
			m_req = req;
			m_resp = null;
			m_socketAddress = socketAddress;
			m_id = id;
		}
		
		public void run()
		{
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			Socket sock = null;
			
			try {
				sock = new Socket();
				sock.connect(m_socketAddress);
				
				oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject(m_req);
				oos.flush();
				
				ois = new ObjectInputStream(sock.getInputStream());
				m_resp = (Communication)ois.readObject();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			catch (ClassNotFoundException ex) {
				//TODO: Log/fail
			}
			finally {
				try {
					if (oos != null)
						oos.close();
					if (ois != null)
						ois.close();
					sock.close();
				}
				catch (IOException ex) {
					//TODO: Log/fail
				}
			}
		}
		
		public Communication getResponse()
		{
			return m_resp;
		}
	}
}