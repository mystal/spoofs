package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;

import java.io.Serializable;

public class CMOperationResponse implements Payload
{
	private StatusCode m_status;
	private String [] m_arrIPAddr;
	private int [] m_arrPort;
	
	public CMOperationResponse(StatusCode status, String [] addrs, int [] ports)
	{
		m_status = status;
		m_arrIPAddr = addrs;
		m_arrPort = ports;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}
	
	public String [] getIPAddresses()
	{
		return m_arrIPAddr;
	}
	
	public int [] getPorts()
	{
		return m_arrPort;
	}
}