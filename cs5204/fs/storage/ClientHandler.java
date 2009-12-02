package cs5204.fs.storage;

import cs5204.fs.common.Protocol;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.CSOperationRequest;
import cs5204.fs.rpc.CSOperationResponse;

import java.net.Socket;

public class ClientHandler extends AbstractHandler
{
	public ClientHandler(int port)
	{
        super(port);
	}
	
    protected AbstractHandlerTask createHandlerTask(Socket socket, int id)
    {
        return new ClientHandlerTask(socket, id);
    }

	private class ClientHandlerTask extends AbstractHandlerTask
	{
		public ClientHandlerTask(Socket socket, int id)
		{
            super(socket, id);
		}
		
		protected Communication processRequest(Communication req)
		{
			Communication resp = null;
			
			switch(req.getProtocol())
			{
				case CS_OPERATION_REQUEST:
					CSOperationRequest csReq = (CSOperationRequest)req.getPayload();
					switch (csReq.getFileOperation())
					{
						case WRITE:
							if (StorageServer.writeFile(csReq.getFilename(), csReq.getData(), csReq.getOffset(), csReq.getLength()))
								resp = new Communication(Protocol.CS_OPERATION_RESPONSE, new CSOperationResponse(StatusCode.OK, null));
							break;
						case READ:
							byte [] data = new byte[csReq.getLength()];
							if (StorageServer.readFile(csReq.getFilename(), data, csReq.getOffset(), csReq.getLength()))
								resp = new Communication(Protocol.CS_OPERATION_RESPONSE, new CSOperationResponse(StatusCode.OK, data));
							break;
						default:
							//TODO: Log/fail
					}
					break;
				default:
					//TODO: Log/fail
			}
			
			return resp;
		}
	}
}
