import java.util.List;

public class TradeOffer {
    public int id;
    public boolean isActive;
    //Inventory botInventory;   // appID/contextID/itemID/partnerID
    //Inventory partnerInventory;


    TradeOffer(int id, boolean isActive)
    {
        this.id = id;
        this.isActive = isActive;

    }
}
