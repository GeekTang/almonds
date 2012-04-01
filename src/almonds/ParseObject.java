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

/**
 * The <b>ParseObject</b> is a local representation of data that can be saved and retrieved from the Parse cloud.
 * 
 * <p>The basic workflow for creating new data is to construct a new ParseObject, use put() to fill it with data, and then use save() to persist to the databa
 * 
 * <p>The basic workflow for accessing existing data is to use a ParseQuery to specify which existing data to retrieve.
 * 
 * @author js
 *
 */
public class ParseObject
{
	/**
	 * A private helper class to facilitate running a ParseObject save operation in the background.
	 * 
	 * @author js
	 *
	 */
	class SaveInBackgroundThread extends Thread
	{
		SaveCallback mSaveCallback;

		/**
		 * 
		 * @param callback A function object of type Savecallback, whose method done will be called upon completion
		 */
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
	 * Creates a new ParseObject based upon a class name. If the class name is a special type (e.g. for ParseUser), then the 
	 * appropriate type of ParseObject is returned.
	 * 
	 * @param className
	 * 
	 * @return
	 */
	public static ParseObject create(String className)
	{
		return new ParseObject(className);
	}

	private String mClassName;

	private Hashtable<String, Object> mData;

	/**
	 * Constructs a new ParseObject with no data in it. A ParseObject constructed in this way will not have an objectId and will 
	 * not persist to the database until save() is called.
	 * 
	 * Class names must be alphanumerical plus underscore, and start with a letter. It is recommended to name classes in CamelCaseLikeThis.
	 * 
	 * @param theClassName The className for this ParseObject.
	 */
	public ParseObject(String theClassName)
	{
		mClassName = theClassName;
		mData = new Hashtable<String, Object>();

	}

	/**
	 * Used to support a query that returns an object from Parse encoded with JSON.  This constructor will create itself
	 * from the JSON.  This is probably a poor method, especially if the JSON is mal-formed.  A better approach would be
	 * move the handling to ParseQuery, where it alone would understands query responses from Parse.
	 * 
	 * @param theClassName The className for this ParseObject
	 * @param json JSON encoded response from Parse corresponding to a ParseObject
	 */
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
						throw new UnsupportedOperationException();
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

	/**
	 * Whether this object has a particular key. Same as 'has'.
	 * 
	 * @param key The key to check for
	 * @return Returns whether this object contains the key
	 */
	public boolean containsKey(String key)
	{
		return mData.containsKey(key);
	}

	/**
	 * Deletes this object on the server. This does not delete or destroy the object locally.
	 * 
	 * @throws ParseException Throws an error if the object does not exist or if the internet fails.
	 * 
	 */
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

	/**
	 * Accessor to the class name.
	 * 
	 * @return
	 */
	public String getClassName()
	{
		return mClassName;
	}

	/**
	 * Accessor to the object id. An object id is assigned as soon as an object is saved to the server. The combination of a className and an 
	 * objectId uniquely identifies an object in your application.
	 * 
	 * @return The object id.
	 */
	public String getObjectId()
	{
		return (String) mData.get("objectId");
	}
	
	
	/**
	 * Setter for the object id. In general you do not need to use this. However, in some cases this can be convenient. For example, if you are serializing a ParseObject yourself 
	 * and wish to recreate it, you can use this to recreate the ParseObject exactly.
	 * 
	 * @param objectId
	 */
	public void setObjectId(String objectId)
	{
		mData.put("objectId", objectId);		
	}
	
	public void setCreatedAt(String createdAt)
	{
		mData.put("createdAt", createdAt);
	}

	/**
	 * Access a ParsePointer value.
	 * 
	 * @param key The key to access the value for.
	 * @return Returns null if there is no such key or if it is not a ParsePointer.
	 */
	public ParsePointer getParsePointer(String key)
	{
		Object value = mData.get(key);
		
		if (value == null)
			return null;
		
		// Verify that the value of this key is, in fact, a ParsePointer, returning null is not
		if (value.getClass() != ParsePointer.class)
			return null;
		
		return (ParsePointer) value;
	}

	/**
	 * Creates and returns a new ParsePointer object that points to the object it's called on.  Use when other
	 * objects need to point to *this* object.
	 * 
	 * @return A ParsePointer object set to point to this object.
	 */
	public ParsePointer getPointer()
	{
		return new ParsePointer(mClassName, getObjectId());
	}

	/**
	 * Access a string value.
	 * 
	 * @param key The key to access the value for.
	 * @return Returns null if there is no such key or if it is not a String. 
	 */
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
