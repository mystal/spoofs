package cs5204.fs.master;

public class File
{
	private Directory m_parent; //needed?
	private int m_storId;
	
	public File(Directory parent, int storId)
	{
		m_parent = parent;
		m_storId = storId;
	}
	
	public int getStorId()
	{
		return m_storId;
	}
}