package com.example.shell;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
        Log.d("hello", "apkFile path:" + apkFile.getAbsolutePath());
        Log.d("hello", "apkFile name:" + apkFile.getName());
        /**
         * 应用首次启动时，进行dex文件解密。
         * 之后启动时不再解密，直接运行。
         */
        if (!app.exists()) {
            //apk文件解压到指定目录
            ZipUtil.upZip(apkFile, app);
            AESUtil.init(getPassword());

            //过来文件夹获取dex文件数组
            File[] files = app.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".dex");
                }
            });
            for (File file : files) {
                Log.d("hello", "file:" + file.getName());
                if(file.getName().equals("classes.dex")) {
                    //获取壳dex(合并了加密的dex文件)文件内容
                    byte[] content = DexFileUtil.getBytes(file);
                    //获取加密dex文件的长度
                    byte[] dexLenArray = new byte[4];
                    System.arraycopy(content, content.length-4, dexLenArray, 0,4);
                    int dexLen = DexFileUtil.byteArrayToInt(dexLenArray);
                    //获取加密dex的内容
                    byte[] mainDexEncrypt = new byte[dexLen];
                    System.arraycopy(content, content.length - 4 - dexLen, mainDexEncrypt, 0, dexLen);
                    //解密dex文件
                    Log.d("hello", "dex文件解密中......");
                    byte[] mainDexBytes = AESUtil.decrypt(mainDexEncrypt);
                    if(mainDexBytes == null || mainDexBytes.length ==0) {
                        throw new RuntimeException("dex文件异常.");
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(mainDexBytes);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("hello", "dex文件解密完成.");
                } else {
                    //非主dex文件，直接解密即可
                    Log.d("hello", "dex文件解密中......");
                    byte[] content = DexFileUtil.getBytes(file);
                    byte[] dexBytes = AESUtil.decrypt(content);
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(dexBytes);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("hello", "dex文件解密完成.");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实际产品应该从后台获取解密秘钥
     * @return
     */
    private String getPassword() {
        return "1122334455667788";
    }
}
