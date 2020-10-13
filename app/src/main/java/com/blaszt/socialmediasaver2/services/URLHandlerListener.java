package com.blaszt.socialmediasaver2.services;

import java.io.File;

interface URLHandlerListener {
    void startNotification(String type);
    void updateNotification(int progress);
    void endNotification(File media, String type);
    File onHandleURL(String url);
    boolean isURLValid(String url);
}
