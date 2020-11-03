package com.blaszt.socialmediasaver2.service.handler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.blaszt.socialmediasaver2.data.SMSContent;
import com.blaszt.socialmediasaver2.helper.data.StringUtils;
import com.blaszt.socialmediasaver2.plugin.ModPlugin;
import com.blaszt.socialmediasaver2.plugin.ModPluginEngine;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;

public class MediaHandler extends HandlerService<Boolean> {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MediaHandler(String name) {
        super(name);
    }

    @Override
    String getAction() {
        return SMSContent.Intent.ACTION_DOWNLOAD_MEDIA;
    }

    @Override
    void onHandle(@NonNull Intent intent) {
        File media;
        NotificationTicket ticket;
        String type;
        String[] mediaURLs = intent.getStringArrayExtra(SMSContent.Intent.EXTRA_MEDIA_URL);
        int pluginIndex = intent.getIntExtra(SMSContent.Intent.EXTRA_MOD_PLUGIN_INDEX, -1);

        if (mediaURLs != null && mediaURLs.length > 0 && pluginIndex > -1) {
            for (String mediaURL : mediaURLs) {
                type = determineType(mediaURL);
                ticket = requestTicket();
                ticket.setContent("Downloading...", String.format("Downloading %s", type));
                media = downloadMedia(mediaURL, pluginIndex, ticket);
                ticket.setContent(media != null ? "Downloaded" : "Failed", StringUtils.toUpperCaseFirst(String.format(media != null ? "%s has been downloaded!" : "Failed to download %s", type)));
                if (media != null) ticket.setPicture(media.getAbsolutePath());
            }
        }
    }

    private String getModPluginSavePath(int pluginIndex) {
        ModPlugin plugin = ModPluginEngine.getInstance(this).each().get(pluginIndex);
        return ModPluginEngine.getInstance(this).getPluginBaseDirPath(plugin);
    }

    private File downloadMedia(String url, int pluginIndex, final NotificationTicket ticket) {
        DownloadTask task = new DownloadTask.Builder(
                url,
                getModPluginSavePath(pluginIndex),
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
                updateProgress(currentOffset, totalLength);
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
                updateProgress(currentOffset, totalLength);
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
                endProgress();
            }

            private void endProgress() {
                ticket.setProgress(-1);
            }

            private void updateProgress(long current, long total) {
                ticket.setProgress((int) ((current * 100d) / total));
                giveTicket(ticket);
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
}
