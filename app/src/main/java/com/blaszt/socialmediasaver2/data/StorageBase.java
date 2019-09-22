package com.blaszt.socialmediasaver2.data;

import android.os.Environment;

import java.io.File;
import java.nio.file.FileSystems;

public final class StorageBase {
    public static final String ENVIRONMENT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "Social Media Saver 2" + File.separator;
}
