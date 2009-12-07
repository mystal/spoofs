package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class MBHandshakeResponse implements Payload
{
	private StatusCode m_status;
	private int m_kaPort;
	private NodeType[] m_types;
	private String[] m_addrs;
    private int[] m_ports;
	private int[] m_ids;
	
	public MBHandshakeResponse(StatusCode status, int kaPort, NodeType[] types, String[] addrs, int[] ports, int[] ids)
	{
		m_status = status;
		m_kaPort = kaPort;
        m_types = types;
        m_addrs = addrs;
        m_ports = ports;
        m_ids = ids;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}

	public int getKAPort()
	{
		return m_kaPort;
	}

    public NodeType[] getTypes()
    {
        return m_types;
    }

    public String[] getAddresses()
    {
        return m_addrs;
    }

    public int[] getPorts()
    {
        return m_ports;
    }

    public int[] getIds()
    {
        return m_ids;
    }
}

