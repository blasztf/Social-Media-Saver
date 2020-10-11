package com.blaszt.socialmediasaver2.plugin.helper.json;

public abstract class AbsJArray<P,T> extends JElement<P,T> {
    private int mIndex;

    public AbsJArray(P element) {
        super(element);
    }

    protected int getIndex() {
        return mIndex;
    }

    public JElement<P,T> get(int index) {
        mIndex = index;
        return this;
    }

    public abstract int size();
}
