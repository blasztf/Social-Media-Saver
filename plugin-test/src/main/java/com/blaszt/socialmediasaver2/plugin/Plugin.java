package com.blaszt.socialmediasaver2.plugin;

public abstract class Plugin {
    private PluginNet mPluginNet;
    private Helper mHelper;

    public abstract String getName();
    public abstract String getIcon(); // Base 64 Format
    public abstract String[] getMediaURLs(String url);
    public abstract boolean isURLValid(String url);

    protected Plugin(PluginNet pluginNet) {
        mPluginNet = pluginNet;
        mHelper = new Helper();
    }

    protected PluginNet getPluginNet() {
        return mPluginNet;
    }

    public Helper getHelper() {
        return mHelper;
    }

    private static class Helper {

    }
}
