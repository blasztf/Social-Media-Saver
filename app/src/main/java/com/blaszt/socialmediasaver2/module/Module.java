package com.blaszt.socialmediasaver2.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.Toast;

import com.blaszt.modulelinker.Base;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import dalvik.system.DexClassLoader;

public class Module extends Base {
    private Class<?> moduleClass;
    private Object moduleInstance;

    private WeakReference<Context> contextWeakReference;

    public Module(Context context, String apkName) {
        contextWeakReference = new WeakReference<>(context);

        apkName = sanitizeApkName(apkName);
        DexClassLoader classLoader = getDexClassLoader(context, apkName);
        try {
            moduleClass = classLoader.loadClass(getModuleClass(context, apkName));
            moduleInstance = moduleClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (InstantiationException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ModuleNotValid(e.getMessage());
        }
    }

    public static File[] getAllModulesApk(Context context) {
        return getModulesBaseDir(context).listFiles();
    }

    public static File getModulesBaseDir(Context context) {
        File modulesDirectory;
        modulesDirectory = new File(context.getExternalFilesDir("Modules"), String.format("%smodules%s", File.separator, File.separator));
        if (!modulesDirectory.exists() || !modulesDirectory.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            modulesDirectory.mkdirs();
        }
        return modulesDirectory;
    }

    private DexClassLoader getDexClassLoader(Context context, String apkName) {
        DexClassLoader dexClassLoader;
        dexClassLoader = new DexClassLoader(getApkFile(context, apkName).getAbsolutePath(), getOptimizedDirectory(context).getAbsolutePath(), null, ClassLoader.getSystemClassLoader().getParent());
        return dexClassLoader;
    }

    private String sanitizeApkName(String apkName) {
        int lastPath = apkName.lastIndexOf(File.separator);
        if (lastPath != -1) {
            apkName = apkName.substring(lastPath + 1);
        }
        return apkName.split("\\.")[0];
    }

    private File getApkFile(Context context, String apkName) {
        File apkFile;
        apkFile = new File(getModulesBaseDir(context), String.format("%s.apk", apkName));
        return apkFile;
    }

    private File getOptimizedDirectory(Context context) {
        File codeCacheDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            codeCacheDir = context.getCodeCacheDir();
        } else {
            codeCacheDir = context.getDir("outdex", Context.MODE_PRIVATE);
        }
        return codeCacheDir;
    }

    private String getModuleClass(Context context, String apkName/*, String className*/) {
        String className = ".Module";
        return context.getApplicationContext().getPackageName() + ".module" + (apkName != null ? ("." + apkName.toLowerCase(Locale.US) + className) : className);
    }

    @Override
    public String[] findMediaURL(String url) {
        try {
            Method method = moduleClass.getMethod("findMediaURL", String.class);
            return (String[]) method.invoke(moduleInstance, url); // Media URL
        } catch (NoSuchMethodException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ModuleNotValid(e.getMessage());
        }
    }

    @Override
    public boolean check(String url) {
        try {
            Method method = moduleClass.getMethod("check", String.class);
            return (Boolean) method.invoke(moduleInstance, url); // Is Valid
        } catch (NoSuchMethodException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ModuleNotValid(e.getMessage());
        }
    }

    @Override
    public String getBaseDir() {
        try {
            Method method = moduleClass.getMethod("getBaseDir");
            return (String) method.invoke(moduleInstance); // Base Dir
        } catch (NoSuchMethodException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ModuleNotValid(e.getMessage());
        }
    }

    @Override
    public String getName() {
        try {
            Method method = moduleClass.getMethod("getName");
            return (String) method.invoke(moduleInstance); // App Name
        } catch (NoSuchMethodException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ModuleNotValid(e.getMessage());
        }
    }

    @Override
    protected String getEncodedImage() {
        try {
            Method method = moduleClass.getMethod("getEncodedImage");
            return (String) method.invoke(moduleInstance); // App Encoded Logo
        } catch (NoSuchMethodException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ModuleNotValid(e.getMessage());
        }
    }

    @Override
    public String getLogo() {
        try {
            Method method = moduleClass.getMethod("getLogo");
            return (String) method.invoke(moduleInstance); // App Logo
        } catch (NoSuchMethodException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ModuleNotValid(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ModuleNotValid(e.getMessage());
        }
    }

    private void log(Exception e) {
        if (true) {
            Toast.makeText(contextWeakReference.get(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void log(String msg) {
        if (true) {
            Toast.makeText(contextWeakReference.get(), msg, Toast.LENGTH_LONG).show();
        }
    }

    private class ModuleNotValid extends RuntimeException {
        ModuleNotValid(String message) {
            super(message);
        }
    }

    public static class ModuleData implements Serializable {
        /**
         * Serial version UID.
         */
        public static final long serialVersionUID = 1352522154948736343L;

        private String name;
        private String logoPath;

        public ModuleData(String name, String logoPath) {
            this.name = name;
            this.logoPath = logoPath;
        }

        public String getName() {
            return name;
        }

        public String getLogo() {
            return logoPath;
        }
    }
}
