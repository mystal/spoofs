package cs5204.fs.storage;

import cs5204.fs.common.StatusCode;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.FileOperation;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.CSOperationRequest;
import cs5204.fs.rpc.CSOperationResponse;
import cs5204.fs.rpc.MSCommitRequest;
import cs5204.fs.rpc.MSCommitResponse;
import cs5204.fs.rpc.MSRecoveryRequest;
import cs5204.fs.rpc.MSRecoveryResponse;

import java.net.Socket;

public class MainHandler extends AbstractHandler
{
	public MainHandler(int port)
	{
        super(port);
	}
	
    protected AbstractHandlerTask createHandlerTask(Socket socket, int id)
    {
        return new MainHandlerTask(socket, id);
    }

	private class MainHandlerTask extends AbstractHandlerTask
	{
		public MainHandlerTask(Socket socket, int id)
		{
            super(socket, id);
		}
		
		protected Communication processRequest(Communication req)
		{
			Communication resp = null;
			
			switch (resp.getProtocol())
			{
				case CS_OPERATION_REQUEST:
				{
					CSOperationRequest csReq = (CSOperationRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					byte [] data = null;
					switch (csReq.getFileOperation())
					{
						case WRITE:
							if (StorageServer.writeFile(csReq.getFilename(), csReq.getData(), csReq.getOffset(), csReq.getLength()))
								status = StatusCode.OK;
							break;
						case READ:
							byte [] buffer = new byte[csReq.getLength()];
							if (StorageServer.readFile(csReq.getFilename(), data, csReq.getOffset(), csReq.getLength()))
							{
								data = buffer;
								status = StatusCode.OK;
							}
							break;
						default:
							//TODO: Log/fail
					}
					resp = new Communication(Protocol.CS_OPERATION_RESPONSE, new CSOperationResponse(status, data));
				} break;
				
				case MS_COMMIT_REQUEST:
				{
					MSCommitRequest msReq = (MSCommitRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					switch(msReq.getFileOperation())
					{
						case CREATE:
							if (StorageServer.createFile(msReq.getFilename()))
								status = StatusCode.OK;
							break;
						case REMOVE:
							if (StorageServer.removeFile(msReq.getFilename()))
								status = StatusCode.OK;
							break;
						default:
							//TODO: Log
					}
					resp = new Communication(Protocol.MS_COMMIT_RESPONSE, new MSCommitResponse(status));
				} break;
				
				case MS_RECOVERY_REQUEST:
				{
					MSRecoveryRequest msReq = (MSRecoveryRequest)req.getPayload();
					
					StatusCode status = StatusCode.DENIED;
					int id = -1;
					String [] filenames = null;
					
					if (StorageServer.verifyBackup(msReq.getTargetNode()))
					{
						StorageServer.setMaster(msReq.getAddress(), msReq.getPort(), msReq.getKAPort());
						filenames = StorageServer.constructRecoveryState();
						id = StorageServer.getId();
						status = StatusCode.OK;
					}
					
					resp = new Communication(Protocol.MS_RECOVERY_RESPONSE, new MSRecoveryResponse(status, id, filenames));
				}
				
				default:
					//TODO: Log/fail
			}
			
			return resp;
		}
	}
}