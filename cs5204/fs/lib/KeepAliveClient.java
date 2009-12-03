package cs5204.fs.lib;

import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.KARequest;
import cs5204.fs.rpc.KAResponse;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;

public class KeepAliveClient implements Runnable
{
	private static int KA_INTERVAL = 5000;
	private int m_id;
	private String m_masterAddr;
	private int m_masterPort;
	private Worker m_worker;
	
	public KeepAliveClient(int id, String addr, int port)
	{
		m_id = id;
		m_masterAddr = addr;
		m_masterPort = port;
		m_worker = new Worker();
	}
	
	public void run()
	{
		while (pulse())
			Thread.sleep(KA_INTERVAL);
	}
	
	public boolean pulse()
	{
		Communication resp = m_worker.submitRequest(
								new Communication(
									Protocol.KA_REQUEST,
									new KARequest(m_id)),
								m_masterAddr,
								m_masterPort));
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