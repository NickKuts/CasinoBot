import com.google.gson.Gson;

import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.DefaultHttpClient;


public class BotUser {
    protected HttpClient httpClient;
    protected HttpClientContext httpClientContext;

    public static final BotUser currentUser = new BotUser();

    BotUser()
    {
        httpClient = new DefaultHttpClient();
    }

}