package cs5204.fs.master;

import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MBHandshakeRequest;
import cs5204.fs.rpc.MBHandshakeResponse;
import cs5204.fs.common.Protocol;

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
			
			switch (req.getProtocol())
			{
				case MB_BACKUP_REQUEST:
				{					
					//TODO: Add node to MasterBackup through static methods
				} break;

				default:
					break;
			}
			
			return resp;
		}
	}
}