package almonds;

import java.util.Hashtable;

public class ParseObject
{
	private String mClassName;
	private String mObjectId;
	
	private Hashtable<String, Object> mData;

	public ParseObject(String theClassName)
	{
		mClassName = theClassName;
		mData = new Hashtable<String, Object>();

	}

	public String getClassName()
	{
		return mClassName;
	}
	
	public String getObjectId()
	{
		return mObjectId;
	}
	
	public void put(String key, Object value)
	{
		mData.put(key, value);
	}
	
	public String getString(String key)
	{
		return (String)mData.get(key);
	}
	
	

}
