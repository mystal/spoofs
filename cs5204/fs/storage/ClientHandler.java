package cs5204.fs.storage;

import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.Communication;

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
			
			//TODO: Implement
			
			return resp;
		}
	}
}
