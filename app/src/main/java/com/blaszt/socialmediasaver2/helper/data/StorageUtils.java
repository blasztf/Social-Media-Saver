package com.blaszt.socialmediasaver2.helper.data;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.RequiresApi;

import java.io.File;

public class StorageUtils {

    public static boolean isDirectoryExists(String path, boolean createIfNecessary) {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            if (createIfNecessary) {
                return directory.mkdirs();
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getAvailableExternalStorageSizeLong() {
        long memorySize = 0;
        if (isExternalStorageAvailable()) {
            StatFs statusFileSystem = getExternalStatusFileSystem();
            long blockSize = statusFileSystem.getBlockSizeLong();
            long availableBlocks = statusFileSystem.getAvailableBlocksLong();
            memorySize = blockSize * availableBlocks;
        }

        return memorySize;
    }

    @Deprecated
    public static int getAvailableExternalStorageSize() {
        int memorySize = 0;
        if (isExternalStorageAvailable()) {
            StatFs statusFileSystem = getExternalStatusFileSystem();
            int blockSize = statusFileSystem.getBlockSize();
            int availableBlocks = statusFileSystem.getAvailableBlocks();
            memorySize = blockSize * availableBlocks;
        }

        return memorySize;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getTotalExternalStorageSizeLong() {
        long memorySize = 0;
        if (isExternalStorageAvailable()) {
            StatFs statusFileSystem = getExternalStatusFileSystem();
            long blockSize = statusFileSystem.getBlockSizeLong();
            long totalBlocks = statusFileSystem.getBlockCountLong();
            memorySize = blockSize * totalBlocks;
        }

        return memorySize;
    }

    @Deprecated
    public static int getTotalExternalStorageSize() {
        int memorySize = 0;
        if (isExternalStorageAvailable()) {
            StatFs statusFileSystem = getExternalStatusFileSystem();
            int blockSize = statusFileSystem.getBlockSize();
            int totalBlocks = statusFileSystem.getBlockCount();
            memorySize = blockSize * totalBlocks;
        }

        return memorySize;
    }

    private static StatFs getExternalStatusFileSystem() {
        File path = Environment.getExternalStorageDirectory();
        return new StatFs(path.getAbsolutePath());
    }
}
