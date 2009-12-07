package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class MBHandshakeResponse implements Payload
{
	private StatusCode m_status;
	private int m_kaPort;
	
	public MBHandshakeResponse(StatusCode status, int kaPort)
	{
		m_status = status;
		m_kaPort = kaPort;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}

	public int getKAPort()
	{
		return m_kaPort;
	}
}

