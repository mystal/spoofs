package cs5204.fs.rpc;

import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.BackupObject;

import java.io.Serializable;

public class MBHandshakeResponse implements Payload
{
	private StatusCode m_status;
	private int m_kaPort;
	private BackupObject [] m_objects;
	
	public MBHandshakeResponse(StatusCode status, int kaPort, BackupObject [] objects)
	{
		m_status = status;
		m_kaPort = kaPort;
        m_objects = objects;
	}
	
	public StatusCode getStatus()
	{
		return m_status;
	}

	public int getKAPort()
	{
		return m_kaPort;
	}

    public BackupObject[] getBackupObjects()
    {
        return m_objects;
    }
}

