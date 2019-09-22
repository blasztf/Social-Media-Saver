package com.blaszt.modulelinker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public abstract class Base implements Validator, MediaFinder {
//    private byte[] cacheImage;

    public abstract String getBaseDir();
    public abstract String getName();
    protected abstract String getEncodedImage();

    public String getLogo() {
//        if (cacheImage == null) {
//            cacheImage = Base64.decode(getEncodedImage(), Base64.DEFAULT);
//        }
//        return cacheImage;
        return getEncodedImage();
    };
    protected boolean isValid(String url) { return false; }
}
