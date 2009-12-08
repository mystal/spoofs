package cs5204.fs.rpc;

public class CMFootshakeRequest implements Payload
{
	private int m_id;
	
	public CMFootshakeRequest(int id)
	{
		m_id = id;
	}
	
	public int getId()
	{
		return m_id;
	}
}