package almonds;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseObject
{
	private String mClassName;

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
		return (String) mData.get("objectId");
	}

	public void put(String key, Object value)
	{
		mData.put(key, value);
	}

	public String getString(String key)
	{
		return (String) mData.get(key);
	}

	public void save()
	{
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Parse.getParseAPIUrlClasses() + mClassName);
			httppost.addHeader("X-Parse-Application-Id", Parse.getApplicationId());
			httppost.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());
			httppost.addHeader("Content-Type", "application/json");

			httppost.setEntity(new StringEntity(toJSONObject().toString()));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null)
			{
			}
		}
		catch (ClientProtocolException e)
		{
			System.out.println(e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
		
	}

	private JSONObject toJSONObject()
	{
		JSONObject jo = new JSONObject();

		try
		{
			for (String key : mData.keySet())
				jo.put(key, mData.get(key));				
		}
		catch (JSONException e)
		{

		}
		
		System.out.println(jo.toString());
		return jo;
	}

}
