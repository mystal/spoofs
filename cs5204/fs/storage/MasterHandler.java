package cs5204.fs.storage;

import cs5204.fs.lib.AbstractHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.net.ServerSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

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
		
		public void run()
		{
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
		}
	}
}
