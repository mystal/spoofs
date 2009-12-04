package cs5204.fs.master;

import cs5204.fs.lib.Worker;
import cs5204.fs.common.NodeType;

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

    public static void initialize(String addr, int backupPort, int kaPort)
    {
        //TODO: setup internal things, handshake with master, start KA server and backup request handler

        _masterAddr = addr;
        _masterBackupPort = backupPort;
        _masterKeepAlivePort = kaPort;

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
								_masterPort);
		
		if (resp == null)
			return false;
			
		MBHandshakeResponse mbResp = (MBHandshakeResponse)resp.getPayload();
		switch(mbResp.getStatus())
		{
			case OK:
				_id = mbResp.getId();
				break;
			case DENIED:
			default:
				return false;
		}
		
		return true;
	}
}
