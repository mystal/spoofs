//TODO: Decide on package information

public class ClientTask implements Runnable
{
	private Socket m_socket;
	private int m_id;
	
	public ClientTask(Socket sock, int id)
	{
		m_socket = sock;
		m_id = id;
	}
	
	public void run()
	{
		//TODO: The rest
	}
}