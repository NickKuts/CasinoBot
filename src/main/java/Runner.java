import java.io.IOException;

public class Runner {


    public static void main(String args[])
    {
        BotUser currentUser = new BotUser();
        Requestor requestor = new Requestor();

        try {
            System.out.println(requestor.getAnswer(Requestor.query_type.GET, "https://steamcommunity.com", null));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ;
    }
}
