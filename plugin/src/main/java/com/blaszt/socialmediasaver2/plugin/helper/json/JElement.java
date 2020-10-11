package com.blaszt.socialmediasaver2.plugin.helper.json;

public abstract class JElement<T,P> {
    private T mElement;

    public JElement(T element) {
        mElement = element;
    }

    protected T getElement() {
        return mElement;
    }

    public abstract String asString();
    public abstract boolean asBoolean();
    public abstract double asDouble();
    public abstract int asInteger();
    public abstract AbsJObject<T,P> asJObject();
    public abstract AbsJArray<P,T> asJArray();
}
