import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                Pair<String, String> curPair = (Pair<String,String>) data;

                Item curItem = new Item(curPair.getKey(), curPair.getValue());
            }
        }
    }
}
