package com.blaszt.socialmediasaver2.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.blaszt.modulelinker.Responder;
import com.blaszt.modulelinker.helper.IOUtils;
import com.blaszt.socialmediasaver2.AppSettings;
import com.blaszt.socialmediasaver2.helper.data.MemeType;
import com.blaszt.socialmediasaver2.helper.data.StorageUtils;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class URLHandler extends IntentService implements URLHandlerListener {
    public static final String EXTRA_URL = URLHandler.class.getName() + ".EXTRA_URL";
    public static final String ACTION_HANDLE_URL = URLHandler.class.getName() + ".intent.action.HANDLE_URL";

    private long currentTimeMillis;
    private int notificationId;
    private int statusNotificationId;

    private NotificationCompat.Builder notification;

    private ModPlugin module;

    public URLHandler() {
        super("URLHandler");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new URLHandlerUncaughtException(this));

        String url;
        if (intent != null) {
            url = intent.getStringExtra(EXTRA_URL);
            if (url != null) {
                if (isURLValid(url)) {
                    onHandleURL(url);
                }
            }
        }
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
        currentTimeMillis = System.currentTimeMillis();

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
        postStatusNotification(String.format("Total mediaURLs : %d", mediaURLs.length));
        if (mediaURLs != null && mediaURLs.length > 0) {
//            Log.d("MEDIA_URL_SIZE", "size : " + mediaURLs.length);
            postStatusNotification("Downloading media...");
            for (String mediaURL : mediaURLs) {
//                Log.d("MEDIA_URL", mediaURL);
                String type = determineType(mediaURL);
                startNotification(type);
                media = downloadMedia(mediaURL);
                endNotification(media, type);
            }
        }
//        postStatusNotification(null);

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

    private void toastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(URLHandler.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
//        return new Downloader(new OnDownloadListener() {
//            @Override
//            public void onProgress(int progress) {
//                if ((System.currentTimeMillis() - currentTimeMillis) >= 1000L) {
//                    currentTimeMillis = System.currentTimeMillis();
//                    updateNotification(progress);
//                }
//            }
//        }).download(url, initializeBaseDir(module.getBaseDir()), null);
    }

    private String initializeBaseDir(String savePath) {
        savePath = AppSettings.getInstance(this).storageMedia() + savePath + File.separator;
        StorageUtils.isDirectoryExists(savePath, true);
        return savePath;
    }

    private String determineType(String uri) {
        uri = uri.split("\\?")[0];
        int lastDot = uri.lastIndexOf(".") + 1;
        String type = uri.substring(lastDot);
        return type.matches("((jpe?|pn)g)") ? "photo" : type.matches("(mp4|ts|m3u8)") ? "video" : "file";
    }

    private interface OnDownloadListener {
        void onProgress(int progress);
    }

    private class Downloader {
        OnDownloadListener listener;

        Downloader(OnDownloadListener listener) {
            this.listener = listener;
        }

        File download(String url, String savePath, HashMap<String, String> headers) {
            String ext = url.substring(url.lastIndexOf(".")).replaceAll("\\?(.*)", "");
            if (".m3u8".equalsIgnoreCase(ext)) {
                return new M3U8Downloader(url, savePath).download(headers, listener);
            } else return download(url, savePath, headers, listener);
        }

        private File download(String url, String savePath, HashMap<String, String> headers, OnDownloadListener listener) {
            String filename;
            File result = null;
            HttpURLConnection connection = null;
            InputStream input = null;
            FileOutputStream output = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                if (headers != null) setConnectionHeaders(connection, headers);

//                Map<String, List<String>> headerFields = connection.getHeaderFields();

                int responseCode = connection.getResponseCode();

                // Connection success
                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    // Guessing filename
//                    if (filename == null) {
                        // Try with header "Content-Disposition" to find the file name.
                        String disposition = connection.getHeaderField("Content-Disposition");
                        String type = connection.getHeaderField("Content-Type");
                        int index;
                        if (type != null && (index = type.indexOf(';')) >= 0) {
                            type = type.substring(0, index);
                            type = MemeType.with(type).guessExtension();
                        }
                        if (disposition != null && (index = disposition.indexOf("filename=")) >= 0) {
                            filename = disposition.substring(index + 10);
                            if (filename.lastIndexOf('.') == -1) {
                                filename += type;
                            }
                        } else {
                            // Try with parsing url link to find the file name.
                            url = url.split("\\?")[0];
                            int beginIndex = url.lastIndexOf("/");
                            int endIndex = url.lastIndexOf(".");

                            if (endIndex == -1) {
                                filename = url.substring(beginIndex + 1);
                                filename += type;
                            } else if (beginIndex < endIndex) {
                                filename = url.substring(beginIndex + 1, endIndex);
                                // Just use the rest of url after the last dot as extension (hopefully there is no parameter behind it).
                                filename += url.substring(endIndex).replaceAll("\\?(.+)", "");
                            } else {
                                // Last attempt, just use "System.currentTimeMillis" as file name.
                                filename = System.currentTimeMillis() + type;
                            }
                        }
//                    }

                    // Substract filename to maximum limited filename length on (hopefully...) most devices
                    if (filename.length() > 30) {
                        filename = filename.substring(filename.length() - 30).replaceAll("\\?(.*)", "");
                    }

                    input = connection.getInputStream();
                    int lengthFile = connection.getContentLength();

                    result = new File(savePath, filename);

                    output = new FileOutputStream(result);
                    byte[] data = new byte[determineBufferSize(lengthFile)];
                    int current, total = 0;
                    while ((current = input.read(data, 0, data.length)) != -1) {
                        output.write(data, 0, current);
                        total += current;

                        if (listener != null)
                            listener.onProgress((int) ((total * 100d) / lengthFile));
                    }
                } else {
                    result = null;
                }
            } catch (IOException e) {
                if (result != null && result.exists() && result.isFile()) {
                    result = result.delete() ? null : result;
                } else {
                    result = null;
                }
            } finally {
                IOUtils.closeQuietly(output);
                IOUtils.closeQuietly(input);
                IOUtils.disconnectQuietly(connection);
            }

            return result;
        }

        private void setConnectionHeaders(HttpURLConnection connection, Map<String, String> headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        private int determineBufferSize(long lengthFile) {
            final int LIMIT_BUFFER_SIZE = 8 * 1024;
            int bufferSize = 0;
            if (lengthFile >= LIMIT_BUFFER_SIZE) {
                bufferSize = LIMIT_BUFFER_SIZE;
            } else if (lengthFile >= 1024) {
                for (int i = 1; i * 1024 < lengthFile; i *= 2) {
                    bufferSize = i * 1024;
                }
            } else {
                bufferSize = 1024;
            }
            return bufferSize;
        }

        private class M3U8Downloader {
            private URI uri;
            private String savePath;
            private URLHandler context = URLHandler.this;

            M3U8Downloader(String url, String savePath) {
                uri = URI.create(url);
                this.savePath = savePath;
            }

            File download(HashMap<String, String> headers, OnDownloadListener listener) {
                Responder.Options options = Responder.Options.create()
                        .setRequestHeaders(headers)
                        .requestResponseCookies(true);

                String m3u8Content = Responder.with(context).apply(options).getResponse(uri.toString());
                if (isM3U8Resolutions(m3u8Content)) {
                    String highestResVideo = findHighestResVideo(m3u8Content);
                    m3u8Content = Responder.with(context).apply(options).getResponse(highestResVideo);
                    if (isM3U8Fragments(m3u8Content)) {
                        String[] mpegtsVideos = findAllMpegtsVideo(m3u8Content);
                        return joiningM3U8Fragments(downloadM3U8Fragments(mpegtsVideos, headers, listener));
                    }
                } else if (isM3U8Fragments(m3u8Content)) {
                    String[] mpegtsVideos = findAllMpegtsVideo(m3u8Content);
                    return joiningM3U8Fragments(downloadM3U8Fragments(mpegtsVideos, headers, listener));
                }

                return null;
            }

            private File joiningM3U8Fragments(ArrayList<File> fragments) {
                byte[] data;
                int current;

                FileInputStream inputStream;
                FileOutputStream outputStream;

                String newFileName = savePath + uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1).replaceAll(".m3u8(\\?.*)?", ".ts");
                File newFile = new File(newFileName);
                try {
                    outputStream = new FileOutputStream(newFile, true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    outputStream = null;
                }

                if (outputStream != null) {
                    for (File fragment : fragments) {
                        if (fragment != null) {
                            try {
                                inputStream = new FileInputStream(fragment);
                            } catch (FileNotFoundException e) {
                                inputStream = null;
                            }

                            if (inputStream != null) {
                                data = new byte[1024];
                                try {
                                    while ((current = inputStream.read(data, 0, data.length)) != -1) {
                                        outputStream.write(data, 0, current);
                                    }
                                } catch (IOException e) {
                                    IOUtils.closeQuietly(inputStream);
                                }

                                IOUtils.closeQuietly(inputStream);
                            }

                            fragment.delete();
                        }
                    }

                    IOUtils.closeQuietly(outputStream);
                } else {
                    newFile = null;
                }

                return newFile;
            }

            private ArrayList<File> downloadM3U8Fragments(final String[] urls, HashMap<String, String> headers, final OnDownloadListener listener) {
                File tempDir = getTempDir();
                ArrayList<File> fragments = new ArrayList<>();
                final int urlsSize = urls.length;
                OnDownloadListener thisListener = new OnDownloadListener() {
                    private int tempProgress = 0;

                    @Override
                    public void onProgress(int progress) {
//                        tempProgress += (progress / urlsSize) - 10;
//                        tempProgress = progress;
                        tempProgress += (progress / urlsSize);
                        listener.onProgress(tempProgress);
                    }

                };
                for (String url : urls) {
                    fragments.add(Downloader.this.download(url, tempDir.getAbsolutePath(), headers, thisListener));
                }
                return fragments;
            }

            private boolean isM3U8Resolutions(@Nullable String m3u8Content) {
                return m3u8Content != null && m3u8Content.contains("#EXT-X-STREAM-INF") && m3u8Content.contains(".m3u8");
            }

            private boolean isM3U8Fragments(@Nullable String m3u8Content) {
                return m3u8Content != null && m3u8Content.contains("#EXT-X-MEDIA-SEQUENCE") && m3u8Content.contains(".ts");
            }

            private String[] findAllMpegtsVideo(String response) {
                ArrayList<String> videoList = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new StringReader(response));
                try {
                    String line;
                    int lastPeriodFound;
                    while ((line = reader.readLine()) != null) {
                        if ((lastPeriodFound = line.lastIndexOf(".")) != -1) {
                            if (line.substring(lastPeriodFound).equals(".ts")) {
                                if (line.charAt(0) == '/') {
                                    line = String.format(uri.getScheme() + "%s" + uri.getHost() + "%s", "://", line);
                                } else {
                                    line = String.format(uri.toString() + "%s", line);
                                }
                                videoList.add(line);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                IOUtils.closeQuietly(reader);

                return videoList.toArray(new String[videoList.size()]);
            }

            private String findHighestResVideo(String response) {
                String highestResVideo = null;
                ArrayList<String> videoList = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new StringReader(response));
                try {
                    String line;
                    int lastPeriodFound;
                    while ((line = reader.readLine()) != null) {
                        if ((lastPeriodFound = line.lastIndexOf(".")) != -1) {
                            if (line.substring(lastPeriodFound).equals(".m3u8")) {
                                if (line.charAt(0) == '/') {
                                    line = String.format(uri.getScheme() + "%s" + uri.getHost() + "%s", "://", line);
                                } else {
                                    line = String.format(uri.toString() + "%s", line);
                                }
                                videoList.add(line);
                            }
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!videoList.isEmpty()) {
                    highestResVideo = videoList.get(videoList.size() - 1);
                }
                return highestResVideo;
            }

            private File getTempDir() {
                File[] volume = ContextCompat.getExternalFilesDirs(context, null);
                File externalStorage = volume[volume.length - 1];
                // temp dir
                externalStorage = new File(externalStorage, String.format("%s.temp_m3u8%s", File.separator, File.separator));
                // create directory if not exists
                if (!externalStorage.exists() || !externalStorage.isDirectory()) {
                    externalStorage.mkdirs();
                }

                return externalStorage;
            }
        }
    }

    private class URLHandlerUncaughtException extends CrashCocoExceptionHandler {
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
    }
}
