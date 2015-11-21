import jdk.internal.util.xml.impl.Input;
import org.apache.http.Consts;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static org.apache.http.Consts.UTF_8;


public class Requestor {

    static enum query_type {GET, POST};
    static String baseURI = "https://steamcommunity.com";

    String getAnswer(query_type type, String baseURI, List<NameValuePair> params) throws IOException {
        HttpRequest request = createNewRequest(type, baseURI, params);


        HttpResponse response = null;

        try {
            response = BotUser.currentUser.httpClient.execute((HttpUriRequest) request, BotUser.currentUser.httpClientContext);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert response != null;

        java.util.Scanner serverAnswer = new java.util.Scanner(response.getEntity().getContent(), "UTF-8").useDelimiter("\\A");


        return serverAnswer.hasNext() ? serverAnswer.next() : "";
    }

    HttpRequest createNewRequest(query_type type, String baseURI, List<NameValuePair> params)
    {
        HttpRequest toReturn;

        if(type == query_type.GET)
        {
            toReturn = new HttpGet(baseURI);
        }
        else
        {
            toReturn = new HttpPost(baseURI);
        }

        toReturn.setHeader("Accept", "text/javascript, text/html, application/xml, text/xml, */*");
        toReturn.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        toReturn.setHeader("Host", "steamcommunity.com");
        toReturn.setHeader("Referer", "http://steamcommunity.com/tradeoffer/1");

        if(params != null && type == query_type.POST)
            ((HttpPost) toReturn).setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

        return toReturn;
    }

}
