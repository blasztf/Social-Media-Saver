package com.blaszt.socialmediasaver2.plugin;

import android.content.Context;
import android.os.Build;

import com.blaszt.socialmediasaver2.AppSettings;
import com.blaszt.socialmediasaver2.helper.data.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class ModPluginEngine {
    private static ModPluginEngine mInstance;
    private List<ModPlugin> mModPluginContainer;
    private String mBaseDirPath;

    public static ModPluginEngine getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ModPluginEngine(context);
        }

        return mInstance;
    }

    private ModPluginEngine(Context context) {
        mModPluginContainer = new ArrayList<>();
        ModPluginEngine
                .with(context)
                .use(new ModPluginNet())
                .setup(this);
    }

    private void add(ModPlugin plugin) {
        mModPluginContainer.add(plugin);
    }

    private void setBaseDirPath(String baseDirPath) {
        mBaseDirPath = baseDirPath;
    }

    public String getPluginBaseDirPath(ModPlugin plugin) {
        return String.format(mBaseDirPath, plugin.getName());
    }

    public List<ModPlugin> each() {
        return mModPluginContainer;
    }

    public static ModPluginEngine.Wizard with(Context context) {
        return new ModPluginEngine.Wizard(context);
    }

    public static class Wizard extends DependInjectContext {
        private ModPluginNet mPluginNet;

        protected Wizard(Context context) {
            super(context);
        }

        public Wizard use(ModPluginNet pluginNet) {
            mPluginNet = pluginNet;
            return this;
        }

        private void setup(ModPluginEngine engine) {
            String baseDirPath = AppSettings.getInstance(getContext()).storageMedia() + "%s" + File.separator;
            StorageUtils.isDirectoryExists(baseDirPath, true);
            engine.setBaseDirPath(baseDirPath);

            File[] pluginFiles = getBaseDir().listFiles();
            install(engine, pluginFiles);
        }

        public void install(ModPluginEngine engine, File... pluginFiles) {
            DexClassLoader dexClassLoader;
            ModPlugin plugin;

            for (File pluginFile : pluginFiles) {
                dexClassLoader = new DexClassLoader(pluginFile.getAbsolutePath(), getOptimizedDirectory().getAbsolutePath(), null, getClass().getClassLoader());
                plugin = new ModPlugin(dexClassLoader, getPluginClassName(pluginFile), mPluginNet);
                engine.add(plugin);
            }
        }

        private String debug(String path) {
            String print = "";
            try {
                DexFile dx = DexFile.loadDex(path, File.createTempFile("opt", "dex",
                        getContext().getCacheDir()).getPath(), 0);
                // Print all classes in the DexFile
                for(Enumeration<String> classNames = dx.entries(); classNames.hasMoreElements();) {
                    String className = classNames.nextElement();
                    print += ("class: " + className + "\n");
                }
            } catch (IOException e) {
                print += ("Error opening " + path + "\n" + e.getMessage());
            }

            return print;
        }

//        public void install(ModPluginEngine engine, String... paths) {
//            DexClassLoader dexClassLoader;
//            ModPlugin plugin;
//            File pluginFile;
//
//            for (String path : paths) {
//                pluginFile = new File(path);
//                dexClassLoader = getDexClassLoader(pluginFile);
//                plugin = new ModPlugin(dexClassLoader, getPluginClassName(pluginFile), mPluginNet);
//                engine.add(plugin);
//            }
//
        private File getBaseDir() {
            File baseDir = getContext().getExternalFilesDir("plugins");
            if (baseDir != null && (!baseDir.exists() || !baseDir.isDirectory())) {
                //noinspection ResultOfMethodCallIgnored
                baseDir.mkdirs();
            }
            return baseDir;
        }

        private String getPluginName(File pluginFile) {
            String pluginName = pluginFile.getName();
            int lastPath = pluginName.lastIndexOf(File.separator);
            if (lastPath != -1) {
                pluginName = pluginName.substring(lastPath + 1);
            }

            if (!ModPlugin.EXTENSION.equalsIgnoreCase(pluginName.substring(pluginName.length() - ModPlugin.EXTENSION.length()))) throw new IllegalStateException("Wrong mod file extension!");
            else return pluginName.split("\\.")[0];
        }

        private String getPluginClassName(File pluginFile) {
            int idx = 0;
            StringBuilder classNameBuilder = new StringBuilder(getPluginName(pluginFile));
            classNameBuilder.setCharAt(idx, Character.toTitleCase(classNameBuilder.charAt(idx)));
            while ((idx = classNameBuilder.indexOf("-", idx)) != -1) {
                classNameBuilder.deleteCharAt(idx);
                classNameBuilder.setCharAt(idx, Character.toTitleCase(classNameBuilder.charAt(idx)));
            }
            return getContext().getApplicationContext().getPackageName() + ".plugin." + classNameBuilder.toString();
        }

        private File getOptimizedDirectory() {
            File codeCacheDir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                codeCacheDir = getContext().getCodeCacheDir();
            } else {
                codeCacheDir = getContext().getDir("outdex", Context.MODE_PRIVATE);
            }
            return codeCacheDir;
        }
    }
}
