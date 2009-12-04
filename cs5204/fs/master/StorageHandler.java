package cs5204.fs.master;

import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MSHandshakeRequest;
import cs5204.fs.rpc.MSHandshakeResponse;
import cs5204.fs.common.Protocol;

import java.net.Socket;

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
					int clientPort = msReq.getClientPort();
					int masterPort = msReq.getMasterPort();
					
					if ((id = MasterServer.addStorageNode(addr, clientPort, masterPort)) != -1)
						status = StatusCode.OK;
					
					resp = new Communication(Protocol.MS_HANDSHAKE_RESPONSE, new MSHandshakeResponse(status, id));
					
					//TODO: Log
				} break;

				default:
					break;
			}
			
			return resp;
		}
	}
}
