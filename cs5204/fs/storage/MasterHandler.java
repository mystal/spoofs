package cs5204.fs.storage;

import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.MSCommitRequest;
import cs5204.fs.rpc.MSCommitResponse;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.FileOperation;

import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;

public class MasterHandler extends AbstractHandler
{
	public MasterHandler(int port)
	{
        super(port);
	}
	
    protected AbstractHandlerTask createHandlerTask(Socket socket, int id)
    {
        return new MasterHandlerTask(socket, id);
    }

	private class MasterHandlerTask extends AbstractHandlerTask
	{
		public MasterHandlerTask(Socket socket, int id)
		{
            super(socket, id);
		}
		
		public Communication processRequest(Communication req)
		{
			Communication resp = null;
			
			switch (req.getProtocol())
			{
				case MS_COMMIT_REQUEST:
					MSCommitRequest msReq = (MSCommitRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					switch(msReq.getFileOperation())
					{
						case CREATE:
							if (StorageServer.createFile(msReq.getFilename()))
								status = StatusCode.OK;
							break;
						case REMOVE:
							//TODO
							break;
						default:
							//TODO: Log
					}
					resp = new Communication(Protocol.MS_COMMIT_REQUEST, new MSCommitResponse(status));
					break;
				default:
					//TODO: Log/fail
			}
			
			return resp;
		}
	}
}
