package almonds;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseObject
{
	class SaveInBackgroundThread extends Thread
	{
		SaveCallback mSaveCallback;

		SaveInBackgroundThread(SaveCallback callback)
		{
			mSaveCallback = callback;
		}

		public void run()
		{
			ParseException exception = null;
			
			try
			{
				save();
			}
			catch (ParseException e)
			{
				exception = e;			
			}
			
			mSaveCallback.done(exception);
		}
	}

	/**
	 * @param className
	 * @return
	 */
	public static ParseObject create(String className)
	{
		return new ParseObject(className);
	}

	private String mClassName;

	private Hashtable<String, Object> mData;

	public ParseObject(String theClassName)
	{
		mClassName = theClassName;
		mData = new Hashtable<String, Object>();

	}

	public ParseObject(String theClassName, JSONObject json)
	{
		mClassName = theClassName;
		mData = new Hashtable<String, Object>();

		for (String name : JSONObject.getNames(json))
		{
			try
			{

				/*
				 * Check for data types here. If the 'value' is JSONObject then
				 * there's additional data type information.
				 */
				if (json.get(name).getClass().getName().equals("org.json.JSONObject"))
				{
					JSONObject oType = (JSONObject) json.get(name);

					if (oType.get("__type").equals("Pointer"))
					{
						System.out.println("Found Pointer");
						put(name,
								new ParsePointer(oType.getString("className"), oType
										.getString("objectId")));
					}
					else if (oType.get("__type").equals("Date"))
					{

					}
				}
				else
				{
					put(name, json.get(name));
				}
			}
			catch (JSONException e)
			{

			}
		}

	}

	public boolean containsKey(String key)
	{
		return mData.containsKey(key);
	}

	public void delete() throws ParseException
	{
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpDelete httpdelete = new HttpDelete(Parse.getParseAPIUrlClasses() + mClassName + "/"
					+ getObjectId());
			httpdelete.addHeader("X-Parse-Application-Id", Parse.getApplicationId());
			httpdelete.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());

			HttpResponse response = httpclient.execute(httpdelete);
			HttpEntity entity = response.getEntity();

			if (entity != null)
			{
				JSONObject jsonResponse = new JSONObject(EntityUtils.toString(entity));

				//
				// Check HTTP status code for error
				//
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode >= 200 && statusCode < 300)
				{

				}
				else
				{
					throw new ParseException(jsonResponse.getInt("code"),
							"Error getting the requested object.  Reason: "
									+ jsonResponse.getString("error"));
				}
			}
			else
			{
				throw new ParseException(ParseException.CONNECTION_FAILED,
						"Connection failed with Parse servers.");
			}
		}
		catch (ClientProtocolException e)
		{
			throw new ParseException(ParseException.CONNECTION_FAILED,
					"Connection failed with Parse servers.  Log: " + e.getMessage());
		}
		catch (IOException e)
		{
			throw new ParseException(ParseException.CONNECTION_FAILED,
					"Connection failed with Parse servers.  Log: " + e.getMessage());
		}
		catch (JSONException e)
		{
			throw new ParseException(ParseException.CONNECTION_FAILED,
					"Bad JSON response from Parse servers. Log: " + e.getMessage());
		}
	}

	public String getClassName()
	{
		return mClassName;
	}

	public String getObjectId()
	{
		return (String) mData.get("objectId");
	}
	
	public void setObjectId(String objectId)
	{
		mData.put("objectId", objectId);		
	}
	
	public void setCreatedAt(String createdAt)
	{
		mData.put("createdAt", createdAt);
	}

	public ParsePointer getParsePointer(String key)
	{
		return (ParsePointer) mData.get(key);
	}

	public ParsePointer getPointer()
	{
		return new ParsePointer(mClassName, getObjectId());
	}

	public String getString(String key)
	{
		return (String) mData.get(key);
	}

	public boolean has(String key)
	{
		return containsKey(key);
	}

	public void put(String key, Object value)
	{
		mData.put(key, value);
	}

	public void save() throws ParseException
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
				JSONObject jsonResponse = new JSONObject(EntityUtils.toString(entity));

				//
				// Check HTTP status code for error
				//
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode >= 200 && statusCode < 300)
				{
					//
					// the response contains the new objectId and createdAt fields
					
					setObjectId(jsonResponse.getString("objectId"));
					setCreatedAt(jsonResponse.getString("createdAt"));
				}
				else
				{
					throw new ParseException(jsonResponse.getInt("code"),
							"Error getting the requested object.  Reason: "
									+ jsonResponse.getString("error"));
				}
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
		catch (org.apache.http.ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void saveInBackground(SaveCallback callback)
	{
		SaveInBackgroundThread t = new SaveInBackgroundThread(callback);
		t.start();
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

		return jo;
	}

}
