package cs5204.fs.rpc;

import cs5204.fs.common.NodeType;

import java.io.Serializable;

public class KARequest implements Payload
{
	private int m_id;
	private NodeType m_type;
	
	public KARequest(NodeType type, int id)
	{
		m_type = type;
		m_id = id;
	}
	
	public NodeType getNodeType()
	{
		return m_type;
	}
	
	public int getId()
	{
		return m_id;
	}
}