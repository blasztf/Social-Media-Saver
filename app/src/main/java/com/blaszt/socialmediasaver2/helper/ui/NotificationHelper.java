package com.blaszt.socialmediasaver2.helper.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.blaszt.socialmediasaver2.R;

public class NotificationHelper {
    private static NotificationManager getManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationCompat.Builder getBuilder(Context context) {
        return new NotificationCompat.Builder(context);
    }

    public static NotificationCompat.Builder getBuilder(Context context, String title, String text) {
        return getBuilder(context)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(title != null ? title : context.getString(R.string.app_name))
                .setContentText(text);
    }

    public static void notify(Context context, int id, Notification notification) {
        getManager(context).notify(id, notification);
    }

    public static void notify(Context context, int id, NotificationCompat.Builder notification) {
        getManager(context).notify(id, notification.build());
    }

    public static void cancel(Context context, int id) {
        getManager(context).cancel(id);
    }

    public static void cancelAll(Context context) {
        getManager(context).cancelAll();
    }
}
