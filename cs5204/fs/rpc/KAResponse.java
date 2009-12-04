package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class KAResponse
{
	private StatusCode m_status;

	public KAResponse(StatusCode status)
	{
		m_status = status;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
}
