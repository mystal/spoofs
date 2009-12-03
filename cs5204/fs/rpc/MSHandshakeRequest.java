package cs5204.fs.rpc;

import java.io.Serializable;

public class MSHandshakeRequest implements Payload
{
	private String m_ipAddr;
	private int m_clientPort;
	private int m_masterPort;
	
	public MSHandshakeRequest(String ipAddr, int clientPort, int masterPort)
	{
		m_ipAddr = ipAddr;
		m_clientPort = clientPort;
		m_masterPort = masterPort;
	}

	public String getIPAddr()
	{
		return m_ipAddr;
	}
	
	public int getClientPort()
	{
		return m_clientPort;
	}
	
	public int getMasterPort()
	{
		return m_masterPort;
	}
}
