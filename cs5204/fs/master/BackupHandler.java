package cs5204.fs.master;

import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.lib.Node;
import cs5204.fs.lib.BackupObject;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MBBackupRequest;
import cs5204.fs.rpc.MBBackupResponse;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;

import java.net.Socket;

public class BackupHandler extends AbstractHandler
{
	public BackupHandler(int port)
	{
        super(port);
	}
	
    protected AbstractHandlerTask createHandlerTask(Socket socket, int id)
    {
        return new BackupHandlerTask(socket, id);
    }

	private class BackupHandlerTask extends AbstractHandlerTask
	{
		public BackupHandlerTask(Socket socket, int id)
		{
            super(socket, id);
		}
		
		protected Communication processRequest(Communication req)
		{
			Communication resp = null;
			StringBuilder msg = new StringBuilder();
			
			switch (req.getProtocol())
			{
				case MB_BACKUP_REQUEST:
				{
					msg.append("Master backup request detected...\n");
                    MBBackupRequest mbReq = (MBBackupRequest)req.getPayload();
					BackupObject [] objects = mbReq.getBackupObjects();
					msg.append("Iterating through objects...\n");
					for (BackupObject obj : objects)
					{
						switch (obj.getBackupOperation())
						{
							case ADD:
								msg.append("Adding node...\n");
								MasterBackup.addNode(obj.getNode());
								break;
							case REMOVE:
								msg.append("Removing node...\n");
								MasterBackup.removeNode(obj.getNode());
								break;
							default:
								msg.append("No operation detected!\n");
						}
					}
					
					resp = new Communication(Protocol.MB_BACKUP_RESPONSE, new MBBackupResponse(StatusCode.OK));
				} break;

				default:
					break;
			}
			
			MasterBackup.info(msg.toString());
			
			return resp;
		}
	}
}
