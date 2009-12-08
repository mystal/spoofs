package cs5204.fs.master;

import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.KARequest;
import cs5204.fs.rpc.KAResponse;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.NodeType;

import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;

public class KeepAliveHandler extends AbstractHandler
{
	public KeepAliveHandler(int port)
	{
        super(port);
	}
	
    protected AbstractHandlerTask createHandlerTask(Socket socket, int id)
    {
        return new KeepAliveHandlerTask(socket, id);
    }

	private class KeepAliveHandlerTask extends AbstractHandlerTask
	{
		public KeepAliveHandlerTask(Socket socket, int id)
		{
            super(socket, id);
		}
		
		public Communication processRequest(Communication req)
		{
			Communication resp = null;
			
			switch (req.getProtocol())
			{
				case KA_REQUEST:
				{
                    KARequest kaReq = (KARequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
                    if(MasterServer.processKA(kaReq.getNodeType(), kaReq.getId()))
						status = StatusCode.OK;
					resp = new Communication(Protocol.KA_RESPONSE, new KAResponse(status));
				} break;
				default:
					//TODO: Log/fail
			}
			
			return resp;
		}
	}
}
