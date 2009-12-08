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
import cs5204.fs.rpc.MSRecoveryResponse;
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
			StringBuilder msg = new StringBuilder();
			
			switch (req.getProtocol())
			{
				case CM_HANDSHAKE_REQUEST:
				{
					CMHandshakeRequest cmReq = (CMHandshakeRequest)req.getPayload();
					int id = -1;
					StatusCode status = StatusCode.DENIED;
					
					msg.append("Client handshake detected.\n");
					
					if ((id = MasterServer.addClientNode(cmReq.getIPAddr(), cmReq.getPort())) != -1)
					{
						status = StatusCode.OK;
						msg.append("Client successfully added with id " + id);
					}
					
					resp = new Communication(Protocol.CM_HANDSHAKE_RESPONSE, new CMHandshakeResponse(StatusCode.OK, id, MasterServer.BLOCK_SIZE));
					
					MasterServer.info(msg.toString());
				} break;
				
				case CM_OPERATION_REQUEST:
				{
					CMOperationRequest cmReq = (CMOperationRequest)req.getPayload();
					File file = null;
					Directory dir = null;
					StatusCode status = StatusCode.DENIED;
					String addr = null;
					int port = -1;
					
					msg.append("Client operation request detected from client " + cmReq.getId() + "\n");
					
					/**
					 *	Use the master server to handle all the different operations.
					 *  Only use master server as needed, as synchronization will be
					 *  required for some operations.
					 */
					switch (cmReq.getFileOperation())
					{
						case CREATE:
							msg.append("CREATE: ");
							if((file = MasterServer.createFile(cmReq.getFilename())) != null)
							{
								status = StatusCode.OK;
								addr = MasterServer.getStorIPAddress(file.getStorId());
								port = MasterServer.getStorPort(file.getStorId());
								msg.append("Success.\n");
								msg.append(cmReq.getFilename() + " located at " + addr + ":" + port);
							}
							break;
						case MKDIR:
							msg.append("MKDIR: ");
							if((dir = MasterServer.makeDirectory(cmReq.getFilename())) != null)
							{
								status = StatusCode.OK;
								msg.append("Success.\n");
								msg.append("Created directory " + cmReq.getFilename());
							}
							break;
						case OPEN:
							msg.append("OPEN: ");
							if ((file = MasterServer.getFile(cmReq.getFilename())) != null)
							{
								status = StatusCode.OK;
								addr = MasterServer.getStorIPAddress(file.getStorId());
								port = MasterServer.getStorPort(file.getStorId());
								msg.append("Success.\n");
								msg.append(cmReq.getFilename() + " located at " + addr + ":" + port);
							}
							break;
						case CLOSE:
							msg.append("CLOSE: Not yet implemented");
							break;
						case REMOVE:
							msg.append("REMOVE: ");
							if (MasterServer.removeFile(cmReq.getFilename()))
							{
								status = StatusCode.OK;
								msg.append("Success.\n");
								msg.append(cmReq.getFilename() + " sucessfully removed");
							}
							break;
						case RMDIR:
							msg.append("RMDIR: ");
							if (MasterServer.removeDirectory(cmReq.getFilename()))
							{
								status = StatusCode.OK;
								msg.append("Success.\n");
								msg.append(cmReq.getFilename() + " sucessfully removed");
							}
							break;
						case NO_OP:
						default:
							msg.append("NO VALID OPERATION DETECTED!");
							break;
					}
					
					resp = new Communication(Protocol.CM_OPERATION_RESPONSE, new CMOperationResponse(status, addr, port));
					
				} break;
				
				case CM_FOOTSHAKE_REQUEST:
				{
					msg.append("Client disconnect request received...\n");
					CMFootshakeRequest cmReq = (CMFootshakeRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					if (MasterServer.removeClientNode(cmReq.getId()))
					{
						status = StatusCode.OK;
						msg.append("Successfully removed client with id " + cmReq.getId());
					}
					resp = new Communication(Protocol.CM_FOOTSHAKE_RESPONSE, new CMFootshakeResponse(status));
				}
				
				case MS_HANDSHAKE_REQUEST:
				{
					msg.append("Server handshake request detected...\n");
					MSHandshakeRequest msReq = (MSHandshakeRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					int id = -1;
					String addr = msReq.getIPAddr();
					int port = msReq.getPort();
					
					if ((id = MasterServer.addStorageNode(addr, port)) != -1)
					{
						status = StatusCode.OK;
						msg.append("Storage server at " + addr + ":" + port + " successfully added with id " + id);
					}
					
					resp = new Communication(Protocol.MS_HANDSHAKE_RESPONSE, new MSHandshakeResponse(status, id, MasterServer.getKAPort()));
				} break;
				
				case MB_HANDSHAKE_REQUEST:
				{
					msg.append("MBHandshakeRequest detected...\n");
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
						msg.append("Backup node added successfully.");
					}
					else
					{
						msg.append("Backup node failed to add.");
					}
					
					resp = new Communication(Protocol.MB_HANDSHAKE_RESPONSE, new MBHandshakeResponse(status, MasterServer.getKAPort(), objects));
				} break;
				
				case MS_RECOVERY_RESPONSE:
				{
					msg.append("Server recovery response detected...");
					MSRecoveryResponse msResp = (MSRecoveryResponse)req.getPayload();
					MasterServer.submitRecovery(msResp.getId(), msResp.getFilenames());					
					
					//DO NOT send anything back...
					resp = null;
				}

				default:
					break;
			}
			
			MasterServer.info(msg.toString());
			
			return resp;
		}
	}
}
