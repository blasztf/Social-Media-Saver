package com.blaszt.socialmediasaver2.module.instagram.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class IUtil {

    private static Hash hashInstance;

    private static Generic genericInstance;

    public static synchronized Hash getHash() {
        if (hashInstance == null) {
            hashInstance = new Hash();
        }

        return hashInstance;
    }

    public static synchronized Generic getGeneric() {
        if (genericInstance == null) {
            genericInstance = new Generic();
        }

        return genericInstance;
    }

    static class Hash {

        static final String XLATE = "0123456789abcdef";

        private String md5(String string) {
            String codec = "md5";
            try {
                MessageDigest digest = MessageDigest.getInstance(codec);
                byte[] digestBytes = digest.digest(string.getBytes(Charset.forName("UTF-8")));
                return hexlate(digestBytes, digestBytes.length);
            } catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException(codec + " codec not available");
            }
        }

        private String hexlate(byte[] bytes, int initialCount) {
            if (bytes == null) {
                return "";
            }

            int count = Math.min(initialCount, bytes.length);
            char[] chars = new char[count * 2];

            for (int i = 0; i < count; ++i) {
                int val = bytes[i];
                if (val < 0) {
                    val += 256;
                }
                chars[(2 * i)] = XLATE.charAt(val / 16);
                chars[(2 * i + 1)] = XLATE.charAt(val % 16);
            }

            return new String(chars);
        }

        public String generateHash(String key, String string) {
            SecretKeySpec object = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init((Key) object);
                byte[] byteArray = mac.doFinal(string.getBytes("UTF-8"));
                return new String(hexEncode(byteArray), "ISO-8859-1");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public String generateDeviceId(String username, String password) {
            String seed = md5(username + password);
            String volatileSeed = "12345";

            return "android-" + md5(seed + volatileSeed).substring(0, 16);
        }

        public String generateSignature(String payload) {
            String parsedData = "";

            try {
                parsedData = URLEncoder.encode(payload, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String signedBody = generateHash(IConstant.API_KEY, payload);

            return "ig_sig_key_version=" + IConstant.API_KEY_VERSION + "&signed_body=" + signedBody + '.'
                    + parsedData;

        }



        private byte[] hexEncode(byte[] num) {
            String a = "";

            char[] hexDigits;

            for (int i = 0; i < num.length; i+=2) {
                hexDigits = new char[2];
                hexDigits[0] = Character.forDigit((num[i] >> 4) & 0xF, 16);
                hexDigits[1] = Character.forDigit((num[i] & 0xF), 16);

                a += new String(hexDigits);
            }

            return a.getBytes();
        }
    }

    static class Generic {

        public String generateUuid(boolean dash) {

            String uuid = UUID.randomUUID().toString();

            if(dash) {
                return uuid;
            }

            return uuid.replaceAll("-", "");

        }
    }
}
