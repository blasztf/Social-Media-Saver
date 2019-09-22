package com.blaszt.socialmediasaver2.logger;

import android.os.Environment;

import com.blaszt.modulelinker.helper.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import toolkit.util.TStrings;

public class CrashCocoExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String ENV = File.separator + "CrashCoco_Reports" + File.separator;

    private String id;
    private Thread.UncaughtExceptionHandler defaultHandler;

    public CrashCocoExceptionHandler(String id) {
        this.id = id;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            writeLog(getMessageLog(throwable));
        } catch (IOException e) {
            e.printStackTrace();
        }

        defaultHandler.uncaughtException(thread, throwable);
    }

    private String getMessageLog(Throwable e) {
        String message;
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        message = writer.toString();
        printWriter.close();
        IOUtils.closeQuietly(writer);
        return message;
    }

    private void writeLog(String msg) throws IOException {
        File reportFile = getFileLog();
        FileWriter reportWriter = new FileWriter(reportFile);
        reportWriter.append(formatLog(msg));
        reportWriter.flush();
        reportWriter.close();
    }

    private String formatLog(String log) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss", Locale.US);
        Date date = new Date();
        log = dateFormat.format(date) + "\n\n[STACKTRACE]\n\t|\n\tV\n" + log + "\n\n" + TStrings.repeat("=", 15) + "\n\n";
        return log;
    }

    private File getFileLog() {
        File file = new File(Environment.getExternalStorageDirectory(), String.format("%s%s.%s.%s.%s", ENV, "cc", id, "log", "txt"));
        if (!file.exists() || !file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }

        return file;
    }
}
