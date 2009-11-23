package cs5204.fs.master;

import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.CMOperationRequest;
import cs5204.fs.rpc.CMOperationResponse;
import cs5204.fs.rpc.CMHandshakeRequest;
import cs5204.fs.rpc.CMHandshakeResponse;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.Protocol;
import cs5204.fs.lib.AbstractHandler;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

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
			
			switch (req.getProtocol())
			{
				case CM_HANDSHAKE_REQUEST:
				{
					CMHandshakeRequest cmReq = (CMHandshakeRequest)req.getPayload();
					int id = MasterServer.addStorageNode(cmReq.getIPAddr(), cmReq.getPort());
					resp = new Communication(Protocol.CM_HANDSHAKE_RESPONSE, new CMHandshakeResponse(StatusCode.OK, id, MasterServer.BLOCK_SIZE));					
				} break;
				
				case CM_OPERATION_REQUEST:
				{
					CMOperationRequest cmReq = (CMOperationRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					String [] addrs = null;
					int [] ports = null;
					/**
					 *	Use the master server to handle all the different operations.
					 *  Only use master server as needed, as synchronization will be
					 *  required for some operations.
					 */
					switch (cmReq.getFileOperation())
					{
						case CREATE:
							//TODO
							break;
						case MKDIR:
							if(MasterServer.makeDirectory(cmReq.getFilename()))
							{
								status = StatusCode.OK;
							}
							break;
						case OPEN:
							//TODO
							break;
						case CLOSE:
							//TODO
							break;
						case READ:
						case WRITE:
						case APPEND:
							//TODO
						case REMOVE:
							//TODO
							break;
						case RMDIR:
							//TODO
							break;
						case NO_OP:
						default:
							//TODO: Log no operation
							break;
					}
					
					resp = new Communication(Protocol.CM_OPERATION_RESPONSE, new CMOperationResponse(status, addrs, ports));
					
				} break;
					
				default:
					break;
			}
			
			return resp;
		}
	}
}
