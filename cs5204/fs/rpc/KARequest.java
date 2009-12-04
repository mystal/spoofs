package cs5204.fs.rpc;

import java.io.Serializable;

public class KARequest
{
	private int m_id;
	public KARequest(int id)
	{
		m_id = id;
	}
	
	public int getId()
	{
		return m_id;
	}
}