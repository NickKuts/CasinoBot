import com.google.gson.Gson;

import com.sun.org.apache.xpath.internal.SourceTree;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public static final Requestor requestor = new Requestor();

    public static final BotUser currentUser = new BotUser();

    BotUser()
    {
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        gsonEntity = new Gson();

        httpClientContext = new HttpClientContext();

        BasicCookieStore cookieStore = new BasicCookieStore();

        httpClientContext.setCookieStore(cookieStore);
    }

    protected void addCookie(String login, String value, boolean secure)
    {
        BasicClientCookie cookie = new BasicClientCookie(login, value);

        cookie.setDomain("steamcommunity.com");
        cookie.setVersion(0);
        cookie.setPath("/");
        cookie.setSecure(secure);

        httpClientContext.getCookieStore().addCookie(cookie);
    }

    private class GetRSAKey
    {
        public boolean success;
        public String publickey_mod;
        public String publickey_exp;
        public String timestamp;
    }

    private class SteamResult
    {
        public boolean success;
        public String message;
        public boolean captcha_needed;
        public String captcha_gid;
        public boolean emailauth_needed;
        public String emailsteamid;
        public HashMap<String, String> transfer_parameters;
        String transfer_url;
    }

    public void steamLogin(String username, String password) throws Exception {
        Scanner scanner = new Scanner(System.in);

        String loginURI = "https://steamcommunity.com/login/getrsakey";

        List<NameValuePair> getRSAKeyParams = new ArrayList<NameValuePair>();

        getRSAKeyParams.add(new BasicNameValuePair("username", username));

        String response = null;

        BufferedReader machineAuthFile = new BufferedReader(new FileReader("SteamAuth.txt"));
        String[] steamAuth = machineAuthFile.readLine().split("=");
        BotUser.currentUser.addCookie(steamAuth[0], steamAuth[1], false);

        try {
            response = requestor.getAnswer(Requestor.query_type.POST, loginURI, getRSAKeyParams);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GetRSAKey rsaJSON = gsonEntity.fromJson(response, GetRSAKey.class);

        if(rsaJSON.success == false)
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

        for(Header header : array)
        {
            if(header.getName().equals("Set-Cookie"))
            {
                String[] pairs = header.getValue().split(";");

                String[] steamLogin = pairs[0].split("=");

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

        for(Header header : array)
        {
            if(header.getName().equals("Set-Cookie"))
            {
                String[] pairs = header.getValue().split(";");

                String[] steamLogin = pairs[0].split("=");
                boolean secure;


                if(steamLogin[0].equals("steamLoginSecure"))
                {
                    secure = true;
                }
                else
                {
                    secure = false;
                }

                addCookie(steamLogin[0], steamLogin[1], secure);
            }
        }

        SteamResult loginResult = gsonEntity.fromJson(new InputStreamReader(webResponse.getEntity().getContent()), SteamResult.class);

        String steamGuardText = "";
        String steamGuardId = "";

        while(loginResult.captcha_needed == true || loginResult.emailauth_needed == true)
        {
            System.out.println("SteamWeb: Logging in.......");

            boolean captcha = loginResult.captcha_needed;
            boolean steamGuard = loginResult.emailauth_needed;

            String time = rsaJSON.timestamp;
            String capGID = loginResult.captcha_gid;

            loginParams = new ArrayList<NameValuePair>();
            loginParams.add(new BasicNameValuePair("password", encryptedBase64Password));
            loginParams.add(new BasicNameValuePair("username", username));

            String capText = "";
            if(captcha)
            {
                System.out.println("SteamWeb: Captcha needed.");

                if(Desktop.isDesktopSupported())
                {
                    Desktop.getDesktop().browse(new URI("https://steamcommunity.com/public/captcha.php?gid=" + loginResult.captcha_gid));
                } else {
                    System.out.println("https://steamcommunity.com/public/captcha.php?gid=" + loginResult.captcha_gid);
                }
                System.out.println("SteamWeb: Type the captcha:");
                capText = scanner.nextLine();
            }

            loginParams.add(new BasicNameValuePair("captchagid", captcha ? capGID : "-1"));
            loginParams.add(new BasicNameValuePair("captcha_text", captcha ? capText : ""));



            if (steamGuard)
            {
                System.out.println("SteamWeb: SteamGuard is needed.");
                System.out.println("SteamWeb: Type the code:");
                steamGuardText = scanner.nextLine();
                steamGuardId   = loginResult.emailsteamid;
            }

            loginParams.add(new BasicNameValuePair("emailauth", steamGuardText));
            loginParams.add(new BasicNameValuePair("emailsteamid", steamGuardId));

            loginParams.add(new BasicNameValuePair("rsatimestamp", time));

            webRequest = requestor.createNewRequest(Requestor.query_type.POST, "https://steamcommunity.com/login/dologin/", loginParams);

            webResponse = BotUser.currentUser.httpClient.execute((HttpUriRequest) webRequest, BotUser.currentUser.httpClientContext);

           loginResult = gsonEntity.fromJson(new InputStreamReader(webResponse.getEntity().getContent()), SteamResult.class);
        }

        if (loginResult.success)
        {
            loginParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> stringStringEntry : loginResult.transfer_parameters.entrySet()) {
                Map.Entry pairs = (Map.Entry) stringStringEntry;
                loginParams.add(new BasicNameValuePair((String) pairs.getKey(), (String) pairs.getValue()));
            }
            requestor.createNewRequest(Requestor.query_type.POST, loginResult.transfer_url, loginParams);

            System.out.println("Logged in successfully");
        }
        else
            throw new Exception("SteamWeb Error: " + loginResult.message);
    }

    public TradeOffer[] getIncomingTradeOffers() throws Exception
    {
        Document document = Jsoup.parse(requestor.getAnswer(Requestor.query_type.GET, "http://steamcommunity.com/my/tradeoffers", null));
        Elements tradeOfferElements = document.getElementsByClass("tradeoffer");

        ArrayList<TradeOffer> offers = new ArrayList<TradeOffer>();

        for(Element tradeOfferElement : tradeOfferElements)
        {

            int id = Integer.parseInt(tradeOfferElement.id().substring(13)); // strip off tradeofferid_

            List<String> itemEconomyData = new ArrayList<String>();
            Elements tradeItems = tradeOfferElement.getElementsByClass("trade_item");

            for(Element element : tradeItems)
            {
                itemEconomyData.add(element.attr("data-economy-item"));   // appID/contextID/itemID/partnerID
            }

            boolean active = tradeOfferElement.getElementsByClass("tradeoffer_items_ctn").get(0).hasClass("active");
            TradeOffer tradeOffer = null;
            try {
                tradeOffer = new TradeOffer(id, active, new ArrayList<String>(), itemEconomyData);
                tradeOffer.decline();
            } catch (Exception e) {
                e.printStackTrace();
            }
            offers.add(tradeOffer);
        }

        return offers.toArray(new TradeOffer[offers.size()]);
    }

}