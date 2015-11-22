import com.sun.org.glassfish.gmbal.NameValue;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.lang.model.element.Name;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Item {
    public String marketHashName;
    public float marketPrice;

    public String appId;
    public String contextId;
    public String itemId;

    public String partnerId;
    public JSONMetrics weaponMetrics;


    class JSONMetrics
    {
        boolean success;
        String lowest_price;
        String volume;
        String median_price;
    }

    Item(String itemEconomyData) throws IOException {
        String[] parts = itemEconomyData.split("/");

        appId = parts[0];
        contextId = parts[1];
        itemId = parts[2];
        partnerId = parts[3];

        weaponMetrics = new JSONMetrics();

        //Getting market_hash_name of item


        Document document = Jsoup.parse(BotUser.currentUser.requestor.getAnswer(Requestor.query_type.GET, "https://steamcommunity.com/"
                + "economy/itemhover/" + appId + "/" + contextId + "/" + itemId + "?o=" + partnerId, null));

        int index = document.toString().indexOf("market_hash_name\":\"");

        StringBuilder str = new StringBuilder();
        int i = 0;
        int counter = 0;
        char c;
        int j = 0;
        while(counter != 3)
        {
            c = document.toString().charAt(index + i);

            if(counter == 2 && c != "\"".toCharArray()[0])
            {
                str.insert(j, c);
                j++;
            }

            if(c == "\"".toCharArray()[0])
                counter++;
            marketHashName = str.toString();
            i++;
        }

        getMetrics(marketHashName, appId);

        System.out.println(weaponMetrics.lowest_price);
    }

    void getMetrics(String marketHashName, String appId) throws IOException {
        String baseURI = "http://steamcommunity.com/market/priceoverview/";
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        String formattedMarketHashName = marketHashName.replace(" ","%20");

        params.add(new BasicNameValuePair("country", "US"));
        params.add(new BasicNameValuePair("currency", "1"));
        params.add(new BasicNameValuePair("appid", appId));
        params.add(new BasicNameValuePair("market_hash_name", marketHashName));

        String response = BotUser.currentUser.requestor.getAnswer(Requestor.query_type.GET, baseURI + "?" + "country=US&" + "currency=1&" +
                                                                     "appid=" + appId + "&" + "market_hash_name=" + formattedMarketHashName, null);

        weaponMetrics = BotUser.currentUser.gsonEntity.fromJson(response, JSONMetrics.class);
    }
}
