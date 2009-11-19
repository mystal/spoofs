package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class CMOperationResponse implements Payload
{
	private StatusCode m_status;
	//TODO: Location of blocks
	
	public CMOperationResponse(StatusCode status)
	{
		m_status = status;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
}