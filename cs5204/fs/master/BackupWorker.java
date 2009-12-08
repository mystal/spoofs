package cs5204.fs.master;

import cs5204.fs.common.Protocol;
import cs5204.fs.common.BackupOperation;
import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.Node;
import cs5204.fs.lib.BackupObject;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MBBackupRequest;
import cs5204.fs.rpc.MBBackupResponse;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class BackupWorker implements Runnable
{
	private Worker m_worker;
	private static int DEFAULT_INTERVAL = 10000;
	private String m_addr;
	private int m_port;
	private LinkedList<BackupObject> m_list;
	private ReentrantLock m_lock;
	
	public BackupWorker(String addr, int port)
	{
		m_worker = new Worker();
		m_addr = addr;
		m_port = port;
		m_list = new LinkedList<BackupObject>();
		m_lock = new ReentrantLock();
	}
	
	public void submit(BackupOperation operation, Node node)
	{
		m_lock.lock();
		m_list.add(new BackupObject(operation, node));
		m_lock.unlock();
	}	
	
	public void run()
	{
		while (pulse())
		{
			try {
				Thread.sleep(DEFAULT_INTERVAL);
			}
			catch (InterruptedException ex) {
				//TODO: Log
			}
		}
	}
	
	public boolean pulse()
	{
		m_lock.lock();
		BackupObject[] backups = new BackupObject[m_list.size()];
		m_list.toArray(backups);
		m_list.clear();
		m_lock.unlock();
		Communication resp = m_worker.submitRequest(
								new Communication(
									Protocol.MB_BACKUP_REQUEST,
									new MBBackupRequest(
										backups)),
								m_addr,
								m_port);
		
		if (resp == null)
			return false;
			
		switch(((MBBackupResponse)resp.getPayload()).getStatus())
		{
			case OK:
				//TODO
				break;
			case DENIED:
			default:
				return false;
		}
		
		return true;
	}
}