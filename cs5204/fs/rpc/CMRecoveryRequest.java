package cs5204.fs.rpc;

import cs5204.fs.lib.Node;

public class CMRecoveryRequest implements Payload
{
	private Node m_node;
	private String m_addr;
	private int m_port;
	
	public CMRecoveryRequest(Node node, String addr, int port)
	{
		m_node = node;
		m_addr = addr;
		m_port = port;
	}
	
	public Node getTargetNode()
	{
		return m_node;
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