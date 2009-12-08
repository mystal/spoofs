package cs5204.fs.master;

import cs5204.fs.common.StatusCode;
import cs5204.fs.common.NodeType;
import cs5204.fs.common.Protocol;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.KeepAliveClient;
import cs5204.fs.lib.Node;
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

	private static ConcurrentHashMap<Integer, Node> _storMap;
	private static ConcurrentHashMap<Integer, Node> _clientMap;

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

		_storMap = new ConcurrentHashMap<Integer, Node>();
		_clientMap = new ConcurrentHashMap<Integer, Node>();

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
	
	public static void addNode(Node node)
	{
		switch (node.getNodeType())
		{
			case STORAGE:
				_storMap.put(node.getId(), node);
				_log.info("Backed up a new storage node.");
				break;
			case CLIENT:
				_clientMap.put(node.getId(), node);
				_log.info("Backed up a new client node.");
				break;
			case MASTER:
			case BACKUP:
			default:
				//TODO: Log
		}
	}
	
	public static void removeNode(Node node)
	{
		switch (node.getNodeType())
		{
			case STORAGE:
				_storMap.remove(node.getId());
				_log.info("Removed a storage node.");
				break;
			case CLIENT:
				_clientMap.remove(node.getId());
				_log.info("Removed a client node.");
				break;
			case MASTER:
			case BACKUP:
			default:
				//TODO: Log
		}
	}
}
