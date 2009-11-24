package cs5204.fs.master;

import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MSHandshakeRequest;
import cs5204.fs.rpc.MSHandshakeResponse;
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

public class StorageHandler extends AbstractHandler
{
	public StorageHandler(int port)
	{
        super(port);
	}
	
    protected AbstractHandlerTask createHandlerTask(Socket socket, int id)
    {
        return new StorageHandlerTask(socket, id);
    }

	private class StorageHandlerTask extends AbstractHandlerTask
	{
		public StorageHandlerTask(Socket socket, int id)
		{
            super(socket, id);
		}
		
		protected Communication processRequest(Communication req)
		{
			Communication resp = null;
			
			switch (req.getProtocol())
			{
				case MS_HANDSHAKE_REQUEST:
				{
					MSHandshakeRequest msReq = (MSHandshakeRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					int id = -1;
					String addr = msReq.getIPAddr();
					int port = msReq.getPort();
					
					if ((id = MasterServer.addStorageNode(addr, port)) != -1)
						status = StatusCode.OK;
					
					resp = new Communication(Protocol.MS_HANDSHAKE_RESPONSE, new MSHandshakeResponse(status, id));
					
					//TODO: Log
				} break;
				case MS_COMMIT_REQUEST:
				{
					//TODO: Handle this
				} break;
				default:
					break;
			}
			
			return resp;
		}
	}
}
