package com.blaszt.toolkit.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

public class TIO {
    public static void closeQuietly(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            //ignore
        }
    }

    public static void disconnectQuietly(HttpURLConnection connection) {
        connection.disconnect();
    }

    public static void disconnectQuietly(URLConnection connection) {
        try {
            connection.getInputStream().close();
        } catch (IOException e) {
            //ignore
        }

        try {
            connection.getOutputStream().close();
        } catch (IOException e) {
            //ignore
        }
    }
}
