package almonds;

public class Parse 
{
	private static String mApplicationId;
	private static String mClientKey;
	
	private static final String PARSE_API_URL = "https://api.parse.com"; 
	
	static void initialize(String applicationId, String clientKey)
	{
		mApplicationId = applicationId;
		mClientKey = clientKey;
	}
	
	static public String getApplicationId() {return mApplicationId;}
	static public String getClientKey() {return mClientKey;}
}
