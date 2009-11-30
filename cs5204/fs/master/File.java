package cs5204.fs.master;

import java.util.ArrayList;

public class File
{
	//private ArrayList<Block> m_blocks;
	private Directory m_parent; //needed?
	private int m_size;
	private int m_storId;
	
	public File(Directory parent, int storId)
	{
		m_parent = parent;
		//m_blocks = new ArrayList<Block>();
		m_size = 0;
		m_storId = storId;
	}
	
	public int getSize()
	{
		return m_size;
	}
	
	public void setSize(int size)
	{
		m_size = size;
	}
	
	public int getStorId()
	{
		return m_storId;
	}
}