package com.blaszt.socialmediasaver2.services;

import com.blaszt.socialmediasaver2.module.Module;

import java.io.File;
import java.util.ArrayList;

interface URLHandlerListener {
    void startNotification(String type);
    void updateNotification(int progress);
    void endNotification(File media, String type);
    File onHandleURL(String url);
    boolean isURLValid(String url);
}
