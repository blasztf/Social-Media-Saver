package com.blaszt.socialmediasaver2.plugin.helper.json;

public abstract class AbsJObject<T,P> extends JElement<T,P> {
    private String mMember;

    public AbsJObject(T element) {
        super(element);
    }

    protected String getMember() {
        return mMember;
    }

    public JElement<T,P> get(String member) {
        mMember = member;
        return this;
    }

    public abstract boolean has(String member);

    public abstract int size();
}
