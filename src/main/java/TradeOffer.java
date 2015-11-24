import java.io.IOException;
import java.util.List;

public class TradeOffer {
    public int id;
    public boolean isActive;
    Inventory botInventory;
    Inventory partnerInventory;


    TradeOffer(int id, boolean isActive, List<String> botItemEconomyData, List<String> partnerItemEconomyData) throws IOException {
        this.id = id;
        this.isActive = isActive;
        botInventory = new Inventory(botItemEconomyData);
        partnerInventory = new Inventory(partnerItemEconomyData);
    }
}
