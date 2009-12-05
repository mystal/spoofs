package cs5204.fs.master;

import cs5204.fs.lib.Worker;
import cs5204.fs.lib.KeepAliveClient;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MBHandshakeRequest;
import cs5204.fs.rpc.MBHandshakeResponse;
import cs5204.fs.common.StatusCode;
import cs5204.fs.common.NodeType;
import cs5204.fs.common.Protocol;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.concurrent.ConcurrentHashMap;

public class MasterBackup
{
	private static final int DEFAULT_MASTER_BACKUP_PORT = 3059;
	private static final int MAX_ATTEMPTS = 5;

    private static String _ipAddr;
    private static int _id;
    private static String _masterAddr;
    private static int _masterBackupPort;
    private static int _masterKeepAlivePort;

	private static ConcurrentHashMap<Integer, StorageNode> _storMap;
	private static ConcurrentHashMap<Integer, ClientNode> _clientMap;

    private static Worker _worker;

    public static void initialize(String addr, int backupPort)
    {
        //TODO: setup internal things, handshake with master, start KA server and backup request handler

        _masterAddr = addr;
        _masterBackupPort = backupPort;

        try {
		    _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }

		_storMap = new ConcurrentHashMap<Integer, StorageNode>();
		_clientMap = new ConcurrentHashMap<Integer, ClientNode>();

        _worker = new Worker();

		int attempts = 0;
		while (!initiateContact())
		{
			//TODO: Log failure
			if(++attempts > MAX_ATTEMPTS)
			{
				//TODO: Log max rety reached
                return;
			}
		}
		
		//TODO: Log successful connection

		Thread kaClient = new Thread(new KeepAliveClient(NodeType.BACKUP, _id, _masterAddr, _masterKeepAlivePort, _worker));
		Thread backupHandler = new Thread(new BackupHandler(DEFAULT_MASTER_BACKUP_PORT));

		kaClient.start();
		backupHandler.start();

        //TODO: Log started
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
								_masterBackupPort);
		
		if (resp == null)
			return false;
			
		MBHandshakeResponse mbResp = (MBHandshakeResponse)resp.getPayload();
		switch(mbResp.getStatus())
		{
			case OK:
				_id = mbResp.getId();
				_masterKeepAlivePort = mbResp.getKAPort();
				break;
			case DENIED:
			default:
				return false;
		}
		
		return true;
	}
	
	private static class StorageNode
	{
		private String m_addr;
		private int m_clientPort;
		private int m_masterPort;

		public StorageNode(String addr, int clientPort, int masterPort)
		{
			m_addr = addr;
            m_clientPort = clientPort;
			m_masterPort = masterPort;
		}
		
		public String getAddress()
		{
			return m_addr;
		}
		
		public int getClientPort()
		{
			return m_clientPort;
		}
		
		public int getMasterPort()
		{
			return m_masterPort;
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
	}
}
