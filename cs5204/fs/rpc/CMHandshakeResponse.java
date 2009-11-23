package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class CMHandshakeResponse implements Payload
{
	private StatusCode m_status;
	private int m_id;
	private int m_blockSize;
	
	public CMHandshakeResponse(StatusCode status, int id, int blockSize)
	{
		m_status = status;
		m_id = id;
		m_blockSize = blockSize;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public int getBlockSize()
	{
		return m_blockSize;
	}
}

