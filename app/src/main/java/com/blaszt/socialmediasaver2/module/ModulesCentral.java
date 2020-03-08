package com.blaszt.socialmediasaver2.module;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ModulesCentral {

    private static ModulesCentral mInstance;

    private List<Module> cacheModules;
    private Module module;
    private File baseDir;

    public static synchronized ModulesCentral with(Context context) {
        if (mInstance == null) {
            mInstance = new ModulesCentral(context);
        }

        return mInstance;
    }

    private ModulesCentral() {

    }

    private ModulesCentral(Context context) {
        inializeBaseDir(context);
        loadAllModule(context);
    }

    private void inializeBaseDir(Context context) {
        baseDir = context.getExternalFilesDir("Modules");
        if (baseDir != null && (!baseDir.exists() || !baseDir.isDirectory())) {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }
    }

    private File[] getAllModuleFile() {
        return getBaseDir().listFiles();
    }

    private void loadAllModule(Context context) {
        cacheModules = new ArrayList<>();
        File[] mods = getAllModuleFile();
        for (File mod : mods) {
            cacheModules.add(new Module(context, mod));
        }
    }

    private File getBaseDir() {
        return baseDir;
    }

    public boolean check(String url) {
        for (Module mod : cacheModules) {
            if (mod != null && mod.check(url)) {
                module = mod;
                return true;
            }
        }
        return false;
    }

    public Module get() {
        Module returnedModule = module;
        module = null;
        return returnedModule;
    }

    public List<Module> getModules() {
        return new ArrayList<>(cacheModules);
    }

    public boolean install(String path) {
        InputStream input;
        OutputStream output;

        File mod = new File(path);
        File installedMod = new File(getBaseDir(), mod.getName());
        byte[] buffer = new byte[1024];

        if (!Module.EXTENSION.equalsIgnoreCase(path.substring(path.length() - 4))
                || !mod.exists() || !mod.isFile()) {
            return false;
        }

        try {
            if (installedMod.createNewFile()) {
                input = new BufferedInputStream(new FileInputStream(mod));
                output = new BufferedOutputStream(new FileOutputStream(installedMod));

                while (input.read(buffer) != -1) {
                    output.write(buffer);
                    output.flush();
                }

                return true;
            }
            else return false;
        }
        catch (FileNotFoundException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }
    }
}
