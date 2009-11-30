package cs5204.fs.rpc;

import cs5204.fs.common.FileOperation;

import java.io.Serializable;

/**
 * Represents a Client-Master Request from client to master.
 */
public class CMOperationRequest implements Payload
{
	private FileOperation m_operation;
	private String m_filename;
	private int m_id;
	
	public CMOperationRequest()
	{
		this(FileOperation.NO_OP, "", -1);
	}
	
	public CMOperationRequest(FileOperation operation, String filename, int id)
	{
		m_operation = operation;
		m_filename = filename;
		m_id = id;
	}
	
	public FileOperation getFileOperation()
	{
		return m_operation;
	}
	
	public void setFileOperation(FileOperation operation)
	{
		m_operation = operation;
	}
	
	public String getFilename()
	{
		return m_filename;
	}
	
	public void setFilename(String fn)
	{
		m_filename = fn;
	}
	
	public int getId()
	{
		return m_id;
	}
	
	public void setId(int id)
	{
		m_id = id;
	}
}