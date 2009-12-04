package cs5204.fs.master;

import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MBHandshakeRequest;
import cs5204.fs.rpc.MBHandshakeResponse;
import cs5204.fs.common.Protocol;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

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
				case MB_HANDSHAKE_REQUEST:
				{
					MBHandshakeRequest mbReq = (MBHandshakeRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					int id = -1;
					String addr = mbReq.getAddress();
					int port = mbReq.getPort();
					
					if ((id = MasterServer.addBackupNode(addr, port)) != -1)
						status = StatusCode.OK;
					
					resp = new Communication(Protocol.MB_HANDSHAKE_RESPONSE, new MBHandshakeResponse(status, id));
					
					//TODO: Log
				} break;

				default:
					break;
			}
			
			return resp;
		}
	}
}
