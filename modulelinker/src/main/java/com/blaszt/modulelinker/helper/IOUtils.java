package com.blaszt.modulelinker.helper;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

public class IOUtils {

    public static void closeQuietly(@Nullable Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void disconnectQuietly(@Nullable HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
