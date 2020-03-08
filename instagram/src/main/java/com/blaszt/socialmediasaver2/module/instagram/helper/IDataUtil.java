package com.blaszt.socialmediasaver2.module.instagram.helper;

public class IDataUtil {

    public static String getPk(String jsonResponse) {
        String key = "\"pk\":";
        int keyPos = jsonResponse.indexOf(key) + key.length();
        int endPos = jsonResponse.indexOf(", \"", keyPos);

        return jsonResponse.substring(keyPos, endPos).trim();
    }

    public static String getStatus(String jsonResponse) {
        String key = "\"status\":";
        int keyPos = jsonResponse.indexOf(key) + key.length();
        int endPos = jsonResponse.indexOf("\"}", keyPos) + 1;

        return jsonResponse.substring(keyPos, endPos).trim().replaceAll("\"", "");
    }

}
