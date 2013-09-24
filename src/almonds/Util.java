package almonds;

import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

public class Util
{
	private static Properties properties = new Properties();
	
	private static final String PROXY_HOST = "http.proxyHost";
	
	private static final String PROXY_PORT = "http.proxyPort";
	
	public static void setHttpProxy(String host, String port){
		properties.setProperty(PROXY_HOST, host);
		properties.setProperty(PROXY_PORT, port);
	}

	public static HttpClient newHttpClient()
	{
		HttpClient httpclient = new DefaultHttpClient();
		if(null != properties.getProperty(PROXY_HOST) && "".equals(properties.getProperty(PROXY_HOST).trim())){
			HttpHost proxy = new HttpHost(properties.getProperty(PROXY_HOST),Integer.parseInt(properties.getProperty(PROXY_PORT)));
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
		}
		
		return httpclient;
	}
}
