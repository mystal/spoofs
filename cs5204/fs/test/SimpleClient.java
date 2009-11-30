package cs5204.fs.test;

import cs5204.fs.client.Filesystem;
import cs5204.fs.client.SFile;

public class SimpleClient
{
	public static void main(String [] args)
	{
		try {
			Filesystem.init("localhost", 2010);
		}
		catch (Exception ex) {
			System.out.println("NOOOOO!!!!    " + ex);
		}
		
		if(Filesystem.createDirectory("/foo"))
			System.out.println("Success in creating directory /foo!");
		else
			System.out.println("Failed to create /foo");
	}
}