package cs5204.fs.master;

import cs5204.fs.common.StatusCode;
import cs5204.fs.lib.AbstractHandler;
import cs5204.fs.rpc.MSRequest;
import cs5204.fs.rpc.MSResponse;

import java.net.Socket;
import java.net.ServerSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class StorageHandler extends AbstractHandler
{
	public StorageHandler(int port)
	{
        super(port);
	}
	
    protected AbstractHandlerTask createHandlerTask(Socket socket, int id)
    {
        return new StorageHandlerTask(socket, id);
    }

	private class StorageHandlerTask extends AbstractHandlerTask
	{
		public StorageHandlerTask(Socket socket, int id)
		{
            super(socket, id);
		}
		
		public void run()
		{
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			MSRequest req = null;
			MSResponse resp = null;
			

			try {
				ois = new ObjectInputStream(m_mySocket.getInputStream());
				req = (MSRequest)ois.readObject();
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
			catch (ClassNotFoundException ex) {
				//TODO: Log/fail
			}
			
			resp = processRequest(req);
				
			try {
				oos = new ObjectOutputStream(m_mySocket.getOutputStream());
				oos.writeObject(resp);
				oos.flush();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			
			try {
                oos.close();
                ois.close();
				m_mySocket.close();
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
		}
		
		private MSResponse processRequest(MSRequest req)
		{
			MSResponse resp = null;
			StatusCode status = StatusCode.DENIED;
			int id = -1;
			
			String addr = req.getIPAddr();
			int port = req.getPort();
			
            id = MasterServer.addStorageNode(addr, port);
			if (id != -1)
			{
				status = StatusCode.OK;
			}
			
			resp = new MSResponse(status, id);
			
			return resp;
		}
	}
}
