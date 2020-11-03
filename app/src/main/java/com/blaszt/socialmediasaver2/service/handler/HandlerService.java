package com.blaszt.socialmediasaver2.service.handler;

import android.content.Intent;
import android.os.Binder;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.blaszt.socialmediasaver2.data.SMSContent;
import com.blaszt.socialmediasaver2.service.NotificationService;

abstract class HandlerService<R> extends NotificationService {
    private Handler<R> mHandler;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HandlerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action;

        if (intent != null) {
            action = (action = getAction()) != null ? action : "";
            if (action.equalsIgnoreCase(intent.getAction())) {
                retrieveHandler(intent);
                onHandle(intent);
            }
        }
    }

    private void retrieveHandler(Intent intent) {
        Messenger msg = intent.getParcelableExtra(SMSContent.Intent.EXTRA_HANDLER);
        mHandler = msg != null ? (Handler<R>) msg.getBinder() : null;
    }

    protected boolean shouldCallback() {
        return getHandler() != null;
    }

    Handler<R> getHandler() {
        return mHandler;
    }

    abstract String getAction();

    abstract void onHandle(@NonNull Intent intent);

    public static abstract class Handler<T> extends Binder {
        abstract void onHandled(T result);
    }
}
