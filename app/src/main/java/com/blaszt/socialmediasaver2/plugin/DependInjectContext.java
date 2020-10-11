package com.blaszt.socialmediasaver2.plugin;

import android.content.Context;

import java.lang.ref.WeakReference;

public abstract class DependInjectContext {
    private WeakReference<Context> mContext;

    protected DependInjectContext(Context context) {
        mContext = new WeakReference<>(context);
    }

    protected Context getContext() {
        return mContext.get();
    }

    protected void releaseContext() {
        mContext.clear();
        mContext = null;
    }
}
