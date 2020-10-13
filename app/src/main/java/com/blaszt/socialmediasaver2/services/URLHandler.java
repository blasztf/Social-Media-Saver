package com.blaszt.socialmediasaver2.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.blaszt.socialmediasaver2.helper.data.StringUtils;
import com.blaszt.socialmediasaver2.helper.ui.NotificationHelper;
import com.blaszt.socialmediasaver2.logger.CrashCocoExceptionHandler;
import com.blaszt.socialmediasaver2.main.GalleryFragment;
import com.blaszt.socialmediasaver2.plugin.ModPlugin;
import com.blaszt.socialmediasaver2.plugin.ModPluginEngine;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

public final class URLHandler extends IntentService implements URLHandlerListener {
    public static final String EXTRA_URL = URLHandler.class.getName() + ".EXTRA_URL";
    public static final String ACTION_HANDLE_URL = URLHandler.class.getName() + ".intent.action.HANDLE_URL";

    private int notificationId;
    private int statusNotificationId;

    private NotificationCompat.Builder notification;

    private ModPlugin module;

    public URLHandler() {
        super("URLHandler");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        URLHandlerUncaughtException uncaughtException = new URLHandlerUncaughtException(this);
        Thread.setDefaultUncaughtExceptionHandler(uncaughtException);

        String url;
        if (intent != null) {
            url = intent.getStringExtra(EXTRA_URL);
            if (url != null) {
                if (isURLValid(url)) {
                    onHandleURL(url);
                }
            }
        }

        Thread.setDefaultUncaughtExceptionHandler(uncaughtException.getDefaultHandler());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notification != null) {
            cancelProgress();
        }
    }

    @Override
    public void startNotification(String type) {
        long currentTimeMillis = System.currentTimeMillis();

        notificationId = (int) currentTimeMillis;
        notification = NotificationHelper.getBuilder(this, "Downloading...", String.format("Downloading %s", type))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setProgress(100, 0, false)
                .setAutoCancel(false)
                .setOngoing(true);

        NotificationHelper.notify(this, notificationId, notification);
    }

    @Override
    public void updateNotification(int progress) {
        notification.setProgress(100, progress, false);
        NotificationHelper.notify(this, notificationId, notification);
    }

    @Override
    public void endNotification(File media, String type) {
        boolean isSuccess = media != null;

        String title = isSuccess ? "Downloaded" : "Failed";
        String msg = StringUtils.toUpperCaseFirst(String.format(isSuccess ? "%s has been downloaded!" : "Failed to download %s", type));

        if (isSuccess) {
            Intent intent = new Intent(URLHandler.this, GalleryFragment.class);
            PendingIntent pending = PendingIntent.getActivity(URLHandler.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            notification.setContentIntent(pending);
        }

        notification
                .setAutoCancel(true)
                .setProgress(0, 0, false)
                .setContentTitle(title)
                .setOngoing(false)
                .setContentText(msg);

        NotificationHelper.notify(this, notificationId, notification);
        notification = null;
    }

    @Override
    public File onHandleURL(String url) {
        File media = null;
        String[] mediaURLs;

        postStatusNotification("Fetching media urls...");
        mediaURLs = module.use(this).getMediaURLs(url);
        postStatusNotification(String.format(Locale.getDefault(), "Total media urls : %d", mediaURLs.length));
        if (mediaURLs.length > 0) {
//            Log.d("MEDIA_URL_SIZE", "size : " + mediaURLs.length);
            for (String mediaURL : mediaURLs) {
//                Log.d("MEDIA_URL", mediaURL);
                String type = determineType(mediaURL);
                startNotification(type);
                media = downloadMedia(mediaURL);
                endNotification(media, type);
            }

            postStatusNotification(null);
        }

        return media;
    }

    private void postStatusNotification(String text) {

//        toastMessage(text);
        if (statusNotificationId == -1) {
            statusNotificationId = (int) System.currentTimeMillis();
        }

        if (text != null) {
            NotificationHelper.notify(this,
                    statusNotificationId,
                    NotificationHelper.getBuilder(this, "SMS2 Status", text));
        }
        else {
            NotificationHelper.cancel(this, statusNotificationId);
            statusNotificationId = -1;
        }
    }

    @Override
    public boolean isURLValid(String url) {
        postStatusNotification("Finding correct plugin...");
        for (ModPlugin plugin : ModPluginEngine.getInstance(this).each()) {
            if (plugin.isURLValid(url)) {
                this.module = plugin;
                postStatusNotification(String.format("Found plugin : %s", plugin.getName()));
                return true;
            }
        }

        this.module = null;
        return false;
//        boolean isValid = ModulesCentral.with(this).check(url);
//        if (isValid)
//            this.module = ModulesCentral.with(this).get();
//        else
//            this.module = null;
//        return isValid;
    }

    private void cancelProgress() {
        notification = NotificationHelper.getBuilder(this, null, "Error occurred while downloading file!")
                .setProgress(0, 0, false)
                .setAutoCancel(true)
                .setOngoing(false);

        NotificationHelper.notify(this, notificationId, notification);
    }

    private File downloadMedia(String url) {
        DownloadTask task = new DownloadTask.Builder(
                url,
                ModPluginEngine.getInstance(this).getPluginBaseDirPath(module),
                null
        ).setFilenameFromResponse(true)
                .setMinIntervalMillisCallbackProcess(1000)
                .build();

        task.execute(new DownloadListener1() {
            @Override
            public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {

            }

            @Override
            public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {

            }

            @Override
            public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
                updateNotification((int) ((currentOffset * 100d) / totalLength));
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
                updateNotification((int) ((currentOffset * 100d) / totalLength));
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {

            }
        });

        return task.getFile();
    }

    private String determineType(String uri) {
        uri = uri.split("\\?")[0];
        int lastDot = uri.lastIndexOf(".") + 1;
        String type = uri.substring(lastDot);
        return type.matches("((jpe?|pn)g)") ? "photo" : type.matches("(mp4|ts|m3u8)") ? "video" : "file";
    }

//    private String initializeBaseDir(String savePath) {
//        savePath = AppSettings.getInstance(this).storageMedia() + savePath + File.separator;
//        StorageUtils.isDirectoryExists(savePath, true);
//        return savePath;
//    }
//
//    private void toastMessage(final String message) {
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(URLHandler.this, message, Toast.LENGTH_LONG).show();
//            }
//        });
//    }

    private static class URLHandlerUncaughtException extends CrashCocoExceptionHandler {
        private Thread.UncaughtExceptionHandler defaultHandler;
        private WeakReference<URLHandler> instance;

        /* package */ URLHandlerUncaughtException(URLHandler instance) {
            super("sms");
            this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            this.instance = new WeakReference<>(instance);
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            this.instance.get().cancelProgress();
            super.uncaughtException(thread, throwable);
        }

        private Thread.UncaughtExceptionHandler getDefaultHandler() {
            return defaultHandler;
        }
    }
}
