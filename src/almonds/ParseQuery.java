package almonds;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The ParseQuery class defines a query that is used to fetch ParseObjects. The most common use case is finding all 
 * objects that match a query through the findInBackground method, using a FindCallback.
 * 
 * @author js
 */
public class ParseQuery
{
	private String mClassName;
	private SimpleEntry mWhereEqualTo = null;

	/**
	 * Constructs a query. A default query with no further parameters will retrieve all ParseObjects of the provided class.
	 * 
	 * @param className The name of the class to retrieve ParseObjects for.
	 */
	public ParseQuery(String className)
	{
		mClassName = className;
	}

	/**
	 * Helper Thread to execute Parse Get calls off of the main application thread.
	 *
	 */
	class GetInBackgroundThread extends Thread
	{
		GetCallback mGetCallback;
		String mObjectId;

		/**
		 * @param objectId
		 * @param callback
		 */
		GetInBackgroundThread(String objectId, GetCallback callback)
		{
			mGetCallback = callback;
			mObjectId = objectId;
		}

		public void run()
		{
			try
			{
				ParseObject getObject = get(mObjectId);
				mGetCallback.done(getObject);
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * Constructs a ParseObject whose id is already known by fetching data from the server in a background thread.
	 * This does not use caching. This is preferable to using the ParseObject(className, objectId) constructor, unless your code is already running in a background thread.
	 * 
	 * @param objectId Object id of the ParseObject to fetch. 
	 * @param callback callback.done(object, e) will be called when the fetch completes.
	 */
	public void getInBackground(String objectId, GetCallback callback)
	{
		GetInBackgroundThread t = new GetInBackgroundThread(objectId, callback);
		t.start();
	}

	/**
	 * Constructs a ParseObject whose id is already known by fetching data from the server. This does not use caching.
	 * 
	 * @param theObjectId Object id of the ParseObject to fetch.
	 * @return 
	 * @throws ParseException  Throws an exception when there is no such object or when the network connection fails.
	 */
	public ParseObject get(String theObjectId) throws ParseException
	{
		ParseObject o = null;

		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(Parse.getParseAPIUrlClasses() + mClassName + "/"
					+ theObjectId);
			httpget.addHeader("X-Parse-Application-Id", Parse.getApplicationId());
			httpget.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());

			HttpResponse response = httpclient.execute(httpget);
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
					o = new ParseObject(mClassName, jsonResponse);
				}
				else
				{
					throw new ParseException(jsonResponse.getInt("code"), "Error getting the requested object.  Reason: " + jsonResponse.getString("error"));
				}
			}
			else
			{
				throw new ParseException (ParseException.CONNECTION_FAILED, "Connection failed with Parse servers.");
			}
			
			return o;
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		finally
		{
		}

		return o;
	}

	/**
	 * A thread used to execute a ParseQuery find off of the main thread. 
	 *
	 */
	private class FindInBackgroundThread extends Thread
	{
		FindCallback mFindCallback;

		FindInBackgroundThread(FindCallback callback)
		{
			mFindCallback = callback;
		}

		public void run()
		{
			mFindCallback.done(find());
		}
	}

	/**
	 * Retrieves a list of ParseObjects that satisfy this query from the server in a background thread. This is preferable to using find(), 
	 * unless your code is already running in a background thread.
	 * 
	 * @param callback callback - callback.done(object, e) is called when the find completes.
	 */
	public void findInBackground(FindCallback callback)
	{
		FindInBackgroundThread t = new FindInBackgroundThread(callback);
		t.start();
	}

	
	/**
	 * Retrieves a list of ParseObjects that satisfy this query. Uses the network and/or the cache, depending on the cache policy.
	 * 
	 * @return A list of all ParseObjects obeying the conditions set in this query.
	 */
	public List<ParseObject> find()
	{
		ArrayList<ParseObject> objects = null;

		try
		{

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(Parse.getParseAPIUrlClasses() + mClassName
					+ getURLConstraints());
			httpget.addHeader("X-Parse-Application-Id", Parse.getApplicationId());
			httpget.addHeader("X-Parse-REST-API-Key", Parse.getRestAPIKey());

			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (entity != null)
			{
				JSONObject obj = new JSONObject(EntityUtils.toString(entity));
				JSONArray results = obj.getJSONArray("results");

				objects = new ArrayList<ParseObject>();

				for (int i = 0; i < results.length(); i++)
				{
					ParseObject parseObject = new ParseObject(mClassName);
					JSONObject jsonObject = results.getJSONObject(i);

					for (String name : JSONObject.getNames(jsonObject))
					{
						parseObject.put(name, jsonObject.get(name));
					}

					objects.add(parseObject);

				}
			}

		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (org.apache.http.ParseException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		finally
		{
		}

		return objects;
	}

	/**
	 * Add a constraint to the query that requires a particular key's value to be equal to the provided value.
	 * 
	 * @param key The key to check.
	 * @param value The value that the ParseObject must contain.
	 * @return Returns the query, so you can chain this call.
	 */
	public ParseQuery whereEqualTo(String key, Object value)
	{
		mWhereEqualTo = new SimpleEntry<String, Object>(key, value);
		return this;
	}

	/**
	 * Helper to easily decide if any contraints have been set.
	 * 
	 * @return True if any type of contraints have been placed on the Query
	 */
	private boolean hasConstraints()
	{
		return (mWhereEqualTo != null);
	}

	/**
	 * Constraints on a Query using the REST API are communicated as 'where' parameters in the URL.  This
	 * method takes the current constraints on the Query and returns them formatted as a partial URL.
	 * 
	 * @return The URL formatted Query constraints.
	 */
	private String getURLConstraints()
	{
		String url = "";

		if (hasConstraints())
		{
			try
			{
				url = "?" + "where=" + URLEncoder.encode(getJSONConstraints(), "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		return url;
	}

	/**
	 * Creates a Parse readable JSON string containing the current Query constraints.  This can then be
	 * URL formatted for using in an HTTP request.
	 * 
	 * @return A Parse readable JSON string of constraints to place on a Query.
	 */
	private String getJSONConstraints()
	{
		String js = "";

		if (mWhereEqualTo != null)
		{
			JSONObject jo = new JSONObject();

			try
			{
				jo.put(((String) mWhereEqualTo.getKey()), mWhereEqualTo.getValue());
				js = jo.toString();
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}

		return js;
	}
}
