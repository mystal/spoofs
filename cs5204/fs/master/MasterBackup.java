package cs5204.fs.master;

import cs5204.fs.common.StatusCode;
import cs5204.fs.common.NodeType;
import cs5204.fs.common.Protocol;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.KeepAliveClient;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MBHandshakeRequest;
import cs5204.fs.rpc.MBHandshakeResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class MasterBackup
{
	private static final int DEFAULT_MASTER_BACKUP_PORT = 3059;
	private static final int MAX_ATTEMPTS = 5;

    private static String _ipAddr;
    private static String _masterAddr;
    private static int _masterMainPort;
    private static int _masterKeepAlivePort;

	private static ConcurrentHashMap<Integer, StorageNode> _storMap;
	private static ConcurrentHashMap<Integer, ClientNode> _clientMap;

    private static Worker _worker;

    private static Logger _log;

    public static void initialize(String addr, int mainPort)
    {
		setupLogging();

        _log.info("Setting up master backup...");

        _masterAddr = addr;
        _masterMainPort = mainPort;

        try {
		    _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }

		_storMap = new ConcurrentHashMap<Integer, StorageNode>();
		_clientMap = new ConcurrentHashMap<Integer, ClientNode>();

        _worker = new Worker();

        _log.info("Connecting to master...");

		int attempts = 0;
		while (!initiateContact())
		{
			_log.warning("Failed to connect to master, attempt " + (attempts+1) + "/" + MAX_ATTEMPTS+ "...");
			if(++attempts >= MAX_ATTEMPTS)
			{
                _log.severe("Could not connect to master!");
                return;
			}
		}
		
        _log.info("Successfully connected to master, starting keep alives and backup handler...");

		Thread backupHandler = new Thread(new BackupHandler(DEFAULT_MASTER_BACKUP_PORT));
		Thread kaClient = new Thread(new KeepAliveClient(NodeType.BACKUP, -1, _masterAddr, _masterKeepAlivePort, _worker));

		kaClient.start();
		backupHandler.start();

		_log.info("Ready to accept requests...\n");
    }

	private static boolean initiateContact()
	{
		Communication resp = _worker.submitRequest(
								new Communication(
									Protocol.MB_HANDSHAKE_REQUEST,
									new MBHandshakeRequest(
										_ipAddr, 
										DEFAULT_MASTER_BACKUP_PORT)),
								_masterAddr,
								_masterMainPort);
		
		if (resp == null)
			return false;
			
		MBHandshakeResponse mbResp = (MBHandshakeResponse)resp.getPayload();
		switch(mbResp.getStatus())
		{
			case OK:
				_masterKeepAlivePort = mbResp.getKAPort();
				break;
			case DENIED:
			default:
				return false;
		}
		
		return true;
	}

    private static void setupLogging()
    {
		_log = Logger.getLogger("cs5204.fs.master");
    }
	
    public static boolean backupStorageNode(String ipAddr, int port, int id)
    {
        _storMap.put(id, new StorageNode(ipAddr, port));
        //TODO: if put fails, return false?
        _log.info("Backed up a new storage node.");
        return true;
    }
	
	public static boolean backupClientNode(String ipAddr, int port, int id)
	{
		_clientMap.put(id, new ClientNode(ipAddr, port));
        //TODO: if put fails, return false?
        _log.info("Backed up a new client node.");
        return true;
	}

	private static class StorageNode
	{
		private String m_addr;
		private int m_port;

		public StorageNode(String addr, int port)
		{
			m_addr = addr;
            m_port = port;
		}
		
		public String getAddress()
		{
			return m_addr;
		}
		
		public int getPort()
		{
			return m_port;
		}
	}
	
	private static class ClientNode
	{
		private String m_addr;
		private int m_port;

		public ClientNode(String addr, int port)
		{
			m_addr = addr;
            m_port = port;
		}
		
		public String getAddress()
		{
			return m_addr;
		}
		
		public int getPort()
		{
			return m_port;
		}
	}
}
