package cs5204.fs.storage;

import cs5204.fs.common.StatusCode;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.FileOperation;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.lib.OneWayWorker;
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
			
			switch (req.getProtocol())
			{
				case CS_OPERATION_REQUEST:
				{
                    StorageServer.info("Client operation request...");
					CSOperationRequest csReq = (CSOperationRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					byte [] data = null;
					switch (csReq.getFileOperation())
					{
						case APPEND:
							StorageServer.info("Write request... for " + csReq.getFilename() + " for  "  + csReq.getLength());
							if (StorageServer.appendFile(csReq.getFilename(), csReq.getData(), csReq.getLength()))
								status = StatusCode.OK;
							break;
						case WRITE:
                            StorageServer.info("Write request... for " + csReq.getFilename() + " for "  + csReq.getLength() + " bytes.");
							if (StorageServer.writeFile(csReq.getFilename(), csReq.getData(), csReq.getOffset(), csReq.getLength()))
								status = StatusCode.OK;
							break;
						case READ:
                            StorageServer.info("Read request...");
							byte [] buffer = new byte[csReq.getLength()];
							if (StorageServer.readFile(csReq.getFilename(), buffer, csReq.getOffset(), csReq.getLength()))
							{
								data = new byte[buffer.length];
								for (int i  = 0 ; i < buffer.length ; i++)
									data[i] = buffer[i];
								status = StatusCode.OK;
							}
							break;
						default:
							StorageServer.warning("Unknown client operation request!");
					}
					resp = new Communication(Protocol.CS_OPERATION_RESPONSE, new CSOperationResponse(status, data));
				} break;
				
				case MS_COMMIT_REQUEST:
				{
                    StorageServer.info("Master commit request...");
					MSCommitRequest msReq = (MSCommitRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					switch(msReq.getFileOperation())
					{
						case CREATE:
                            StorageServer.info("Create file request...");
							if (StorageServer.createFile(msReq.getFilename()))
								status = StatusCode.OK;
							break;
						case REMOVE:
                            StorageServer.info("Remove file request...");
							if (StorageServer.removeFile(msReq.getFilename()))
								status = StatusCode.OK;
							break;
						default:
							StorageServer.warning("Unknown master commit request!");
					}
					resp = new Communication(Protocol.MS_COMMIT_RESPONSE, new MSCommitResponse(status));
				} break;
				
				case MS_RECOVERY_REQUEST:
				{
                    StorageServer.info("Master recovery request...");
					MSRecoveryRequest msReq = (MSRecoveryRequest)req.getPayload();
					
					StatusCode status = StatusCode.DENIED;
					int id = -1;
					String [] filenames = null;
					
                    StorageServer.info("Verifying backup node...  " + (msReq.getTargetNode() == null));
					if (StorageServer.verifyBackup(msReq.getTargetNode()))
					{
                        StorageServer.info("Backup node good, proceeding with recovery...");
						StorageServer.stopKA();
						StorageServer.setMaster(msReq.getAddress(), msReq.getPort(), msReq.getKAPort());
						filenames = StorageServer.constructRecoveryState();
						id = StorageServer.getId();
						status = StatusCode.OK;
						StorageServer.startKA();
					}
					StorageServer.info("Creating reponse to master...  " + filenames.length);
					resp = new Communication(Protocol.MS_RECOVERY_RESPONSE, new MSRecoveryResponse(status, id, filenames));
				} break;
				
				default:
                    StorageServer.warning("Unknown request!");
			}
			
			return resp;
		}
	}
}
