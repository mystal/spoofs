package cs5204.fs.rpc;

import cs5204.fs.common.FileOperation;

import java.io.Serializable;

public class MSCommitRequest implements Payload
{
	private FileOperation m_operation;
	private String m_filename;
	public MSCommitRequest(FileOperation operation, String filename)
	{
		m_operation = operation;
		m_filename = filename;
	}
	
	public FileOperation getFileOperation()
	{
		return m_operation;
	}
	
	public String getFilename()
	{
		return m_filename;
	}
}