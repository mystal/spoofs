package cs5204.fs.lib;

import cs5204.fs.common.NodeType;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.KARequest;
import cs5204.fs.rpc.KAResponse;

import java.util.concurrent.atomic.AtomicBoolean;

public class KeepAliveClient implements Runnable
{
	private static int KA_INTERVAL = 5000;
	private NodeType m_type;
	private int m_id;
	private String m_masterAddr;
	private int m_masterPort;
	private Worker m_worker;
	private AtomicBoolean m_alive;
	
	public KeepAliveClient(NodeType type, int id, String addr, int port)
	{
		this(type, id, addr, port, 0);
	}
	
	public KeepAliveClient(NodeType type, int id, String addr, int port, int timeout)
	{
		m_type = type;
		m_id = id;
		m_masterAddr = addr;
		m_masterPort = port;
		m_alive = new AtomicBoolean(true);
		m_worker = new Worker(timeout);
	}
	
	public void run()
	{
		while (m_alive.get() && pulse())
		{
			try {
				Thread.sleep(KA_INTERVAL);
			}
			catch (InterruptedException ex) {
				//TODO: Log/fail
			}
		}
	}
	
	public void stop()
	{
		m_alive.set(false);
	}
	
	public boolean pulse()
	{
		Communication resp = m_worker.submitRequest(
								new Communication(
									Protocol.KA_REQUEST,
									new KARequest(
										m_type,
										m_id)),
								m_masterAddr,
								m_masterPort);
		if (resp == null)
			return false;
		KAResponse kaResp = (KAResponse)resp.getPayload();
		switch(kaResp.getStatus())
		{
			case OK:
				//Nothing
				break;
			case DENIED:
			default:
				return false;
		}
		return true;
	}
}
