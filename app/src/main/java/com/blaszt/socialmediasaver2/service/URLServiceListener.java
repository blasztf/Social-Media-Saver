package com.blaszt.socialmediasaver2.service;

import android.support.v4.app.NotificationCompat;

interface URLServiceListener {
    int onBuildNotificationForeground(NotificationCompat.Builder notification);
    void setupNotificationForeground();
    void setupClipboardService();
    void clearClipboardService();
    void onURLCopied(String url);
    void handle(String url);
}
