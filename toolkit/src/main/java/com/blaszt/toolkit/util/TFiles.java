package com.blaszt.toolkit.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public final class TFiles {

    public static File[] listFiles(File file, int max) {
        return listFiles(file, 0, max, null);
    }

    public static File[] listFiles(File file, int from, int max) {
        return listFiles(file, from, max, null);
    }

    public static File[] listFiles(File file, int from, int max, FilenameFilter filter) {
        final File[] ls;
        final int fr = from;
        final int mx = max;
        final FilenameFilter fi = filter;
        if (file != null && from >= 0) {
            ls = file.listFiles(new FilenameFilter() {
                int i = 0, f = 0;

                @Override
                public boolean accept(File file, String s) {
                    if (i >= fr && f != mx) {
                        if (fi != null) {
                            if (fi.accept(file, s)) {
                                f++;
                                return true;
                            }
                        } else {
                            f++;
                            return true;
                        }
                    }
                    i++;
                    return false;
                }
            });
        } else {
            ls = new File[0];
        }
        return ls;
    }

    public static String[] list(File file, int max) {
        return list(file, 0, max, null);
    }

    public static String[] list(File file, int from, int max) {
        return list(file, from, max, null);
    }

    public static String[] list(File file, int from, int max, FilenameFilter filter) {
        final String[] ls;
        final int fr = from;
        final int mx = max;
        final FilenameFilter fi = filter;
        if (file != null && from >= 0) {
            ls = file.list(new FilenameFilter() {
                int i = 0, f = 0;

                @Override
                public boolean accept(File file, String s) {
                    if (i >= fr && f != mx) {
                        if (fi != null) {
                            if (fi.accept(file, s)) {
                                f++;
                                return true;
                            }
                        } else {
                            f++;
                            return true;
                        }
                    }
                    i++;
                    return false;
                }
            });
        } else {
            ls = new String[0];
        }
        return ls;
    }
}
