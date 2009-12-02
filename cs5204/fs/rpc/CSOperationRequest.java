package cs5204.fs.rpc;

import cs5204.fs.common.FileOperation;

import java.io.Serializable;

public class CSOperationRequest implements Payload
{
	private int m_id;
	private FileOperation m_operation;
	private String m_filename;
	private byte[] m_data;
	private int m_offset;
	private int m_length;
	
	public CSOperationRequest(int id, FileOperation operation, String filename, byte[] data, int offset, int length)
	{
		m_id = id;
		m_operation = operation;
		m_filename = filename;
		m_offset = offset;
		m_data = data;
		m_length = length;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public FileOperation getFileOperation()
	{
		return m_operation;
	}
	
	public String getFilename()
	{
		return m_filename;
	}
	
	public int getOffset()
	{
		return m_offset;
	}
	
	public byte[] getData()
	{
		return m_data;
	}
	
	public int getLength()
	{
		return m_length;
	}
}