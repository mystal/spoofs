package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class MBHandshakeResponse implements Payload
{
	private StatusCode m_status;
	private int m_id;
	
	public MBHandshakeResponse(StatusCode status, int id)
	{
		m_status = status;
        m_id = id;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}

    public int getId()
    {
        return m_id;
    }
}

