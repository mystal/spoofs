package cs5204.fs.lib;

import cs5204.fs.common.NodeType;

import java.io.Serializable;

public class Node implements Serializable
{
	private NodeType m_type;
	private int m_id;
	private String m_addr;
	private int m_port;
	
	public Node(int id, NodeType type, String addr, int port)
	{
		m_id = id;
		m_type = type;
		m_addr = addr;
		m_port = port;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public NodeType getNodeType()
	{
		return m_type;
	}
	
	public String getAddress()
	{
		return m_addr;
	}
	
	public int getPort()
	{
		return m_port;
	}
}