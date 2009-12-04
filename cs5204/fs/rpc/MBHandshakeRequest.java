package cs5204.fs.rpc;

import java.io.Serializable;

public class MBHandshakeRequest implements Payload
{
	private String m_ipAddr;
	private int m_port;
	
	public MBHandshakeRequest(String ipAddr, int port)
	{
		m_ipAddr = ipAddr;
        m_port = port;
	}

	public String getAddress()
	{
		return m_ipAddr;
	}
	
	public int getPort()
	{
		return m_port;
	}
}

