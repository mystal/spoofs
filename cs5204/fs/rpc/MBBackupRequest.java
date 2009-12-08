package cs5204.fs.rpc;

import cs5204.fs.common.NodeType;
import cs5204.fs.lib.BackupObject;

import java.io.Serializable;

/**
 * Represents a Master-Backup Request to send backup data.
 */
public class MBBackupRequest implements Payload
{
	private BackupObject [] m_objects;
	
	public MBBackupRequest(BackupObject[] objects)
	{
        m_objects = objects;
	}
	
	public BackupObject [] getBackupObjects()
	{
		return m_objects;
	}
}
