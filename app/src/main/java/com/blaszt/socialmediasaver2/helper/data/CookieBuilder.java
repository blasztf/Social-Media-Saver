package com.blaszt.socialmediasaver2.helper.data;

public class CookieBuilder {
    private StringBuilder mBuilder;

    public CookieBuilder() {
        mBuilder = new StringBuilder();
    }

    public CookieBuilder append(String name, String value) {
        mBuilder.append(name).append("=").append(value).append(";");
        return this;
    }

    @Override
    public String toString() {
        return mBuilder.replace(mBuilder.length() - 1, mBuilder.length(), "").toString();
    }
}
