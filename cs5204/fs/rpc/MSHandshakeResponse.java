package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class MSHandshakeResponse implements Payload
{
	private StatusCode m_status;
	private int m_id;
	private int m_kaPort;
	
	public MSHandshakeResponse(StatusCode status, int id, int kaPort)
	{
		m_status = status;
		m_id = id;
		m_kaPort = kaPort;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public int getKAPort()
	{
		return m_kaPort;
	}
}