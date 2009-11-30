package cs5204.fs.master;

import java.util.concurrent.ConcurrentHashMap;

public class Directory
{
	private ConcurrentHashMap<String, File> m_files;
	private ConcurrentHashMap<String, Directory> m_directories;
	private String m_name;//needed?
	private Directory m_parent;//needed?
	
	public Directory(Directory parent, String name)
	{
		m_parent = parent;
		m_name = name;
		m_files = new ConcurrentHashMap<String, File>();
		m_directories = new ConcurrentHashMap<String, Directory>();
	}
	
	public File getFile(String name)
	{
        return m_files.get(name);
	}
	
	public Directory getDirectory(String name)
	{
        return m_directories.get(name);
	}
	
	public boolean addDirectory(String name)
	{
		m_directories.put(name, new Directory(this, name));
		return true;
	}
	
	public boolean addFile(String name)
	{
		m_files.put(name, new File(this, name));
		return true;
	}
	
	//TODO: remove[File|Directory]
	
}
