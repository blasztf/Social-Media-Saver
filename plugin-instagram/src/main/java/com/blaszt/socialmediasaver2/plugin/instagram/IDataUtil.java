package com.blaszt.socialmediasaver2.plugin.instagram;

public class IDataUtil {

    public static String getPk(String jsonResponse) {
        if (jsonResponse == null) return null;

        String key = "\"pk\":";
        int keyPos = jsonResponse.indexOf(key) + key.length();
        int endPos = jsonResponse.indexOf(", \"", keyPos);

        return jsonResponse.substring(keyPos, endPos).trim();
    }

    public static String getStatus(String jsonResponse) {
        if (jsonResponse == null) return null;

        String key = "\"status\":";
        int keyPos = jsonResponse.indexOf(key) + key.length();
        int endPos = jsonResponse.indexOf("\"}", keyPos) + 1;

        return jsonResponse.substring(keyPos, endPos).trim().replaceAll("\"", "");
    }

}
