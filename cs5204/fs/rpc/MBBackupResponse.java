package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class MBBackupResponse
{
	private StatusCode m_status;
	
	public MBBackupResponse(StatusCode status)
	{
		m_status = status;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
}