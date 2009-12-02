package cs5204.fs.storage;

import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.MSHandshakeRequest;
import cs5204.fs.rpc.MSHandshakeResponse;
import cs5204.fs.common.Protocol;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StorageServer
{
	private static final int DEFAULT_PORT = 2059;
	private static final int MAX_ATTEMPTS = 5;
	
	private static SocketAddress _masterAddr;
	private static Socket _socket;
	private static int _id;
	private static String _ipAddr;
	
	private static File _rootDir;
	private static AtomicInteger _fileCounter;
	private static ConcurrentHashMap<String, StorFile> _fileMap;

	public static void main(String [] args)
	{
		if (args.length < 2)
		{
			System.out.println("Master IP address and port needed");
			System.exit(0);
		}
		
		//First initiate the contact with the master
		String addr = args[0];
		int port = Integer.parseInt(args[1]);
		
		//Get cwd
		_rootDir = new File(new File(System.getProperty("user.dir")), "spoofs_files");
		_rootDir.mkdir();
		_fileCounter = new AtomicInteger(0);
		_fileMap = new ConcurrentHashMap<String, StorFile>();
		
		_masterAddr = new InetSocketAddress(addr, port);
		_socket = new Socket();
        try {
		    _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }
		
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
		
		Thread clientHandler = new Thread(new ClientHandler(DEFAULT_PORT));
		
		clientHandler.start();
	}
	
	public static boolean initiateContact()
	{
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		Communication comm = null;
		MSHandshakeRequest req = null;
		MSHandshakeResponse resp = null;
		boolean success = false;
		
        try {
            establishSocketConnection();
        }
        catch (IOException ex) {
            //TODO: Log/fail
        }
		
		req = new MSHandshakeRequest(_ipAddr, DEFAULT_PORT);
		
		try {
			oos = new ObjectOutputStream(_socket.getOutputStream());
			comm = new Communication(Protocol.MS_HANDSHAKE_REQUEST, req);
			oos.writeObject(comm);
            oos.flush();
		}
		catch (IOException ex) {
			//TODO: Log/fail
		}
		
		try {
			ois = new ObjectInputStream(_socket.getInputStream());
			comm = (Communication)ois.readObject();
			resp = (MSHandshakeResponse)comm.getPayload();
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
				success = true;
				break;
			case DENIED:
			default:
				success = false;
				break;
		}
		
        try {
            oos.close();
            ois.close();
        }
		catch (IOException ex) {
			//TODO: Log/fail
		}

		return success;
	}
	
	public static void establishSocketConnection() throws IOException
	{
		_socket = new Socket();
		_socket.connect(_masterAddr);
	}
	
	public static boolean createFile(String filename)
	{
		if (_fileMap.get(filename) != null) return false;  //if it already exists
		String localName = "localfile_" + _fileCounter.getAndIncrement();
		try {
			StorFile sFile = new StorFile(localName);
			_fileMap.put(filename, sFile);
		}
		catch (FileNotFoundException ex) {
			return false;
		}
		return true;
	}
	
	public static boolean removeFile(String filename)
	{
		//TODO: Implement
		return false;
	}
	
	public static boolean writeFile(String filename, byte[] data, int off, int len)
	{
		StorFile file = _fileMap.get(filename);
		if (file == null) return false;
		try {
			file.write(data, off, len);
		}
		catch (IOException ex) {
			//TODO: Log
			return false;
		}
		return true;
	}
	
	public static boolean readFile(String filename, byte[] data, int off, int len)
	{
		StorFile file = _fileMap.get(filename);
		if (file == null) return false;
		try {
			file.read(data, off, len);
		}
		catch (IOException ex) {
			//TODO: Log
			return false;
		}
		return true;
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
		
		public void write(byte [] data, int off, int len) throws IOException
		{
			m_writeLock.lock();
			m_file.write(data, off, len);
			m_writeLock.unlock();
		}
		
		public void read(byte [] data, int off, int len) throws IOException
		{
			m_readLock.lock();
			m_file.read(data, off, len);
			m_readLock.unlock();
		}
		
		public long getLength() throws IOException
		{
			return m_file.length();
		}
	}
}
