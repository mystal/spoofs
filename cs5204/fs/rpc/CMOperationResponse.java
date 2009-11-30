package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class CMOperationResponse implements Payload
{
	private StatusCode m_status;
	private String m_addr;
	private int m_port;
	
	public CMOperationResponse(StatusCode status, String addr, int port)
	{
		m_status = status;
		m_addr = addr;
		m_port = port;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
	
	public String getAddress()
	{
		return m_addr;
	}
	
	public int getPort()
	{
		return m_port;
	}
}