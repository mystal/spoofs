package cs5204.fs.master;

public class Block
{
	private int m_id; //needed?
	private int m_storId;
	private File m_parent; //needed?
	
	public Block(File parent, int id, int storId)
	{
		m_id = id;
		m_storId = storId;
		m_parent = parent;
	}
}