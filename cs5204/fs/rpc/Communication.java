package cs5204.fs.rpc;

import cs5204.fs.common.Protocol;

import java.io.Serializable;

public class Communication implements Serializable
{
	private Protocol m_protocol;
	private Payload m_payload;
	public Communication(Protocol protocol, Payload payload)
	{
		m_protocol = protocol;
		m_payload = payload;
	}
	
	public Protocol getProtocol()
	{
		return m_protocol;
	}
	
	public Payload getPayload()
	{
		return m_payload;
	}
}