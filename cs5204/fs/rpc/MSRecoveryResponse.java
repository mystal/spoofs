package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

public class MSRecoveryResponse implements Payload
{
	private StatusCode m_status;
	private int m_id;
	private String [] m_filenames;
	
	public MSRecoveryResponse(StatusCode status, int id, String [] filenames)
	{
		m_status = status;
		m_id = id;
		m_filenames = filenames;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public String [] getFilenames()
	{
		return m_filenames;
	}
}