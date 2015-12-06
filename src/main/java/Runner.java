import java.io.IOException;

public class Runner {


    public static void main(String args[])
    {
        BotUser currentUser = new BotUser();

        try {
            currentUser.steamLogin("vov4iktr", "botsteam190");
            TradeOffer[] tradeOffers = currentUser.getIncomingTradeOffers();
            System.out.println("Program finished");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ;
    }
}
