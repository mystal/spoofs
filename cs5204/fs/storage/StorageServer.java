package cs5204.fs.storage;

import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MSHandshakeRequest;
import cs5204.fs.rpc.MSHandshakeResponse;
import cs5204.fs.common.Protocol;
import cs5204.fs.common.NodeType;
import cs5204.fs.lib.Worker;
import cs5204.fs.lib.KeepAliveClient;
import cs5204.fs.lib.Node;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class StorageServer
{
	private static final int DEFAULT_MAIN_PORT = 2059;
	private static final int MAX_ATTEMPTS = 5;
	private static final int DEFAULT_MASTER_KA_TIMEOUT = 3000;
	
	private static int _id;
	private static String _masterAddr;
	private static int _masterPort;
	private static int _masterKAPort;
	private static Thread _kaThread;
	private static String _ipAddr;
	private static Worker _worker;
	
	private static File _rootDir;
	private static AtomicInteger _fileCounter;
	private static ConcurrentHashMap<String, StorFile> _fileMap;

    private static Logger _log;

	public static void main(String [] args)
	{
		if (args.length < 2)
		{
			System.out.println("Master IP address and port needed");
			System.exit(0);
		}

		_log = Logger.getLogger("cs5204.fs.storage");

		_log.info("Setting up storage server parameters..");
		
		//First initiate the contact with the master
		_masterAddr = args[0];
		_masterPort = Integer.parseInt(args[1]);
		_worker = new Worker();
		
		//Get cwd
		_rootDir = new File(new File(System.getProperty("user.dir")), "spoofs_files");
		_rootDir.mkdir();
		_fileCounter = new AtomicInteger(0);
		_fileMap = new ConcurrentHashMap<String, StorFile>();
		
        try {
		    _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }
		
		_log.info("Done initializing storage server.");
		
		_log.info("Initiating contact with master...");
		
		int attempts = 0;
		while (!initiateContact())
		{
			if(++attempts >= MAX_ATTEMPTS)
			{
				_log.warning("Max retry attempts reached!");
                return;
			}
			_log.warning("Failed to initiate contact, retrying...");
		}
		
		_log.info("Successful connection to master!");
		
		Thread mainHandler = new Thread(new MainHandler(DEFAULT_MAIN_PORT));
		_kaThread = new Thread(new KeepAliveClient(NodeType.STORAGE, _id, _masterAddr, _masterKAPort, DEFAULT_MASTER_KA_TIMEOUT));
		
		mainHandler.start();
		_kaThread.start();
	}
	
	private static boolean initiateContact()
	{
		Communication resp = _worker.submitRequest(
								new Communication(
									Protocol.MS_HANDSHAKE_REQUEST,
									new MSHandshakeRequest(
										_ipAddr, 
										DEFAULT_MAIN_PORT)),
								_masterAddr,
								_masterPort);
		
		if (resp == null)
			return false;
				
		MSHandshakeResponse msResp = (MSHandshakeResponse)resp.getPayload();
		switch(msResp.getStatus())
		{
			case OK:
				_id = msResp.getId();
				_masterKAPort = msResp.getKAPort();
				break;
			case DENIED:
			default:
				return false;
		}
		
		return true;
	}
	
	public static int getId()
	{
		return _id;
	}
	
	public static boolean createFile(String filename)
	{
        _log.info("Trying to create file: " + filename);
		if (_fileMap.get(filename) != null) return false;  //if it already exists
		String localName = "localfile_" + _fileCounter.getAndIncrement();
		try {
			StorFile sFile = new StorFile(localName);
			_fileMap.put(filename, sFile);
		}
		catch (FileNotFoundException ex) {
			_log.warning("File: " + filename + " failed to create");
			return false;
		}
		_log.info("File: " + filename + " successfully created as " + localName);
		return true;
	}
	
	public static boolean removeFile(String filename)
	{
        _log.info("Trying to remove file: " + filename);
		return _fileMap.remove(filename) != null;
	}
	
	public static boolean appendFile(String filename, byte[] data, int len)
	{
		_log.info("Trying to append " + len + " bytes in file: " + filename);
		StorFile file = _fileMap.get(filename);
		if (file == null) return false;
		try {
			file.append(data);
		}
		catch (IOException ex) {
			_log.warning("Could not complete append!");
			return false;
		}
        _log.info("Append complete.");
		return true;
	}
	
	public static boolean writeFile(String filename, byte[] data, int off, int len)
	{
        _log.info("Trying to write " + len + " bytes at " + off + " in file: " + filename);
		StorFile file = _fileMap.get(filename);
		if (file == null) return false;
		try {
			file.write(data, off, len);
		}
		catch (IOException ex) {
			_log.warning("Could not complete write!");
			return false;
		}
        _log.info("Write complete.");
		return true;
	}
	
	public static boolean readFile(String filename, byte[] data, int off, int len)
	{
        _log.info("Trying to read " + len + " bytes at " + off + " in file: " + filename);
		StorFile file = _fileMap.get(filename);
		if (file == null || data==null) return false;
		try {
			file.read(data, off, len);
		}
		catch (IOException ex) {
			_log.warning("Could not complete read!");
			return false;
		}
        _log.info("Read complete.");
		return true;
	}
	
	public static boolean verifyBackup(Node node)
	{
		_log.info(node.getNodeType() + " " + node.getId() + " " + node.getAddress() + " " + node.getPort());
		_log.info(NodeType.STORAGE + " " + _id + " " + _ipAddr + " " + DEFAULT_MAIN_PORT);
		return node.getNodeType() == NodeType.STORAGE &&
			node.getId() == _id &&
			node.getAddress().equals(_ipAddr) &&
			node.getPort() == DEFAULT_MAIN_PORT;
	}
	
	public static void setMaster(String addr, int port, int kaPort)
	{
        _log.info("Setting master to " + addr + " (" + port + ", " + kaPort + ").");
		_masterAddr = addr;
		_masterPort = port;
		_masterKAPort = kaPort;
	}
	
	public static String [] constructRecoveryState()
	{
		String [] filenames = new String [_fileMap.size()];
		int i = 0;
		for (String name : _fileMap.keySet())
			filenames[i++] = name;
		return filenames;
	}
	
	public static void stopKA()
	{
		_log.info("Attempting to stop the KA Thread");
		try {
			_kaThread.join();
		}
		catch (InterruptedException ex) {
			//TODO: repeat?
		}
		_log.info("KA Thread stopped");
	}
	
	public static void startKA()
	{
		_kaThread = new Thread(new KeepAliveClient(NodeType.STORAGE, _id, _masterAddr, _masterKAPort, DEFAULT_MASTER_KA_TIMEOUT));
		_kaThread.start();
	}
	
	public static void info(String msg)
	{
		_log.info(msg);
	}
	
	public static void warning(String msg)
	{
		_log.warning(msg);
	}
	
	private static class StorFile
	{
		private RandomAccessFile m_file;
		private ReentrantReadWriteLock m_readWriteLock;
		private ReentrantReadWriteLock.ReadLock m_readLock;
		private ReentrantReadWriteLock.WriteLock m_writeLock;
		
		public StorFile(String localname) throws FileNotFoundException
		{
			m_file = new RandomAccessFile(new File(_rootDir, localname), "rw");
			m_readWriteLock = new ReentrantReadWriteLock();
			m_readLock = m_readWriteLock.readLock();
			m_writeLock = m_readWriteLock.writeLock();
		}
		
		public void append(byte[] data) throws IOException
		{
			m_writeLock.lock();
			m_file.write(data, (int)m_file.length(), data.length); 
			m_writeLock.unlock();
		}
		
		public void write(byte [] data, int off, int len) throws IOException
		{
			m_writeLock.lock();
			m_file.write(data, off, len);
			m_writeLock.unlock();
		}
		
		public void read(byte [] data, int off, int len) throws IOException
		{
			m_readLock.lock();
			m_file.seek(off);
			m_file.read(data, off, len);
			m_readLock.unlock();
		}
		
		public long getLength() throws IOException
		{
			return m_file.length();
		}
	}
}
