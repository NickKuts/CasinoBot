import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TradeOffer {
    public int id;
    public boolean isActive;
    Inventory botInventory;
    Inventory partnerInventory;

    public boolean isAccepted;


    TradeOffer(int id, boolean isActive, List<String> botItemEconomyData, List<String> partnerItemEconomyData) throws IOException {
        this.id = id;
        this.isActive = isActive;
        botInventory = new Inventory(botItemEconomyData);
        partnerInventory = new Inventory(partnerItemEconomyData);

        isAccepted = false;
    }

    void accept()
    {
        String baseURI = "https://steamcommunity.com/tradeoffer/" + id + "/accept";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tradeofferid", String.valueOf(id)));
        params.add(new BasicNameValuePair("serverid", "1"));
        //params.add(new BasicNameValuePair("partner", ""));



    }



}
