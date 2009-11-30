package cs5204.fs.client;

public class SFile
{
    private String m_filepath;
	private String m_addr;
	private int m_port;
	private int m_pos;
	private boolean m_opened;

	//Only allow same package construction
    protected SFile(String filepath, String addr, int port, boolean opened)
	{
		m_filepath = filepath;
		m_addr = addr;
		m_port = port;
		m_pos = 0;
		m_opened = opened;
	}
	
	protected String getAddress()
	{
		return m_addr;
	}
	
	protected int getPort()
	{
		return m_port;
	}

    public String getPath()
    {
        return m_filepath;
    }
	
	public boolean append(byte[] data)
	{
		return Filesystem.append(this, data);
	}
	
	public byte [] read()
	{
		return Filesystem.read(this);
	}
	
	public void seek(int pos)
	{
		m_pos = pos;
	}
	
	public boolean isOpened()
	{
		return m_opened;
	}
}
