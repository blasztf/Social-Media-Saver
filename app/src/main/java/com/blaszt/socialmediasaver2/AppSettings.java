package com.blaszt.socialmediasaver2;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.view.ContextThemeWrapper;

import com.blaszt.socialmediasaver2.data.Pair;
import com.blaszt.socialmediasaver2.data.StorageBase;
import com.blaszt.socialmediasaver2.main.MainActivity;
import com.blaszt.socialmediasaver2.services.URLHandler;

import java.io.File;
import java.lang.ref.WeakReference;

public final class AppSettings {
    private static AppSettings mInstance;

    private String KEY_SORT_MEDIA;
    private String KEY_RECURSIVE;
    private String KEY_STORAGE;

    private WeakReference<Context> mContext;
    private SharedPreferences mSharedPreferences;

    private String mVirtualEnv;

    private AppSettings(Context context) {
        mContext = new WeakReference<>(context.getApplicationContext());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.get());

        setAllKey();
        setVirtualEnv();
    }

    private Context getContext() {
        return mContext.get();
    }

    private void setAllKey() {
        KEY_SORT_MEDIA = getContext().getString(R.string.pref_key_sort_media);
        KEY_RECURSIVE = getContext().getString(R.string.pref_key_recursive);
        KEY_STORAGE = getContext().getString(R.string.pref_key_storage);
    }

    private void setVirtualEnv() {
        File[] dirs = ContextCompat.getExternalFilesDirs(getContext(), Environment.DIRECTORY_PICTURES);
        if (!dirs[dirs.length - 1].exists() || !dirs[dirs.length - 1].isDirectory()) {
            dirs[dirs.length - 1].mkdirs();
        }
        mVirtualEnv = dirs[dirs.length - 1].getAbsolutePath() + File.separator + "Social Media Saver 2" + File.separator;
    }

    public static synchronized AppSettings getInstance(Context context) {
        boolean contextValid = checkContext(context);
        if (contextValid) {
            if (mInstance == null || mInstance.getContext() == null) {
                mInstance = new AppSettings(context);
            }
            return mInstance;
        } else {
            throw new RuntimeException("Context not valid!");
        }
    }

    public int sortMedia() {
        return mSharedPreferences.getInt(KEY_SORT_MEDIA, Pair.BY_DATE | Pair.ORDER_DESCENDING);
    }

    public boolean findRecursively() {
        return true; //mSharedPreferences.getBoolean(KEY_RECURSIVE, true);
    }

    public String storageMedia() {
        return parseStorageValue(mSharedPreferences.getString(KEY_STORAGE, "0"));
    }

    private String parseStorageValue(String prefValue) {
        if (prefValue.equals("1")) {
            return getVirtualEnv();
        } else {
            return StorageBase.ENVIRONMENT;
        }
    }

    private String getVirtualEnv() {
        return mVirtualEnv;
    }

    private static boolean checkContext(Context context) {
        return context instanceof MainActivity ||
                context instanceof SettingsActivity ||
                context instanceof URLHandler ||
                isContextFromBroadcast(context);
    }

    private static boolean isContextFromBroadcast(Context context) {
        return !(context instanceof Activity) &&
                !(context instanceof Service) &&
                !(context instanceof Application) &&
                !(context instanceof ContextThemeWrapper) &&
                context instanceof ContextWrapper;
    }
}
