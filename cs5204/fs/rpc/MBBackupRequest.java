package cs5204.fs.rpc;

import cs5204.fs.common.NodeType;

import java.io.Serializable;

/**
 * Represents a Master-Backup Request to send backup data.
 */
public class MBBackupRequest implements Payload
{
	private NodeType m_type;
	private String m_addr;
    private int m_port;
	private int m_id;
	
	public MBBackupRequest(NodeType type, String addr, int port, int id)
	{
        m_type = type;
        m_addr = addr;
        m_port = port;
		m_id = id;
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
	
	public int getId()
	{
		return m_id;
	}
}
