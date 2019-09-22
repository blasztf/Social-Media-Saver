package com.blaszt.socialmediasaver2.helper.data;

import java.util.HashMap;

public class MemeType {
    private static MemeType instance;

    private String type;
    private String subtype;

    public static synchronized MemeType with(String mimeType) {
        if (instance == null) {
            instance = new MemeType();
        }
        instance.setMimeType(mimeType);
        return instance;
    }

    private MemeType() {
    }

    private void setMimeType(String mimeType) {
        if (mimeType == null) throw new MimeTypeNotValidException();
        String[] types = mimeType.split("/");
        if (types.length != 2) throw new MimeTypeNotValidException();
        if (!types[0].matches("[a-z]+")) throw new MimeTypeNotValidException();
        if (!types[1].matches("[a-z0-9+.*\\-]")) throw new MimeTypeNotValidException();
        type = types[0];
        subtype = types[1];
    }

    private HashMap<String, String> getExtensionMap() {
        HashMap<String, String> extensionMap = new HashMap<>();
        extensionMap.put("x-mpegURL", ".m3u8");
        extensionMap.put("vnd.apple.mpegurl", ".m3u8");
        extensionMap.put("mpegurl", ".m3u");
        extensionMap.put("mp2t", ".ts");
        extensionMap.put("jpeg", ".jpg");
        extensionMap.put("jpg", ".jpg"); // ghost
        extensionMap.put("png", ".png");
        return extensionMap;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String guessExtension() {
        String extension = getExtensionMap().get(subtype);
        if (extension == null) extension = ".file";
        return extension;
    }

    private class MimeTypeNotValidException extends RuntimeException {

    }
}
