package cs5204.fs.rpc;

import java.io.Serializable;

public class CMHandshakeRequest implements Payload
{
	private String m_ipAddr;
	private int m_port;
	
	public CMHandshakeRequest(String ipAddr, int port)
	{
		m_ipAddr = ipAddr;
		m_port = port;
	}

	public String getIPAddr()
	{
		return m_ipAddr;
	}
	
	public int getPort()
	{
		return m_port;
	}
}

