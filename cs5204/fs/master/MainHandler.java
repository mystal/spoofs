package cs5204.fs.master;

import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.FileOperation;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MSHandshakeRequest;
import cs5204.fs.rpc.MSHandshakeResponse;
import cs5204.fs.rpc.CMHandshakeRequest;
import cs5204.fs.rpc.CMHandshakeResponse;
import cs5204.fs.rpc.CMOperationRequest;
import cs5204.fs.rpc.CMOperationResponse;
import cs5204.fs.rpc.MBHandshakeRequest;
import cs5204.fs.rpc.MBHandshakeResponse;

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
				case CM_HANDSHAKE_REQUEST:
				{
					CMHandshakeRequest cmReq = (CMHandshakeRequest)req.getPayload();
					int id = MasterServer.addClientNode(cmReq.getIPAddr(), cmReq.getPort());
					resp = new Communication(Protocol.CM_HANDSHAKE_RESPONSE, new CMHandshakeResponse(StatusCode.OK, id, MasterServer.BLOCK_SIZE));
					//TODO: Log success
				} break;
				
				case CM_OPERATION_REQUEST:
				{
					CMOperationRequest cmReq = (CMOperationRequest)req.getPayload();
					File file = null;
					Directory dir = null;
					StatusCode status = StatusCode.DENIED;
					String addr = null;
					int port = -1;
					/**
					 *	Use the master server to handle all the different operations.
					 *  Only use master server as needed, as synchronization will be
					 *  required for some operations.
					 */
					switch (cmReq.getFileOperation())
					{
						case CREATE:
							if((file = MasterServer.createFile(cmReq.getFilename())) != null)
							{
								status = StatusCode.OK;
								addr = MasterServer.getStorIPAddress(file.getStorId());
								port = MasterServer.getStorClientPort(file.getStorId());
							}
							break;
						case MKDIR:
							if((dir = MasterServer.makeDirectory(cmReq.getFilename())) != null)
								status = StatusCode.OK;
							break;
						case OPEN:
							if ((file = MasterServer.getFile(cmReq.getFilename())) != null)
							{
								status = StatusCode.OK;
								addr = MasterServer.getStorIPAddress(file.getStorId());
								port = MasterServer.getStorClientPort(file.getStorId());								
							}
							break;
						case CLOSE:
							//TODO
							break;
						case REMOVE:
							if (MasterServer.removeFile(cmReq.getFilename()))
								status = StatusCode.OK;
							break;
						case RMDIR:
							if (MasterServer.removeDirectory(cmReq.getFilename()))
								status = StatusCode.OK;
							break;
						case NO_OP:
						default:
							//TODO: Log no operation
							break;
					}
					
					resp = new Communication(Protocol.CM_OPERATION_RESPONSE, new CMOperationResponse(status, addr, port));
					
				} break;
				
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