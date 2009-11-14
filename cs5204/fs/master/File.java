package cs5204.fs.master;

import java.util.ArrayList;

public class File
{
	private ArrayList<Block> m_blocks;
	private String m_name; //needed?
	private Directory m_parent; //needed?
	
	public File(Directory parent, String name)
	{
		m_parent = parent;
		m_name = name;
		m_blocks = new ArrayList<Block>();
	}
}