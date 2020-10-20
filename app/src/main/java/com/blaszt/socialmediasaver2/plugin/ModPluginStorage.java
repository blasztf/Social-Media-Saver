package com.blaszt.socialmediasaver2.plugin;

import android.content.Context;

import com.blaszt.socialmediasaver2.plugin.helper.storage.StorageCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

class ModPluginStorage implements StorageCache {
    private File mBaseDir;

    ModPluginStorage(Context context, ModPlugin plugin) {
        mBaseDir = new File(context.getExternalFilesDir("plugins_cache"), plugin.getName());
        if (!mBaseDir.exists() || !mBaseDir.isDirectory()) {
            mBaseDir.mkdirs();
        }
    }

    @Override
    public boolean write(String key, String data) {
        boolean result;
        FileWriter writer = null;

        try {
            writer = new FileWriter(new File(mBaseDir, key + ".cache"), false);
            writer.write(data);
            writer.flush();
            writer.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    @Override
    public String read(String key) {
        String result;
        BufferedReader reader;
        StringBuilder line = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(mBaseDir + File.separator + key + ".cache")));
            while ((result = reader.readLine()) != null) {
                line.append(result);
            }
            result = line.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = null;
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }

}
