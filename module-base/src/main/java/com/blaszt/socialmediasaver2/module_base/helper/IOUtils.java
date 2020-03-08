package com.blaszt.socialmediasaver2.module_base.helper;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

public class IOUtils {

    public static void closeQuietly(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void disconnectQuietly(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
