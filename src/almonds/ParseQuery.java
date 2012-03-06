package almonds;

import java.util.ArrayList;
import java.util.List;

public class ParseQuery 
{
	private String mClassName;
	
	public ParseQuery(String className)
	{
		mClassName = className;
	}
	
	public List<ParseObject> find()
	{
		ArrayList<ParseObject> objects = new ArrayList<ParseObject>();
		return objects;
	}
}
