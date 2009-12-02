package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class CSOperationResponse implements Payload
{
	private StatusCode m_status;
	private byte [] m_data;
	public CSOperationResponse(StatusCode status, byte [] data)
	{
		m_status = status;
		m_data = data;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
	
	public byte [] getData()
	{
		return m_data;
	}
}