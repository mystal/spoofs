package cs5204.fs.lib;

import cs5204.fs.common.BackupOperation;

import java.io.Serializable;

public class BackupObject implements Serializable
{
	private BackupOperation m_operation;
	private Node m_node;
	
	public BackupObject(BackupOperation operation, Node node)
	{
		m_operation = operation;
		m_node = node;
	}
	
	public BackupOperation getBackupOperation()
	{
		return m_operation;
	}
	
	public Node getNode()
	{
		return m_node;
	}
}