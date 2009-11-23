package cs5204.fs.lib;

import java.util.ArrayList;

public class StringUtil
{
	public static ArrayList<String> explodeString(String path)
	{
		ArrayList<String> list = new ArrayList<String>();
		String [] allTokens = path.split("/");
		
		for (String s : allTokens)
		{
			if (s.equals("") || s.equals("."))
				continue;
			else if (s.equals(".."))
				list.remove(list.size()-1);
			else
				list.add(s);
		}
		
		return list;
	}
}