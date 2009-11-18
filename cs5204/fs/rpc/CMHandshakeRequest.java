package cs5204.fs.rpc;

import java.io.Serializable;

public class CMHandshakeRequest implements Payload
{
	private String m_ipAddr;
	public CMHandshakeRequest(String ipAddr)
	{
		m_ipAddr = ipAddr;
	}

	public String getIPAddr()
	{
		return m_ipAddr;
	}
}

