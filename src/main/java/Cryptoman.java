import org.apache.commons.codec.binary.Hex;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SignatureException;
import java.util.Arrays;


public class Cryptoman {

    // Returns 5 char long string by using TOTP algorithm

    public String getSteamGuardCode(String shared_secret, String timeOffset)
    {
        byte[] hmacRaw = null;

        byte[] timeOffsetRaw = transformToBytes(timeOffset);

        try {
             hmacRaw = HMAC_SHA1_encode(shared_secret, ByteBuffer.wrap(timeOffsetRaw).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        int start = hmacRaw[19] & 0x0F;
        byte[] reworkedByteArray = Arrays.copyOfRange(hmacRaw, start, start + 4);

        ByteBuffer wrapper = ByteBuffer.wrap(reworkedByteArray);

        int numToCompare = wrapper.getInt();

        int fullcode = numToCompare & 0x7fffffff;

        String codingChars = new String("23456789BCDFGHJKMNPQRTVWXY");

        char[] finalCode = new char[5];

        for(int i = 0; i < finalCode.length; ++i)
        {
            finalCode[i] = codingChars.charAt(fullcode % codingChars.length());
            fullcode /= codingChars.length();
        }

        return new String(finalCode);
    }

    private byte[] HMAC_SHA1_encode(String key, String message) throws Exception {

        SecretKeySpec keySpec = new SecretKeySpec(
                key.getBytes(),
                "HmacSHA1");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(message.getBytes());

        return rawHmac;
    }

    private byte[] transformToBytes(String timeOffset)
    {
        byte[] finalArray = new byte[8];

        for(int i = 0; i < 4; ++i)
        {
            finalArray[i] = 0;
        }

        // We need to divide it by 30
        // SteamGuard gives new code each 30 secs

        int dividedTime = new Integer(timeOffset) / 30;

        byte[] tmpArray = ByteBuffer.allocate(4).putInt(dividedTime).array();

        for(int i = 0; i < 4; ++i)
        {
            finalArray[4 + i] = tmpArray[i];            // At this moment Vovan went full retard
        }

        return finalArray;
    }

}
