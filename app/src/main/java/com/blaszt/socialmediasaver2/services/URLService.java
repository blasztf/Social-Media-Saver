package com.blaszt.socialmediasaver2.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.helper.ui.NotificationHelper;
import com.blaszt.socialmediasaver2.main.MainActivity;

import java.util.Objects;

public final class URLService extends Service implements URLServiceListener {
    private static final int NOTIFICATION_ID = 8;

    private ClipboardManager clipboardManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupClipboardService();
        setupNotificationForeground();
        validateIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearClipboardService();
    }

    @Override
    public int onBuildNotificationForeground(NotificationCompat.Builder notification) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.remote_views);
        remoteViews.setTextViewText(R.id.title, "Social Media Saver has been started!");
        remoteViews.setTextViewText(R.id.text, "Tap to see downloaded photos / videos");
        remoteViews.setOnClickPendingIntent(R.id.close, requestStopSelf());

        notification
                .setSmallIcon(R.drawable.ic_notification_small)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContent(remoteViews)
                .setContentIntent(goToMainApplication());

        return NOTIFICATION_ID;
    }

    @Override
    public void setupNotificationForeground() {
        NotificationCompat.Builder notification = NotificationHelper.getBuilder(this);
        int notificationId = onBuildNotificationForeground(notification);
        startForeground(notificationId, notification.build());
    }

    @Override
    public void setupClipboardService() {
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener);
    }

    @Override
    public void clearClipboardService() {
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(primaryClipChangedListener);
            primaryClipChangedListener = null;
            clipboardManager = null;
        }
    }

    @Override
    public void onURLCopied(String url) {
        handle(url);
    }

    @Override
    public void handle(String url) {
        Intent intent = new Intent(URLService.this, URLHandler.class);
        intent.setAction(URLHandler.ACTION_HANDLE_URL);
        intent.putExtra(URLHandler.EXTRA_URL, url);
        startService(intent);
    }

    private PendingIntent goToMainApplication() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ServiceBroadcast.ACTION_GO_TO_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent requestStopSelf() {
        Intent intent = new Intent(this, URLService.class);
        intent.setAction(ServiceBroadcast.ACTION_CLOSE_URL_SERVICE);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void validateIntent(Intent intent) {
        if (ServiceBroadcast.ACTION_CLOSE_URL_SERVICE.equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
        }
    }

    private ClipboardManager.OnPrimaryClipChangedListener primaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            if (clipboardManager != null) {
                if (clipboardManager.hasPrimaryClip()) {
                    ClipData clipData = clipboardManager.getPrimaryClip();
                    String url = clipData.getItemAt(0).getText().toString();

                    onURLCopied(url);
                }
            }
        }
    };

    public static final class ServiceBroadcast extends BroadcastReceiver {
        private static final String ACTION_CLOSE_URL_SERVICE = ServiceBroadcast.class.getName() + ".ACTION_CLOSE_URL_SERVICE";
        private static final String ACTION_GO_TO_MAIN = ServiceBroadcast.class.getName() + ".ACTION_GO_TO_MAIN ";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (context instanceof URLService) {
                if (ACTION_CLOSE_URL_SERVICE.equals(action)) {
                    intent = new Intent(context, URLService.class);
                    context.stopService(intent);
                } else if (ACTION_GO_TO_MAIN.equals(action)) {
                    intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }
    }
}
