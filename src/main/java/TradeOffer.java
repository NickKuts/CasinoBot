import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TradeOffer {
    public int id;
    public boolean isActive;
    public String partnerId;
    Inventory botInventory;
    Inventory partnerInventory;

    public boolean isAccepted;

    public class TradeID
    {
        String tradeid;
    }

    TradeOffer(int id, boolean isActive, List<Object> botItemEconomyData, List<Object> partnerItemEconomyData) throws Exception {
        this.id = id;
        this.isActive = isActive;
        botInventory = new Inventory(botItemEconomyData, Inventory.typeOfOperation.incomingTradeoffer);
        partnerInventory = new Inventory(partnerItemEconomyData, Inventory.typeOfOperation.incomingTradeoffer);

        String[] parts = ((String)partnerItemEconomyData.get(0)).split("/");

        this.partnerId = parts[3];

        isAccepted = false;
    }

    public void accept() throws IOException {
        String baseURI = "https://steamcommunity.com/tradeoffer/" + id + "/accept";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tradeofferid", String.valueOf(id)));
        params.add(new BasicNameValuePair("serverid", "1"));
        params.add(new BasicNameValuePair("partner", partnerId));

        String sessionId = BotUser.currentUser.httpClientContext.getCookieStore().getCookies().get(0).getValue();


        params.add(new BasicNameValuePair("sessionid",sessionId));
        

        String answer = BotUser.currentUser.requestor.getAnswer(Requestor.query_type.POST, baseURI, params);

        System.out.println(answer);;

        TradeID final_answer = BotUser.currentUser.gsonEntity.fromJson(answer, TradeID.class);

        if(final_answer.tradeid != null)
            System.out.println("SUCCESS");
    }

    public void sendTradeOffer()
    {




    }


}
