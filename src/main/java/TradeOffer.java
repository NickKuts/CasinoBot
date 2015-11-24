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

    public class TradeID
    {
        String tradeid;
    }

    TradeOffer(int id, boolean isActive, List<String> botItemEconomyData, List<String> partnerItemEconomyData) throws Exception {
        this.id = id;
        this.isActive = isActive;
        botInventory = new Inventory(botItemEconomyData);
        partnerInventory = new Inventory(partnerItemEconomyData);

        isAccepted = false;
    }

    void accept() throws IOException {
        String baseURI = "https://steamcommunity.com/tradeoffer/" + id + "/accept";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tradeofferid", String.valueOf(id)));
        params.add(new BasicNameValuePair("serverid", "1"));
        //params.add(new BasicNameValuePair("partner", ""));

        String answer = BotUser.currentUser.requestor.getAnswer(Requestor.query_type.POST, baseURI, params);

        System.out.println(answer);;


        TradeID final_answer = BotUser.currentUser.gsonEntity.fromJson(answer, TradeID.class);

        if(final_answer.tradeid != null)
            System.out.println("SUCCESS");
    }



}
