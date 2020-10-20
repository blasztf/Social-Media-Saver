package com.blaszt.socialmediasaver2.plugin;

import android.content.Context;

import com.blaszt.socialmediasaver2.plugin.helper.Helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class ModPlugin extends Plugin {
    static final String EXTENSION = ".spin";

    private Class<?> mClass;
    private Object mInstance;

    ModPlugin(DexClassLoader classLoader, String pluginClassName, ModPluginNet pluginNet) throws ModPluginException {
        super(pluginNet);
        Constructor<?> ctor;

        try {
            mClass = classLoader.loadClass(pluginClassName);
            ctor = mClass.getDeclaredConstructor(PluginNet.class);
            ctor.setAccessible(true);
            mInstance = ctor.newInstance(pluginNet);
        } catch (InstantiationException e) {
            throw new ModPluginException(e);
        } catch (NoSuchMethodException e) {
            throw new ModPluginException(e);
        } catch (IllegalAccessException e) {
            throw new ModPluginException(e);
        } catch (ClassNotFoundException e) {
            throw new ModPluginException(e);
        } catch (InvocationTargetException e) {
            throw new ModPluginException(e);
        }
    }

    @Override
    public String getName() {
        Method method;
        String name;

        try {
            method = mClass.getMethod("getName");
            name = (String) method.invoke(mInstance);
        } catch (NoSuchMethodException e) {
            throw new ModPluginException(e);
        } catch (IllegalAccessException e) {
            throw new ModPluginException(e);
        } catch (InvocationTargetException e) {
            throw new ModPluginException(e);
        }

        return name;
    }

    @Override
    public String getIcon() {
        Method method;
        String icon;

        try {
            method = mClass.getMethod("getIcon");
            icon = (String) method.invoke(mInstance);
        } catch (NoSuchMethodException e) {
            throw new ModPluginException(e);
        } catch (IllegalAccessException e) {
            throw new ModPluginException(e);
        } catch (InvocationTargetException e) {
            throw new ModPluginException(e);
        }

        return icon;
    }

    @Override
    public String[] getMediaURLs(String url) {
        Method method;
        String[] mediaURLs;

        try {
            method = mClass.getMethod("getMediaURLs", String.class);
            mediaURLs = (String[]) method.invoke(mInstance, url);
        } catch (NoSuchMethodException e) {
            throw new ModPluginException(e);
        } catch (IllegalAccessException e) {
            throw new ModPluginException(e);
        } catch (InvocationTargetException e) {
            throw new ModPluginException(e);
        }

        return mediaURLs;
    }

    @Override
    public boolean isURLValid(String url) {
        Method method;
        boolean urlValid;

        try {
            method = mClass.getMethod("isURLValid", String.class);
            urlValid = (boolean) method.invoke(mInstance, url);
        } catch (NoSuchMethodException e) {
            throw new ModPluginException(e);
        } catch (IllegalAccessException e) {
            throw new ModPluginException(e);
        } catch (InvocationTargetException e) {
            throw new ModPluginException(e);
        }

        return urlValid;
    }

    @Override
    protected PluginNet getPluginNet() {
        Method method;
        ModPluginNet pluginNet;

        try {
            method = mClass.getDeclaredMethod("getPluginNet");
            method.setAccessible(true);
            pluginNet = (ModPluginNet) method.invoke(mInstance);
        } catch (IllegalAccessException e) {
            throw new ModPluginException(e);
        } catch (InvocationTargetException e) {
            throw new ModPluginException(e);
        } catch (NoSuchMethodException e) {
            throw new ModPluginException(e);
//            pluginNet = null;
        }

        return pluginNet;
    }

    <T extends Helper> void setHelper(Class<T> clazz, T helper) {
        Method method;

        try {
            method = mClass.getDeclaredMethod("setHelper", String.class, Helper.class);
            method.setAccessible(true);
            method.invoke(mInstance, clazz.getName(), helper);
        } catch (IllegalAccessException e) {
            throw new ModPluginException(e);
        } catch (InvocationTargetException e) {
            throw new ModPluginException(e);
        } catch (NoSuchMethodException e) {
            throw new ModPluginException(e);
        }
    }

    private ModPluginNet getModPluginNet() {
        return (ModPluginNet) getPluginNet();
    }

    public ContextInjector use(Context context) {
        return new ContextInjector(this, context);
    }

    public static class ContextInjector extends DependInjectContext {
        private ModPlugin mModPlugin;

        private ContextInjector(ModPlugin plugin, Context context) {
            super(context);
            mModPlugin = plugin;
            mModPlugin.getModPluginNet().injectContext(this);
        }

        public String[] getMediaURLs(String url) {
            return mModPlugin.getMediaURLs(url);
        }
    }

    public static class ModPluginException extends RuntimeException {
        public ModPluginException(String message) {
            super(message);
        }

        public ModPluginException(Throwable cause) {
            super(cause);
        }
    }
}
