package com.blaszt.socialmediasaver2.module_base;

public abstract class Base implements Validator, MediaFinder {
//    private byte[] cacheImage;

    private Responder mResponder;

    public Base() {
        Responder.inject(this);
    }

    public abstract String getBaseDir();
    public abstract String getName();
    protected abstract String getEncodedImage();

    public String getLogo() {
//        if (cacheImage == null) {
//            cacheImage = Base64.decode(getEncodedImage(), Base64.DEFAULT);
//        }
//        return cacheImage;
        return getEncodedImage();
    }

    @Override
    public String[] findMediaURL(String url) throws ModuleNotInjected {
        if (mResponder == null) throw new ModuleNotInjected();
        return new String[0];
    }

    void setResponder(Responder responder) {
        mResponder = responder;
    }

    protected Responder getResponder() {
        return mResponder;
    }

    protected boolean isValid(String url) { return false; }
}
