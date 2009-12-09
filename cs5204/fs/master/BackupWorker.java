package cs5204.fs.master;

import cs5204.fs.common.Protocol;
import cs5204.fs.common.BackupOperation;
import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.BackupObject;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MBBackupRequest;
import cs5204.fs.rpc.MBBackupResponse;

public class BackupWorker implements Runnable
{
	private Worker m_worker;
	private static int DEFAULT_INTERVAL = 10000;
	private String m_addr;
	private int m_port;
	
	public BackupWorker(String addr, int port)
	{
		m_worker = new Worker();
		m_addr = addr;
		m_port = port;
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
		BackupObject[] backups = MasterServer.getBackupObjects();
		
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