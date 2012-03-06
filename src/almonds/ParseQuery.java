package almonds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseQuery
{
	private String mClassName;

	public ParseQuery(String className)
	{
		mClassName = className;
	}

	public List<ParseObject> find()
	{
		ArrayList<ParseObject> objects = null;

		try
		{

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(Parse.getParseAPIUrlClasses() + mClassName);
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

					/*
					 * mCountries.put(
					 * results.getJSONObject(i).getString("name"), new
					 * Country(results.getJSONObject(i).getString( "objectId"),
					 * results.getJSONObject(i) .getString("name")));
					 */

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

		return objects;
	}
}
