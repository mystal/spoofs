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
	
	public boolean isEmpty()
	{
		return m_files.isEmpty() && m_directories.isEmpty();
	}
	
	public File getFile(String name)
	{
        return m_files.get(name);
	}
	
	public Directory getDirectory(String name)
	{
        return m_directories.get(name);
	}
	
	public Directory addDirectory(String name)
	{
		Directory dir = new Directory(this, name);
		m_directories.put(name, dir);
		return dir;
	}
	
	public File addFile(String name, int storId)
	{
		File file = new File(this, storId);
		m_files.put(name, file);
		return file;
	}
	
	public boolean removeFile(String name)
	{
		m_files.remove(name);
		return true;
	}
	
	public boolean removeDirectory(String name)
	{
		Directory toRemove = m_directories.get(name);
		if (toRemove != null && toRemove.isEmpty())
			return false;
		m_directories.remove(name);
		return true;
	}
}
