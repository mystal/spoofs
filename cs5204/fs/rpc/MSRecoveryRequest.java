package cs5204.fs.rpc;

import cs5204.fs.lib.Node;

public class MSRecoveryRequest implements Payload
{
	private Node m_targetNode;
	private String m_addr;
	private int m_port;
	
	public MSRecoveryRequest(Node targetNode, String addr, int port)
	{
		m_targetNode = targetNode;
		m_addr = addr;
		m_port = port;
	}
	
	public Node getTargetNode()
	{
		return m_targetNode;
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