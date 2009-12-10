package cs5204.fs.test;

import cs5204.fs.client.SClient;
import cs5204.fs.client.SFile;

public class SimpleClient
{
	public static void main(String [] args) throws InterruptedException
	{
        SClient client = new SClient("localhost", 2009);
		
		System.out.println("Attempting to connect client...");
		Thread.sleep(3000);
        if (client.connect())
            System.out.println("Client connected.");
        else
			System.out.println("Connecting client failed! NOOOO!!!");
		Thread.sleep(3000);
		
		System.out.println("Attempting to create directory /foo");
		Thread.sleep(5000);
		if(client.createDirectory("/foo"))
			System.out.println("Success in creating directory /foo!");
		else
			System.out.println("Failed to create /foo");
		Thread.sleep(3000);
		
		System.out.println("Attempting to create file /foo/bar.txt");
		Thread.sleep(5000);
		SFile file = client.createFile("/foo/bar.txt");
		if (file != null)
			System.out.println("Success in creating /foo/bar.txt!");
		else
			System.out.println("Failed to create /foo/bar.txt");
		Thread.sleep(3000);
		
		System.out.println("Attempting to write Hello World! to /foo/bar.txt");
		Thread.sleep(5000);
		String appendData = "Hello World!";
		if(client.append(file, appendData.getBytes()))
			System.out.println("Success in appending data to /foo/bar.txt!");
		else
			System.out.println("Failed to append data");
		Thread.sleep(3000);
		
		System.out.println("Attempting to read contents of file /foo/bar.txt");
		Thread.sleep(5000);
		byte[] readData = new byte[appendData.length()];
		if(client.read(file, readData, 0, readData.length))
			System.out.println("Success in reading " + new String(readData) + " from /foo/bar.txt!");
		else
			System.out.println("Failed to read data");
		
        client.disconnect();
	}
}
