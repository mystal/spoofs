package cs5204.fs.client;

import cs5204.fs.common.StatusCode;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.FileOperation;
import cs5204.fs.common.NodeType;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.KeepAliveClient;
import cs5204.fs.lib.Node;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.CMHandshakeRequest;
import cs5204.fs.rpc.CMHandshakeResponse;
import cs5204.fs.rpc.CMFootshakeRequest;
import cs5204.fs.rpc.CMFootshakeResponse;
import cs5204.fs.rpc.CMOperationRequest;
import cs5204.fs.rpc.CMOperationResponse;
import cs5204.fs.rpc.CSOperationRequest;
import cs5204.fs.rpc.CSOperationResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class SClient
{
	private static final int DEFAULT_MASTER_KA_TIMEOUT = 3000;
	
	private static ConcurrentHashMap<Integer, SClient> _clientMap = new ConcurrentHashMap<Integer, SClient>();

    //Information about the master
	private String m_masterAddr;
	private int m_masterPort;
	//private int m_blockSize;

    //Information about the client
	private boolean m_connected;
	private int m_handlerPort;
    private int m_id;
    private String m_ipAddr;

	//Caching of SFile handles
    private HashMap<String,SFile> m_fileMap;

	private Worker m_worker;
	private KeepAliveClient m_kaClient;
	private Thread m_kaThread;
	private MasterHandler m_masterHandler;
	private Thread m_masterThread;

	private static Logger m_log;
	
    public SClient(String addr, int port, int handlerPort)
    {
        m_masterAddr = addr;
        m_masterPort = port;
		m_handlerPort = handlerPort;

        m_fileMap = new HashMap<String,SFile>();

        m_worker = new Worker();
        m_log = Logger.getLogger("cs5204.fs.client");
    }

    public boolean connect()
    {
		Communication comm = null;
		CMHandshakeResponse resp = null;
		
        try {
            m_ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }
		
		m_log.info("Done setting up parameters.");
		
		m_log.info("Submitting handshake to master at " + m_masterAddr + ":" + m_masterPort + "...");
		
		comm = m_worker.submitRequest(
								new Communication(
									Protocol.CM_HANDSHAKE_REQUEST, 
									new CMHandshakeRequest(m_ipAddr, m_handlerPort)), 
								m_masterAddr,
								m_masterPort);
		
		if (comm == null)
		{
			m_log.warning("NO COMMUNICATION RECEIVED FROM MASTER!");
			return false;
		}
			
		resp = (CMHandshakeResponse)comm.getPayload();
		
		switch (resp.getStatus())
		{
			case OK:
				m_id = resp.getId();
				//m_blockSize = resp.getBlockSize();
				m_connected = true;
				m_log.info("Successful handshake!  Now connected with id " + m_id);
				break;
			case DENIED:
			default:
				m_log.warning("Request denied");
				return false;
		}
		
		_clientMap.put(m_handlerPort, this);
		
		//Start KA client
		startKA(m_handlerPort);
		
		//Start thread on _port that listens for failover requests from backup
		m_masterHandler = new MasterHandler(m_handlerPort);
		m_masterThread = new Thread(m_masterHandler);
		m_masterThread.start();
        
        return true;
    }

    public void disconnect()
    {
		//Unregister from master
		Communication resp = m_worker.submitRequest(
										new Communication(
											Protocol.CM_FOOTSHAKE_REQUEST,
											new CMFootshakeRequest(m_id)),
										m_masterAddr,
										m_masterPort);
		
		//Clean up exec pool
		m_worker.shutdown();
		
		//Stop daemon threads
		stopKA(m_handlerPort);
		stopMasterHandler(m_handlerPort);
		
		_clientMap.remove(m_handlerPort);

        for (String filepath: m_fileMap.keySet())
            m_fileMap.get(filepath).setOpened(false);
        m_fileMap = new HashMap<String,SFile>();
    }
	
	public static void startKA(int handlerPort)
	{
		SClient cli = _clientMap.get(handlerPort);
		if (cli == null) return;
		cli.m_kaClient = new KeepAliveClient(NodeType.CLIENT, cli.m_id, cli.m_masterAddr, cli.m_masterPort, DEFAULT_MASTER_KA_TIMEOUT);
		cli.m_kaThread = new Thread(cli.m_kaClient);
		cli.m_kaThread.start();
	}
	
	public static void stopKA(int handlerPort)
	{
		SClient cli = _clientMap.get(handlerPort);
		if (cli == null) return;
		cli.m_log.info("Attempting to stop the KA Thread");
		cli.m_kaClient.stop();
		try {
			cli.m_kaThread.join();
		}
		catch (InterruptedException ex) {
			//TODO: repeat?
		}
		cli.m_log.info("KA Thread stopped");
	}
	
	public static void stopMasterHandler(int handlerPort)
	{
		SClient cli = _clientMap.get(handlerPort);
		if (cli == null) return;
		cli.m_log.info("Attempting to stop MasterHandler");
		cli.m_masterHandler.stop();
		try {
			cli.m_masterThread.join();
		}
		catch (InterruptedException ex) {
			//TODO: ?
		}
		cli.m_log.info("MasterHandler thread stopped");
	}
	
	public static boolean verifyBackup(int handlerPort, Node node)
	{
		SClient cli = _clientMap.get(handlerPort);
		if (node == null || cli == null) return false;
		return node.getNodeType() == NodeType.CLIENT &&
			node.getId() == cli.m_id &&
			node.getAddress().equals(cli.m_ipAddr) &&
			node.getPort() == cli.m_handlerPort;
	}
	
	public static boolean setMaster(int handlerPort, String address, int port)
	{
		SClient cli = _clientMap.get(handlerPort);
		if (cli == null) return false;
		cli.m_masterAddr = address;
		cli.m_masterPort = port;
		return true;
	}

    public SFile createFile(String filepath)
    {
		if (!m_connected)
			return null;
		
		SFile file = null;
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.CREATE, filepath, m_id));
		
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

    public boolean createDirectory(String dirpath)
    {
		if (!m_connected)
			return false;
		
		boolean success = false;
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.MKDIR, dirpath, m_id));
		
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

    public SFile open(String filepath)
    {
        if (!m_connected)
			return null;
		
		SFile file = null;
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.OPEN, filepath, m_id));
		
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

    public boolean close(SFile file)
    {
		if (!m_connected)
			return false;
		//TODO:  Close with master???
		file.setOpened(false);
        return true;
    }

    public boolean removeFile(String filepath)
    {
		if (!m_connected)
			return false;
		
		SFile file = null;
		CMOperationResponse masterResp = sendMasterOperationRequest(new CMOperationRequest(FileOperation.REMOVE, filepath, m_id));
		
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

    public boolean removeDirectory(String dirpath)
    {
		if (!m_connected)
			return false;
		//TODO: do the actual remove operation
        return false;
    }

    //TODO: get attribute?
	
	//For use with SFile objects
	
	public boolean append(SFile file, byte[] data)
	{
		if (!m_connected)
			return false;
		
		CSOperationResponse csResp = sendStorageOperationRequest(
											new CSOperationRequest(m_id, FileOperation.APPEND, file.getPath(), data, -1, data.length),
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
	
	public boolean read(SFile file, byte [] data, int off, int len)
	{
		if (!m_connected)
			return false;
		
		CSOperationResponse csResp = sendStorageOperationRequest(
											new CSOperationRequest(m_id, FileOperation.READ, file.getPath(), null, off, len),
											file.getAddress(),
											file.getPort());
		switch (csResp.getStatus())
		{
			case OK:
				byte[] reply = csResp.getData();
				for (int i = 0 ; i < len ; i++)
				{
					data[i] = reply[i];
				}
				break;
			case DENIED:
			default:
				//TODO: Log
				return false;
		}
		
		return true;
	}
	
	public boolean write(SFile file, byte [] data, int off, int len)
	{
		if (!m_connected)
			return false;
		
		CSOperationResponse csResp = sendStorageOperationRequest(
											new CSOperationRequest(m_id, FileOperation.WRITE, file.getPath(), data, off, len),
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
	
	private CMOperationResponse sendMasterOperationRequest(CMOperationRequest masterReq)
	{
		Communication resp = m_worker.submitRequest(new Communication(Protocol.CM_OPERATION_REQUEST, masterReq), m_masterAddr, m_masterPort);
		
		if (resp == null)
			return null;
		
		return (CMOperationResponse)resp.getPayload();
	}
	
	private CSOperationResponse sendStorageOperationRequest(CSOperationRequest storageReq, String ipAddr, int port)
	{
		Communication resp = m_worker.submitRequest(new Communication(Protocol.CS_OPERATION_REQUEST, storageReq), ipAddr, port);
		
		if (resp == null)
			return null;
		
		return (CSOperationResponse)resp.getPayload();
	}
}

