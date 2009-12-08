package cs5204.fs.master;

import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.BackupOperation;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.lib.Node;
import cs5204.fs.lib.BackupObject;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MSHandshakeRequest;
import cs5204.fs.rpc.MSHandshakeResponse;
import cs5204.fs.rpc.CMHandshakeRequest;
import cs5204.fs.rpc.CMHandshakeResponse;
import cs5204.fs.rpc.CMOperationRequest;
import cs5204.fs.rpc.CMOperationResponse;
import cs5204.fs.rpc.CMFootshakeRequest;
import cs5204.fs.rpc.CMFootshakeResponse;
import cs5204.fs.rpc.MBHandshakeRequest;
import cs5204.fs.rpc.MBHandshakeResponse;

import java.net.Socket;

import java.util.ArrayList;

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
					int id = -1;
					StatusCode status = StatusCode.DENIED;
					
					if ((id = MasterServer.addClientNode(cmReq.getIPAddr(), cmReq.getPort())) != -1)
						status = StatusCode.OK;
					
					resp = new Communication(Protocol.CM_HANDSHAKE_RESPONSE, new CMHandshakeResponse(StatusCode.OK, id, MasterServer.BLOCK_SIZE));
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
								port = MasterServer.getStorPort(file.getStorId());
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
								port = MasterServer.getStorPort(file.getStorId());								
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
				
				case CM_FOOTSHAKE_REQUEST:
				{
					CMFootshakeRequest cmReq = (CMFootshakeRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					if (MasterServer.removeClientNode(cmReq.getId()))
						status = StatusCode.OK;
					resp = new Communication(Protocol.CM_FOOTSHAKE_RESPONSE, new CMFootshakeResponse(status));
				}
				
				case MS_HANDSHAKE_REQUEST:
				{
					MSHandshakeRequest msReq = (MSHandshakeRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					int id = -1;
					String addr = msReq.getIPAddr();
					int port = msReq.getPort();
					
					if ((id = MasterServer.addStorageNode(addr, port)) != -1)
						status = StatusCode.OK;
					
					resp = new Communication(Protocol.MS_HANDSHAKE_RESPONSE, new MSHandshakeResponse(status, id, MasterServer.getKAPort()));
					
					//TODO: Log
				} break;
				
				case MB_HANDSHAKE_REQUEST:
				{
					StringBuilder builder = new StringBuilder();
					builder.append("MBHandshakeRequest detected...\n");
					MBHandshakeRequest mbReq = (MBHandshakeRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					String addr = mbReq.getAddress();
					int port = mbReq.getPort();
					ArrayList<Node> nodes = MasterServer.getCurrentNodes();
					BackupObject [] objects = new BackupObject[nodes.size()];
					for (int i = 0 ; i < nodes.size() ; i++)
						objects[i] = new BackupObject(BackupOperation.ADD, nodes.get(i));
					
					if (MasterServer.addBackupNode(addr, port))
					{
						status = StatusCode.OK;
						builder.append("Backup node added successfully.");
					}
					else
					{
						builder.append("Backup node failed to add.");
					}
					
					resp = new Communication(Protocol.MB_HANDSHAKE_RESPONSE, new MBHandshakeResponse(status, MasterServer.getKAPort(), objects));
					
					MasterServer.info(builder.toString());
				} break;

				default:
					break;
			}
			
			return resp;
		}
	}
}
