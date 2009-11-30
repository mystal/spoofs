package cs5204.fs.rpc;

import cs5204.fs.common.FileOperation;

import java.io.Serializable;

public class CSOperationRequest implements Payload
{
	private int m_id;
	private FileOperation m_operation;
	private int m_blockId;
	private int m_offset;
	private byte[] m_data;
	
	public CSOperationRequest(int id, FileOperation operation, int blockId, int offset, byte[] data)
	{
		m_id = id;
		m_operation = operation;
		m_blockId = blockId;
		m_offset = offset;
		m_data = data;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public FileOperation getFileOperation()
	{
		return m_operation;
	}
	
	public int getBlockId()
	{
		return m_blockId;
	}
	
	public int getOffset()
	{
		return m_offset;
	}
	
	public byte[] getData()
	{
		return m_data;
	}
}