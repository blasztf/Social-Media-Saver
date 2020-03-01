package com.blaszt.modulelinker.helper;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HttpLogger implements Thread.UncaughtExceptionHandler {
    private static final String ENV = File.separator + "CrashCoco_Reports" + File.separator;

    private String id;
    private Thread.UncaughtExceptionHandler defaultHandler;

    public HttpLogger(String id) {
        this.id = id;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        writeLog(getMessageLog(throwable));
        defaultHandler.uncaughtException(thread, throwable);
    }

    public String getMessageLog(Throwable e) {
        String message;
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        message = writer.toString();
        printWriter.close();
        IOUtils.closeQuietly(writer);
        return message;
    }

    public void writeLog(String msg)  {
        File reportFile = getFileLog();
        try {
            FileWriter reportWriter = new FileWriter(reportFile, true);
            reportWriter.append(formatLog(msg));
            reportWriter.flush();
            reportWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatLog(String log) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss", Locale.US);
        Date date = new Date();
        log = dateFormat.format(date) + "\n\n[STACKTRACE]\n\t|\n\tV\n" + log + "\n\n====================\n\n";
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
