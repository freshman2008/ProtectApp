package com.example.shell;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class StubApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        File apkFile = new File(getApplicationInfo().sourceDir);
        File app = new File(getDir("fake_apk", MODE_PRIVATE), "apk");

        if (!app.exists()) {
            ZipUtil.upZip(apkFile, app);
        }
        File[] files = app.listFiles();
        for (File file : files) {
            if(file.equals("classes.dex")) {
                byte[] content = DexFileUtil.getBytes(file);
                byte[] dexLenArray = new byte[4];
                System.arraycopy(content, content.length-4, dexLenArray, 0,4);
                int dexLen = DexFileUtil.byteArrayToInt(dexLenArray);
                byte[] mainDexEncrypt = new byte[dexLen];
                System.arraycopy(content, content.length -4 - dexLen, mainDexEncrypt, 0, dexLen);
                byte[] mainDexBytes = AESUtil.decrypt(mainDexEncrypt);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(mainDexBytes);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                byte[] content = DexFileUtil.getBytes(file);
                byte[] dexBytes = AESUtil.decrypt(content);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    fos.write(dexBytes);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        List<File> dexFiles = new ArrayList<>();
        for(File file:app.listFiles()) {
            if(file.getName().endsWith(".dex")) {
                dexFiles.add(file);
            }
        }

        File optimizedDirectory = new File(getDir("opt", MODE_PRIVATE), "opt");
        try {
            V19.install(getClassLoader(), dexFiles, optimizedDirectory);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }
}
