package com.blaszt.socialmediasaver2.data;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaData implements Comparable<MediaData>, Serializable{
    /**
     *
     */
    private static final long serialVersionUID = -5908252885165357475L;

    private String name;
    private String path;
    private String moduleType;
    private int type;
    private long size;
    private long date;

    private String readableSize;
    private String readableDate;

    private int sortFlags;

    private static final int TYPE_PHOTO = 0/*0x00F0705*/, TYPE_VIDEO = 1/*0x00B1D10*/;

    public MediaData(String name, String path, long size, long date, int sortFlags) {
        // TODO Auto-generated constructor stub
        setName(name);
        setPath(path);
        setSize(size);
        setDate(date);
        setSortFlags(sortFlags);
    }

    public void setName(String name) {
        this.name = name;
        this.type = isImage(name) ? TYPE_PHOTO : TYPE_VIDEO;
    }

    public void setPath(String path) {
        this.path = path;

        setModuleType(new File(path).getParentFile().getName());
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public void setSize(long size) {
        this.size = size;
        this.readableSize = humanReadableByteCount(size, true);
    }

    public void setDate(long date) {
        this.date = date;
        this.readableDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(new Date(date));
    }

    private void setSortFlags(int sortFlags) {
        this.sortFlags = sortFlags;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getModuleType() {
        return moduleType;
    }

    public int getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public long getDate() {
        return date;
    }

    public String getReadableSize() {
        return readableSize;
    }

    public String getReadableDate() {
        return readableDate;
    }

    public boolean isPhoto() {
        return getType() == TYPE_PHOTO;
    }

    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private boolean isImage(String name) {
        int find = name.lastIndexOf('.');
        if (find == -1) {
            return false;
        }
        else {
            String extension = name.substring(find+1, name.length());
            return extension.matches("((jpe?|pn)g|gif)");
        }
    }

    @Override
    public int compareTo(@NonNull MediaData polaroid) {
        // TODO Auto-generated method stub
        if ((sortFlags & Pair.BY_NAME) != 0) {
            String o = polaroid.getName();
            String t = getName();
            if ((sortFlags & Pair.ORDER_DESCENDING) != 0) {
                return o.compareTo(t);
            }
            else {
                return t.compareTo(o);
            }
        }
        else {
            long o = (sortFlags & Pair.BY_SIZE) != 0 ? polaroid.getSize() : (sortFlags & Pair.BY_TYPE) != 0 ? polaroid.getType() : polaroid.getDate();
            long t = (sortFlags & Pair.BY_SIZE) != 0 ? getSize() : (sortFlags & Pair.BY_TYPE) != 0 ? getType() : getDate();
            if ((sortFlags & Pair.ORDER_DESCENDING) != 0) {
                return t > o ? -1 : t == o ? 0 : 1; // API19 => Long.compare(o, t);
            }
            else {
                return t < o ? -1 : t == o ? 0 : 1; // API19 => Long.compare(t, o);
            }
        }
    }
}
