package cs5204.fs.master;

import java.util.HashMap;

public class Directory
{
	private HashMap<String, File> m_files;
	private HashMap<String, Directory> m_directories;
	private String m_name;//needed?
	private Directory m_parent;//needed?
	
	public Directory(Directory parent, String name)
	{
		m_parent = parent;
		m_name = name;
		m_files = new HashMap<String, File>();
		m_directories = new HashMap<String, Directory>();
	}
	
	public File getFile(String name)
	{
        return m_files.get(name);
	}
	
	public Directory getDirectory(String name)
	{
        return m_directories.get(name);
	}
	
	//TODO: add[File|Directory], remove[File|Directory]
	
}
