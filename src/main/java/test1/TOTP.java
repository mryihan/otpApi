package test1;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.TimeZone;

public class TOTP {

    //public TOTP() {}
    private static String otp = "a";
    private static String secretkey = null;

    public String getSecretkey() {
        return secretkey;
    }

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a
     * Hashed Message Authentication Code with the crypto hash algorithm as a
     * parameter.
     *
     * @param crypto the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param keyBytes the bytes to use for the HMAC key
     * @param text the message or text to be authenticated.
     */
    public static byte[] hmac_sha1(String crypto, byte[] keyBytes,
            byte[] text) {
        try {
            Mac hmac; // declare an instance of mac as hmac
            //call an instance of mc algorithm, take in fixed string input such as 
            //HmacSHA1,HmacSHA256,HmacSHA384,HmacSHA512
            hmac = Mac.getInstance(crypto);
            //construct a secretkey from a byte array , uses "RAW" as encoding format
            SecretKeySpec macKey
                    = new SecretKeySpec(keyBytes, "RAW");

            //initialize the mac object with the given key
            hmac.init(macKey);

            //Processes the given array of bytes and finishes the MAC operation
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    //convert to byte array
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * This method converts HEX string to Byte[]
     *
     * @param hex the HEX string, in number
     *
     * @return A byte array
     */
    public static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bArray[i + 1];
        }
        return ret;
    }

    public static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    /**
     * This method generates an TOTP value for the given set of parameters.
     *
     * @param key the shared secret, HEX encoded
     * @param time a value that reflects a time
     * @param returnDigits number of digits to return
     *
     * @return A numeric String in base 10 that includes
     * {@link truncationDigits} digits
     */
    public static String generateTOTP(String key,
            String time,
            String returnDigits){
        return generateTOTP(key, time, returnDigits, "HmacSHA1");
    }
    
    public static String generateTOTP512(String key,
            String time,
            String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA512");
    }

    /**
     * This method generates an TOTP value for the given set of parameters.
     *
     * @param key the shared secret, HEX encoded
     * @param time a value that reflects a time
     * @param returnDigits number of digits to return
     * @param crypto the crypto function to use
     *
     * @return A numeric String in base 10 that includes
     * {@link truncationDigits} digits
     */
    public static String generateTOTP(String key,
            String time,
            String returnDigits,
            String crypto) {
        // convert the string of digits to a int
        int codeDigits = Integer.decode(returnDigits).intValue();
        String result = null;
        byte[] hash;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Complaint with base RFC 4226 (HOTP)
        while (time.length() < 16) {
            time = "0" + time;
        }

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);

        // Adding one byte to get the right conversion
        //byte[] k = hexStr2Bytes(key);
        byte[] k = hexStringToByteArray(key);
        /*
        System.out.print("k is:");
        System.out.println(k);
        System.out.print("Converting back to string is:");
        String tests=new String(k);
        System.out.println(tests);
         */

        hash = hmac_sha1(crypto, k, msg);
        //System.out.print("Hash is: ");
        //System.out.println(hash);

        // grab the last byte, the value of the lower 4 bits to use as offset value to determine starting point of slice
        int offset = hash[hash.length - 1] & 0xf;
        //System.out.println("offset is:");
        //System.out.printf("0x%02X", hash[offset]);
        //System.out.println("\n");

        //truncate the hash message [bytes] 
        int binary
                = ((hash[offset] & 0x7f) << 24)
                | //ox7f:127 in decimal value
                ((hash[offset + 1] & 0xff) << 16)
                | //0xff:255 in decimal value
                ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        //System.out.print("Binary is: ");
        //System.out.println(Integer.toBinaryString(binary).length());
        //System.out.println("end");
        // take the modulus to ensure that the otp is within the codeDigits range
        //digits_power is an array up to 8 value
        int otp = binary % DIGITS_POWER[codeDigits];

        result = Integer.toString(otp);   //convert the int otp into result string

        while (result.length() < codeDigits) {
            result = "0" + result;
        }

        return result;
    }

    public String generateSecretKey() {
        String keylist = "1234567890abcdefghijklmnopqrstuvwxyz";

        //set 64 bytes otp
        char[] randomS = new char[64];
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < randomS.length; i++) {
            randomS[i] = keylist.charAt(random.nextInt(keylist.length()));
        }
        String skey = new String(randomS);
        return skey;
    }

    public String OTP(String secretKey) {
        //set secret key, in this instance its a random string, 64 byte/512bit for hmac512
        //String secretkey = "3132333435363738393031323334353637383930" +
        //"313233343536373839303132";
        //randomly generate a secret key

        secretkey = secretKey;
        System.out.println("key being used:" + secretkey);
        //randomly generate a secretkey
        //System.out.print("Byte length: ");
        //System.out.println(secretkey.getBytes().length);
        long T0 = 0;
        //60s in millisec
        //timestep or how long the token take to expire
        long X = 60000;

        long testTime = System.currentTimeMillis();
        String steps = "0";

        try {

            //take current time - epoch
            long T = (testTime - T0) / X;

            //System.out.println(T);
            //convert the time decimal value into hexadecimal value in string format
            steps = Long.toHexString(T).toUpperCase();
            //ensure that steps is 16word long else add zero from the left
            while (steps.length() < 16) {
                steps = "0" + steps;
            }

            otp = generateTOTP(secretkey, steps, "5",
                    "HmacSHA512");

        } catch (final Exception e) {
            System.out.println("Error : " + e);
        }

        return otp;
    }

    public String OTP() {
        secretkey = generateSecretKey();

        return OTP(secretkey);
    }

    //test function
    public static boolean verifyOTP(String s) {
        boolean verify = false;
        if (s.equals(otp)) {
            verify = true;
        } else {
            verify = false;
        }
        return verify;
    }
    /*
    public static void main(String[] args) {
        System.out.println("Printing");
        System.out.println(OTP());
        System.out.println("End");
    }
     */

}
