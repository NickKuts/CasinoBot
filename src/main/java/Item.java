import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Item {
    public String marketHashName;
    public float marketPrice;

    public String appId;
    public String contextId;
    public String itemId;

    public String partnerId;

    Item(String itemEconomyData) throws IOException {
        String[] parts = itemEconomyData.split("/");

        appId = parts[0];
        contextId = parts[1];
        itemId = parts[2];
        partnerId = parts[3];

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
        System.out.println(marketHashName);


    }
}
