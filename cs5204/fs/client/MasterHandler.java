package cs5204.fs.client;

import cs5204.fs.common.Protocol;
import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.CMRecoveryRequest;
import cs5204.fs.rpc.CMRecoveryResponse;

import java.net.Socket;

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
		
		protected Communication processRequest(Communication req)
		{
			Communication resp = null;
			
			switch(req.getProtocol())
			{
				case CM_RECOVERY_REQUEST:
				{
					CMRecoveryRequest cmReq = (CMRecoveryRequest)req.getPayload();
					StatusCode status = StatusCode.DENIED;
					
					if (SClient.verifyBackup(m_port, cmReq.getTargetNode()))
					{
						SClient.stopKA(m_port);
						SClient.setMaster(m_port, cmReq.getAddress(), cmReq.getPort());
						SClient.startKA(m_port);
						status = StatusCode.OK;
					}
					
					resp = new Communication(Protocol.CM_RECOVERY_RESPONSE, new CMRecoveryResponse(status));
				} break;
				
				default:
					//TODO: log
			}
			
			return resp;
		}
	}
}
