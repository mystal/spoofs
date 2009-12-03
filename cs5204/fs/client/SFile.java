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
	
	public boolean write(byte [] data)
	{
		return Filesystem.write(this, data, m_pos, data.length);
	}
	
	public boolean read(byte [] data)
	{
		return Filesystem.read(this, data, m_pos, data.length);
	}
	
	public void seek(int pos)
	{
		m_pos = pos;
	}
	
	public void seekEnd()
	{
		m_pos = -1;
	}
	
	public int tell()
	{
		return m_pos;
	}
	
	protected boolean isOpened()
	{
		return m_opened;
	}
	
	protected void setOpened(boolean opened)
	{
		m_opened = opened;
	}
}
