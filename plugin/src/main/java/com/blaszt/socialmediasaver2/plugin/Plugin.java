package com.blaszt.socialmediasaver2.plugin;

import com.blaszt.socialmediasaver2.plugin.helper.Helper;

import java.util.HashMap;
import java.util.Map;

public abstract class Plugin {
    private PluginNet mPluginNet;
    private Map<String, Helper> mListHelper;

    public abstract String getName();
    public abstract String getIcon(); // Base 64 Format
    public abstract String[] getMediaURLs(String url);
    public abstract boolean isURLValid(String url);

    protected Plugin(PluginNet pluginNet) {
        mPluginNet = pluginNet;
        mListHelper = new HashMap<>();
    }

    protected Plugin(Plugin extendFrom) {
        mPluginNet = extendFrom.mPluginNet;
        mListHelper = extendFrom.mListHelper;
    }

    protected PluginNet getPluginNet() {
        return mPluginNet;
    }

    protected void setHelper(String clazz, Helper obj) {
        mListHelper.put(clazz, obj);
    }

    public <T extends Helper> T getHelper(Class<T> clazz) {
        return (T) mListHelper.get(clazz.getName());
    }
}
