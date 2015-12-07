import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TEST

public class Inventory {
    List<Item> items;

    static enum typeOfOperation {incomingTradeoffer, outcomingTradeoffer};

    Inventory(List<Object>  items, typeOfOperation type) throws Exception {
        if(type == typeOfOperation.incomingTradeoffer)
        {
            // each object is a string
            this.items = new ArrayList<Item>();

            for(Object data : items)
            {
                this.items.add(new Item((String)data));
            }
        }
        else if((type == typeOfOperation.outcomingTradeoffer))
        {
            // each object is a pair of strings (appId, marketHashName)

            this.items = new ArrayList<Item>();

            for(Object data : items)
            {
                BotUser.ItemDesription item = (BotUser.ItemDesription) data;

<<<<<<< HEAD
                this.items.add(new Item(item.appId, item.marketHashName, item.assetId));
=======
                Item curItem = new Item(item.appId, item.marketHashName, item.assetId);
>>>>>>> origin/master
            }
        }
    }
}
