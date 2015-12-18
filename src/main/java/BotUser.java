import com.google.gson.Gson;

import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.util.Pair;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.rmi.runtime.Log;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.CookieStore;
import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.List;


public class BotUser {
    protected HttpClient httpClient;
    protected HttpClientContext httpClientContext;
    protected Gson gsonEntity;

    protected TwoFactorAuthData data2fa;

    Inventory wholeInventory;

    public static final Requestor requestor = new Requestor();

    public static final BotUser currentUser = new BotUser();

    BotUser() {
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        gsonEntity = new Gson();

        data2fa = new TwoFactorAuthData();

        httpClientContext = new HttpClientContext();

        BasicCookieStore cookieStore = new BasicCookieStore();

        httpClientContext.setCookieStore(cookieStore);
    }

    // Inits whole inventory with only CS:GO items

    private void initInventory(String steamBeautyID) throws Exception {
        // Change SteamID
        String inventoryGetURI = "http://steamcommunity.com/id/" + steamBeautyID + "/inventory/json/730/2";

        String response = null;

        try {
            response = requestor.getAnswer(Requestor.query_type.GET, inventoryGetURI, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GetInventory botInventory = gsonEntity.fromJson(response, GetInventory.class);

        HashMap<Pair<String, String>, Pair<Integer, List<String>>> repeatCounter = new HashMap<Pair<String, String>, Pair<Integer, List<String>>>();

        for (Map.Entry<String, HashMap<String, String>> item : botInventory.rgInventory.entrySet()) {

            //class id + instance id
            Pair<String, String> tmp;

            String classid = null;
            String instanceid = null;
            String assetid = null;

            for (Map.Entry<String, String> innerItem : item.getValue().entrySet()) {
                if (innerItem.getKey().equals("classid")) {
                    classid = innerItem.getValue();
                }

                if (innerItem.getKey().equals("instanceid")) {
                    instanceid = innerItem.getValue();
                }
                if (innerItem.getKey().equals(("id"))) {
                    assetid = innerItem.getValue();
                }
            }

            tmp = new Pair(classid, instanceid);

            if (!repeatCounter.containsKey(tmp)) {
                //repeatCounter.put(tmp, 1);
                ArrayList<String> toPaste = new ArrayList<String>();
                toPaste.add(assetid);

                repeatCounter.put(tmp, new Pair(1, toPaste));
            } else {
                repeatCounter.get(tmp).getValue().add(assetid);
                repeatCounter.put(tmp, new Pair(new Integer(repeatCounter.get(tmp).getKey().intValue() + 1), repeatCounter.get(tmp).getValue()));
            }
        }

        List<Object> items = new ArrayList<Object>();

        for (Map.Entry<String, HashMap<String, Object>> item : botInventory.rgDescriptions.entrySet()) {
            String classAndInstance = item.getKey();
            String class_id = classAndInstance.split("_")[0];
            String instance_id = classAndInstance.split("_")[1];

            Pair<String, String> pair = new Pair(class_id, instance_id);

            int counter = repeatCounter.get(pair).getKey();
            List<String> assetids = repeatCounter.get(pair).getValue();


            String appid = null;
            String marketHashName = null;

            for (Map.Entry<String, Object> innerItem : item.getValue().entrySet()) {

                if (innerItem.getKey().equals("appid")) {
                    appid = innerItem.getValue().toString();
                }

                if (innerItem.getKey().equals("market_hash_name")) {
                    marketHashName = innerItem.getValue().toString();
                }
                if (innerItem.getKey().equals("tradable") && innerItem.getValue().equals(0.0)) {
                    appid = null;
                    marketHashName = null;
                    break;
                }
                if (innerItem.getKey().equals("marketable") && innerItem.getValue().equals(0.0)) {
                    appid = null;
                    marketHashName = null;
                    break;
                }
            }

            if (marketHashName != null && appid != null) {
                for (int i = 0; i < counter; ++i) {
                    ItemDesription curItem = new ItemDesription(marketHashName, appid, assetids.get(i));
                    items.add(curItem);
                }
            }
        }

        wholeInventory = new Inventory(items, Inventory.typeOfOperation.outcomingTradeoffer);
    }

    protected void addCookie(String login, String value, boolean secure) {
        BasicClientCookie cookie = new BasicClientCookie(login, value);

        cookie.setDomain("steamcommunity.com");
        cookie.setVersion(0);
        cookie.setPath("/");
        cookie.setSecure(secure);

        httpClientContext.getCookieStore().addCookie(cookie);
    }

    public class ItemDesription {
        String marketHashName;
        String appId;
        String assetId;

        public ItemDesription(String marketHashName, String appId, String assetId) {
            this.marketHashName = marketHashName;
            this.appId = appId;
            this.assetId = assetId;
        }

        ItemDesription() {
        }
    }

    private class parsedJSON {
        public String classid;
        public String instanceid;
        public String market_hash_name;
        public String appid;
    }

    private class GetInventory {
        public boolean more;
        public boolean more_start;
        public int[] rgCurrency;
        public HashMap<String, HashMap<String, Object>> rgDescriptions;
        public HashMap<String, HashMap<String, String>> rgInventory;
        public boolean success;
    }

    private class GetRSAKey {
        public boolean success;
        public String publickey_mod;
        public String publickey_exp;
        public String timestamp;
    }

    private class SteamResult {
        public boolean success;
        public String message;
        public boolean captcha_needed;
        public String captcha_gid;
        public boolean emailauth_needed;
        public String emailsteamid;
        public HashMap<String, String> transfer_parameters;
        String transfer_url;
    }

    private class TwoFactorAuthData
    {
        public String shared_secret;
        public String identity_secret;
        public String secret_1;
        public String server_time;
    }

    private class SteamServerInfo
    {
        public int servertime;
        public String servertimestring;
    }

    // Returns current server time in milleseconds from 1970

    public String currentServerTime() throws IOException {
        String serverInfoURI = "http://api.steampowered.com/SteamWebAPIUtil/GetServerInfo/v0001";
        String response = requestor.getAnswer(Requestor.query_type.GET, serverInfoURI, null);
        SteamServerInfo info = gsonEntity.fromJson(response, SteamServerInfo.class);

        int servertime = info.servertime / 1000;

        String toReturn = new String(String.valueOf(servertime));

        return toReturn;
    }

    public void steamLogin(String username, String password) throws Exception {
        Scanner test = new Scanner(new File("steamAuth.txt"));
        String loginInfo = test.nextLine();
        String headerName = loginInfo.split("=")[0];
        String cookieValue = loginInfo.split("=")[1];
        BotUser.currentUser.addCookie(headerName, cookieValue, false);

        initAuthData();

        Scanner scanner = new Scanner(System.in);

        String loginURI = "https://steamcommunity.com/login/getrsakey";

        List<NameValuePair> getRSAKeyParams = new ArrayList<NameValuePair>();

        getRSAKeyParams.add(new BasicNameValuePair("username", username));

        String response = null;

        try {
            response = requestor.getAnswer(Requestor.query_type.POST, loginURI, getRSAKeyParams);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GetRSAKey rsaJSON = gsonEntity.fromJson(response, GetRSAKey.class);

        if (rsaJSON.success == false)
            throw new Exception("Could not get RSA Key");

        BigInteger m = new BigInteger(rsaJSON.publickey_mod, 16);
        BigInteger e = new BigInteger(rsaJSON.publickey_exp, 16);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey key = keyFactory.generatePublic(spec);

        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.ENCRYPT_MODE, key);

        byte[] encodedPassword = rsa.doFinal(password.getBytes("ASCII"));
        String encryptedBase64Password = DatatypeConverter.printBase64Binary(encodedPassword);

        getRSAKeyParams = null;

        // We need current time in specified format to generate 2FA SteamGuard code

        long timeOffset = (new Date().getTime())/1000;


        List<NameValuePair> loginParams = new ArrayList<NameValuePair>();

        // emailauth=&loginfriendlyname=&captchagid=-1&captcha_text=&emailsteamid=&rsatimestamp=209027900000&remember_login=false

        loginParams.add(new BasicNameValuePair("password", encryptedBase64Password));
        loginParams.add(new BasicNameValuePair("username", username));


        loginParams.add(new BasicNameValuePair("emailauth", ""));
        loginParams.add(new BasicNameValuePair("emailsteamid", ""));
        loginParams.add(new BasicNameValuePair("captchagid", "-1"));
        loginParams.add(new BasicNameValuePair("captcha_text", ""));
        loginParams.add(new BasicNameValuePair("loginfriendlyname", ""));

        loginParams.add(new BasicNameValuePair("rsatimestamp", rsaJSON.timestamp));
        loginParams.add(new BasicNameValuePair("remember_login", "false"));

        HttpRequest webRequest = requestor.createNewRequest(Requestor.query_type.GET, "https://steamcommunity.com/", null);
        HttpResponse webResponse = null;
        webResponse = BotUser.currentUser.httpClient.execute((HttpUriRequest) webRequest, BotUser.currentUser.httpClientContext);

        Header[] array = webResponse.getAllHeaders();

        for (Header header : array) {
            if (header.getName().equals("Set-Cookie")) {
                String[] pairs = header.getValue().split(";");

                String[] steamLogin = pairs[0].split("=");
                String[] path = pairs[1].split("=");

                addCookie(steamLogin[0], steamLogin[1], false);
            }
        }

        HttpEntity enty = webResponse.getEntity();
        if (enty != null)
            enty.consumeContent();


        webRequest = requestor.createNewRequest(Requestor.query_type.POST, "https://steamcommunity.com/login/dologin/", loginParams);
        webResponse = null;
        webResponse = BotUser.currentUser.httpClient.execute((HttpUriRequest) webRequest, BotUser.currentUser.httpClientContext);

        array = webResponse.getAllHeaders();

        for (Header header : array) {
            if (header.getName().equals("Set-Cookie")) {
                String[] pairs = header.getValue().split(";");

                String[] steamLogin = pairs[0].split("=");
                String[] path = pairs[1].split("=");
                boolean secure;


                if (steamLogin[0].equals("steamLoginSecure")) {
                    secure = true;
                } else {
                    secure = false;
                }

                addCookie(steamLogin[0], steamLogin[1], secure);
            }
        }

        SteamResult loginResult = gsonEntity.fromJson(new InputStreamReader(webResponse.getEntity().getContent()), SteamResult.class);

        String steamGuardText = "";
        String steamGuardId = "";

        while (loginResult.captcha_needed == true || loginResult.emailauth_needed == true) {
            System.out.println("SteamWeb: Logging in.......");

            boolean captcha = loginResult.captcha_needed;
            boolean steamGuard = loginResult.emailauth_needed;

            String time = rsaJSON.timestamp;
            String capGID = loginResult.captcha_gid;

            loginParams = new ArrayList<NameValuePair>();
            loginParams.add(new BasicNameValuePair("password", encryptedBase64Password));
            loginParams.add(new BasicNameValuePair("username", username));

            String capText = "";
            if (captcha) {
                System.out.println("SteamWeb: Captcha needed.");

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI("https://steamcommunity.com/public/captcha.php?gid=" + loginResult.captcha_gid));
                } else {
                    System.out.println("https://steamcommunity.com/public/captcha.php?gid=" + loginResult.captcha_gid);
                }
                System.out.println("SteamWeb: Type the captcha:");
                capText = scanner.nextLine();
            }

            loginParams.add(new BasicNameValuePair("captchagid", captcha ? capGID : "-1"));
            loginParams.add(new BasicNameValuePair("captcha_text", captcha ? capText : ""));


            if (steamGuard) {
                System.out.println("SteamWeb: SteamGuard is needed.");
                System.out.println("SteamWeb: Type the code:");
                steamGuardText = scanner.nextLine();
                steamGuardId = loginResult.emailsteamid;
            }

            loginParams.add(new BasicNameValuePair("emailauth", steamGuardText));
            loginParams.add(new BasicNameValuePair("emailsteamid", steamGuardId));

            loginParams.add(new BasicNameValuePair("rsatimestamp", time));

            webRequest = requestor.createNewRequest(Requestor.query_type.POST, "https://steamcommunity.com/login/dologin/", loginParams);

            webResponse = BotUser.currentUser.httpClient.execute((HttpUriRequest) webRequest, BotUser.currentUser.httpClientContext);

            loginResult = gsonEntity.fromJson(new InputStreamReader(webResponse.getEntity().getContent()), SteamResult.class);
        }

        if (loginResult.success) {
            initInventory("vov4iktr");

           /* loginParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> stringStringEntry : loginResult.transfer_parameters.entrySet()) {
                Map.Entry pairs = (Map.Entry) stringStringEntry;
                loginParams.add(new BasicNameValuePair((String) pairs.getKey(), (String) pairs.getValue()));
            }
            requestor.createNewRequest(Requestor.query_type.POST, loginResult.transfer_url, loginParams);*/

            System.out.println("Logged in successfully");
        } else
            throw new Exception("SteamWeb Error: " + loginResult.message);
    }

    private void initAuthData() {
        try (BufferedReader br = new BufferedReader(new FileReader("2faData.txt"))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");

                if(parts[0].equals("shared_secret"))
                {
                    data2fa.shared_secret = parts[1];
                }
                else if(parts[0].equals("secret_1"))
                {
                    data2fa.secret_1 = parts[1];
                }
                else if(parts[0].equals("identity_secret"))
                {
                    data2fa.identity_secret = parts[1];
                }
                else if(parts[0].equals("server_time"))
                {
                    data2fa.server_time = parts[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TradeOffer[] getIncomingTradeOffers() throws Exception {
        Document document = Jsoup.parse(requestor.getAnswer(Requestor.query_type.GET, "http://steamcommunity.com/my/tradeoffers", null));
        Elements tradeOfferElements = document.getElementsByClass("tradeoffer");

        ArrayList<TradeOffer> offers = new ArrayList<TradeOffer>();
        System.out.println("FOR GITHUB");

        for (Element tradeOfferElement : tradeOfferElements) {

            int id = Integer.parseInt(tradeOfferElement.id().substring(13)); // strip off tradeofferid_

            List<Object> itemEconomyData = new ArrayList<Object>();
            Elements tradeItems = tradeOfferElement.getElementsByClass("trade_item");

            for (Element element : tradeItems) {
                itemEconomyData.add(element.attr("data-economy-item"));   // appID/contextID/itemID/partnerID
            }

            boolean active = tradeOfferElement.getElementsByClass("tradeoffer_items_ctn").get(0).hasClass("active");
            TradeOffer tradeOffer = null;

            try {
                tradeOffer = new TradeOffer(id, active, new ArrayList<Object>(), itemEconomyData);
                tradeOffer.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }

            offers.add(tradeOffer);
        }

        return offers.toArray(new TradeOffer[offers.size()]);
    }

}