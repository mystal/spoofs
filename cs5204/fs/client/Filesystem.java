package cs5204.fs.client;

import cs5204.fs.common.Protocol;
import cs5204.fs.rpc.Communication;
import cs5204.fs.rpc.Payload;
import cs5204.fs.rpc.CMHandshakeRequest;
import cs5204.fs.rpc.CMHandshakeResponse;

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

public class Filesystem
{
	private static SocketAddress _masterAddr;
    private static int _id;
    private static String _ipAddr;

    public static void init(String addr, int port) throws Exception
    {
        InputStream is = null;
        OutputStream os = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		Communication comm = null;
		CMHandshakeRequest req = null;
		CMHandshakeResponse resp = null;
        Socket socket = null;

		_masterAddr = new InetSocketAddress(addr, port);

        try {
            _ipAddr = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex) {
            //TODO: Log/fail
        }
		
        try {
            socket = new Socket();
            socket.connect(_masterAddr);

            is = socket.getInputStream();
            os = socket.getOutputStream();
        }
        catch (IOException ex) {
            //TODO: Log/fail
        }

        req = new CMHandshakeRequest(_ipAddr);
		
		try {
			oos = new ObjectOutputStream(os);
			comm = new Communication(Protocol.CM_HANDSHAKE_REQUEST, req);
			oos.writeObject(comm);
			//oos.close();
            oos.flush();
		}
		catch (IOException ex) {
			//TODO: Log/fail
		}
		
		try {
			ois = new ObjectInputStream(is);
			comm = (Communication)ois.readObject();
			resp = (CMHandshakeResponse)comm.getPayload();
			//ois.close();
		}
		catch (IOException ex) {
			//TODO: log/fail
		}
		catch (ClassNotFoundException ex) {
			//TODO: log/fail
		}
		
		switch (resp.getStatus())
		{
			case OK:
				_id = resp.getId();
				break;
			case DENIED:
			default:
				//TODO: Throw exception
                break;
		}
		
        try {
            oos.close();
            ois.close();
        }
		catch (IOException ex) {
			//TODO: Log/fail
		}
    }

    public static SFile createFile(String filepath)
    {
        return new SFile(filepath);
    }

    public static boolean createDirectory(String dirpath)
    {
        return false;
    }

    public static SFile open(String filepath)
    {
        return new SFile(filepath);
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
}
