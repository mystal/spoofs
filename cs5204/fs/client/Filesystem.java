package cs5204.fs.client;

import cs5204.fs.common.StatusCode;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.FileOperation;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.CMHandshakeRequest;
import cs5204.fs.rpc.CMHandshakeResponse;
import cs5204.fs.rpc.CMOperationRequest;
import cs5204.fs.rpc.CMOperationResponse;
import cs5204.fs.rpc.CSOperationRequest;
import cs5204.fs.rpc.CSOperationResponse;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Filesystem
{
	private static final int CORE_POOL_SIZE = 10;
	private static final int MAX_POOL_SIZE = 100;
	private static final long KEEP_ALIVE_TIME = 100;
	
	private static SocketAddress _masterAddr;
	private static int _blockSize;
    private static int _id;
    private static String _ipAddr;
	private static final int _port = 3009;
	private static ExecutorService _exec;
	private static AtomicInteger _counter;
	
	//TODO: Caching of SFile handles

    public static void init(String addr, int port) throws Exception
    {
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		Communication comm = null;
		CMHandshakeRequest req = null;
		CMHandshakeResponse resp = null;
        Socket socket = null;
		
		_masterAddr = new InetSocketAddress(addr, port);
		
		_exec = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
					KEEP_ALIVE_TIME, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE));
					
		_counter = new AtomicInteger(0);
		
        try {
            _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }
		
        try {
            socket = new Socket();
            socket.connect(_masterAddr);
        }
        catch (IOException ex) {
            //TODO: Log/fail
        }
		
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			req = new CMHandshakeRequest(_ipAddr, _port);
			comm = new Communication(Protocol.CM_HANDSHAKE_REQUEST, req);
			oos.writeObject(comm);
            oos.flush();
		}
		catch (IOException ex) {
			//TODO: Log/fail
		}
		
		try {
			ois = new ObjectInputStream(socket.getInputStream());
			comm = (Communication)ois.readObject();
			resp = (CMHandshakeResponse)comm.getPayload();
		}
		catch (IOException ex) {
			//TODO: Log/fail
		}
		catch (ClassNotFoundException ex) {
			//TODO: Log/fail
		}
		
		switch (resp.getStatus())
		{
			case OK:
				_id = resp.getId();
				_blockSize = resp.getBlockSize();
				//TODO: Log success
				break;
			case DENIED:
			default:
				//TODO: Throw exception
                break;
		}
		
        try {
            oos.close();
            ois.close();
			socket.close();
        }
		catch (IOException ex) {
			//TODO: Log/fail
		}
		
		//TODO: Start thread on _port that listens for failover requests from backup
    }

    public static SFile createFile(String filepath)
    {
		SFile file = null;
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.CREATE, filepath, _id));
		
		switch (masterResp.getStatus())
		{
			case OK:
				file = new SFile(filepath, masterResp.getAddress(), masterResp.getPort(), true);
				break;
			case DENIED:
			default:
				//TODO: Log/fail
		}
		
        return file;
    }

    public static boolean createDirectory(String dirpath)
    {
		boolean success = false;
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.MKDIR, dirpath, _id));
		
		switch (masterResp.getStatus())
		{
			case OK:
				success = true;
				break;
			case DENIED:
			default:
				success = false;
		}
		
        return success;
    }

    public static SFile open(String filepath)
    {
        SFile file = null;
		
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.OPEN, filepath, _id));
		
		switch (masterResp.getStatus())
		{
			case OK:
				file = new SFile(filepath, masterResp.getAddress(), masterResp.getPort(), true);
				break;
			case DENIED:
			default:
				//TODO: Log/fail
		}
		
        return file;
    }

    public static boolean close(SFile file)
    {
        return false;
    }

    public static boolean removeFile(String filepath)
    {
        return false;
    }

    public static boolean removeDirectory(String dirpath)
    {
        return false;
    }

    //TODO: get attribute?
	
	//For use with SFile objects
	
	public static boolean append(SFile file, byte[] data)
	{
		/*CMOperationResponse masterResp = null;
		CSOperationResponse storageResp = null;
		String addr = null;
		int port = -1;
		
		if (!file.isOpened())
			return false;
		
		addr = file.getAddress();
		port = file.getPort();
		
		storageResp = sendStorageOperationRequest(new CSOperationRequest(_id, FileOperation.APPEND, file.getPath(), -1, data), addr, port);
		
		switch (storageResp.getStatus())
		{
			case OK:
				//TODO
				break;
			case DENIED:
			default:
				//TODO/fail			
		}*/
		
		return true;
	}
	
	public static boolean read(SFile file, byte [] data, int off, int len)
	{
		CSOperationResponse csResp = sendStorageOperationRequest(
											new CSOperationRequest(_id, FileOperation.READ, file.getPath(), null, off, len),
											file.getAddress(),
											file.getPort());
		switch (csResp.getStatus())
		{
			case OK:
				byte[] reply = csResp.getData();
				for (int i = 0 ; i < len ; i++)
					data[i] = reply[i];
				break;
			case DENIED:
			default:
				//TODO: Log
				return false;
		}
		
		return true;
	}
	
	public static boolean write(SFile file, byte [] data, int off, int len)
	{
		CSOperationResponse csResp = sendStorageOperationRequest(
											new CSOperationRequest(_id, FileOperation.WRITE, file.getPath(), data, off, len),
											file.getAddress(),
											file.getPort());
		switch (csResp.getStatus())
		{
			case OK:
				//Nothing?
				break;
			case DENIED:
			default:
				//TODO: Log
				return false;
		}
		
		return true;
	}
	
	private static CMOperationResponse sendMasterOperationRequest(CMOperationRequest masterReq)
	{
		int count = _counter.getAndIncrement();
		Communication req = null;
		Communication resp = null;
		ClientTask task = null;
		
		req = new Communication(Protocol.CM_OPERATION_REQUEST, masterReq);
		
		try {
			//Submit a ClientTask, get result of a future which will return null on completion
			//...the wonders of java.util.concurrent
			task = new ClientTask(_masterAddr, req, count);
			_exec.submit(task).get();
			resp = task.getResponse();
		}
		catch (InterruptedException ex) {
			//TODO: Log/fail
		}
		catch (ExecutionException ex) {
			//TODO: Log/fail
		}
		
		return (CMOperationResponse)resp.getPayload();
	}
	
	private static CSOperationResponse sendStorageOperationRequest(CSOperationRequest storageReq, String ipAddr, int port)
	{
		int count = _counter.getAndIncrement();
		Communication req = null;
		Communication resp = null;
		ClientTask task = null;
		
		req = new Communication(Protocol.CS_OPERATION_REQUEST, storageReq);
		
		try {
			//Submit a ClientTask, get result of a future which will return null on completion
			//...the wonders of java.util.concurrent
			task = new ClientTask(new InetSocketAddress(ipAddr, port), req, count);
			_exec.submit(task).get();
			resp = task.getResponse();
		}
		catch (InterruptedException ex) {
			//TODO: Log/fail
		}
		catch (ExecutionException ex) {
			//TODO: Log/fail
		}
		
		return (CSOperationResponse)resp.getPayload();
	}
	
	private static class ClientTask implements Runnable
	{
		private SocketAddress m_address;
		private Communication m_req;
		private Communication m_resp;
		private int m_id;
		
		public ClientTask(SocketAddress addr, Communication req, int id)
		{
			m_address = addr;
			m_req = req;
			m_resp = null;
			m_id = id;
		}
		
		public void run()
		{
			Socket sock = null;
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			
			try {
				sock = new Socket();
				sock.connect(m_address);
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
			
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject(m_req);
				oos.flush();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			
			try {
				ois = new ObjectInputStream(sock.getInputStream());
				m_resp = (Communication)ois.readObject();
			}
			catch (IOException ex) {
				//TODO: log/fail
			}
			catch (ClassNotFoundException ex) {
				//TODO: Log/fail
			}
			
			try {
				oos.close();
				ois.close();
				sock.close();
			}
			catch (IOException ex) {
				//TODO: Log/fail
			}
		}
		
		public Communication getResponse()
		{
			return m_resp;
		}
	}
}
