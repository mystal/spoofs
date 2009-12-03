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
import cs5204.fs.lib.Worker;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Filesystem
{	
	private static String _masterAddr;
	private static int _masterPort;
	private static int _blockSize;
    private static int _id;
    private static String _ipAddr;
	private static final int _port = 3009;
	private static boolean _connected;
	private static Worker _worker;
	
	//TODO: Caching of SFile handles

    public static void Connect(String addr, int port) throws Exception
    {
		Communication comm = null;
		CMHandshakeResponse resp = null;
		
		_worker = new Worker();
		
		_masterAddr = addr;
		_masterPort = port;
		
        try {
            _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }
		
		comm = _worker.submitRequest(
								new Communication(
									Protocol.CM_HANDSHAKE_REQUEST, 
									new CMHandshakeRequest(_ipAddr, _port)), 
								_masterAddr, 
								_masterPort);
		
		if (comm == null)
			//TODO: throw exception
			
		resp = (CMHandshakeResponse)comm.getPayload();
		
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
		
		_connected = true;
		
		//TODO: Start thread on _port that listens for failover requests from backup
    }
	
	public static void Disconnect()
	{
		//TODO:  Clean up exec pool
		//TODO:  Close open handles
		//TODO:  Stop daemon thread
	}

    public static SFile createFile(String filepath)
    {
		if (!_connected)
			return null;
		
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
		if (!_connected)
			return false;
		
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
        if (!_connected)
			return null;
		
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
		if (!_connected)
			return false;
		//TODO:  Close with master???
		file.setOpened(false);
        return true;
    }

    public static boolean removeFile(String filepath)
    {
		if (!_connected)
			return false;
		
		SFile file = null;
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.REMOVE, filepath, _id));
		
		switch (masterResp.getStatus())
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

    public static boolean removeDirectory(String dirpath)
    {
		if (!_connected)
			return false;
		//TODO
        return false;
    }

    //TODO: get attribute?
	
	//For use with SFile objects
	
	public static boolean append(SFile file, byte[] data)
	{
		if (!_connected)
			return false;
		
		CSOperationResponse csResp = sendStorageOperationRequest(
											new CSOperationRequest(_id, FileOperation.APPEND, file.getPath(), data, -1, data.length),
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
	
	public static boolean read(SFile file, byte [] data, int off, int len)
	{
		if (!_connected)
			return false;
		
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
		if (!_connected)
			return false;
		
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
		Communication resp = _worker.submitRequest(new Communication(Protocol.CM_OPERATION_REQUEST, masterReq), _masterAddr, _masterPort);
		
		if (resp == null)
			return null;
		
		return (CMOperationResponse)resp.getPayload();
	}
	
	private static CSOperationResponse sendStorageOperationRequest(CSOperationRequest storageReq, String ipAddr, int port)
	{
		Communication resp = _worker.submitRequest(new Communication(Protocol.CS_OPERATION_REQUEST, storageReq), ipAddr, port);
		
		if (resp == null)
			return null;
		
		return (CSOperationResponse)resp.getPayload();
	}
}
