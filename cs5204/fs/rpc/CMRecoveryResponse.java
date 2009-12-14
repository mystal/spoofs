package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

public class CMRecoveryResponse implements Payload
{
	private StatusCode m_status;
	
	public CMRecoveryResponse(StatusCode status)
	{
		m_status = status;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
}