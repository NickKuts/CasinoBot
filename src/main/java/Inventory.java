import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Inventory {
    List<Item> items;

    Inventory(List<String> items) throws Exception {
        this.items = new ArrayList<Item>();

        for(String data : items)
        {
            this.items.add(new Item(data));
        }
    }
}
