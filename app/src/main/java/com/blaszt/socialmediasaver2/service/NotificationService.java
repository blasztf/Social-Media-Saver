package com.blaszt.socialmediasaver2.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import com.blaszt.socialmediasaver2.helper.ui.NotificationHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.NotificationTarget;

import java.util.concurrent.ExecutionException;

public abstract class NotificationService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationService(String name) {
        super(name);
    }

    protected NotificationTicket requestTicket() {
        return new NotificationTicket(this);
    }

    protected void giveTicket(NotificationTicket ticket) {
        NotificationHelper.notify(this, ticket.getId(), ticket.getNotification());
    }

    protected void shredTicket(NotificationTicket ticket) {
        NotificationHelper.cancel(this, ticket.tearApart());
    }

    public static class NotificationTicket {
        private int mId;
        private NotificationCompat.Builder mNotificationBuilder;
        private RequestManager mRequestPictureManager;

        private NotificationTicket() {

        }

        private NotificationTicket(NotificationService context) {
            mId = generateNotificationId();
            mNotificationBuilder = NotificationHelper.getBuilder(context);
            mRequestPictureManager = Glide.with(context);
        }

        private int generateNotificationId() {
            return (int) System.currentTimeMillis();
        }

        private Notification getNotification() {
            return mNotificationBuilder.build();
        }

        private int getId() {
            return mId;
        }

        private int tearApart() {
            int id = mId;
            mNotificationBuilder = null;
            mRequestPictureManager = null;
            mId = 0;

            return id;
        }

        public void setContent(String title, String text) {
            mNotificationBuilder
                    .setContentTitle(title)
                    .setContentText(text);
        }

        public void setProgress(int progress) {
            if (progress <= -1) {
                mNotificationBuilder.setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setProgress(0, 0, false)
                        .setAutoCancel(true)
                        .setOngoing(false);
            }
            else {
                mNotificationBuilder.setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setProgress(100, progress, false)
                        .setAutoCancel(false)
                        .setOngoing(true);
            }
        }

        public void setPicture(String picture) {
            FutureTarget<Bitmap> target = mRequestPictureManager
                    .asBitmap()
                    .load(picture)
                    .submit();

            try {
                mNotificationBuilder
                        .setStyle(new NotificationCompat
                                .BigPictureStyle()
                                .bigPicture(target.get()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
