package com.example.dexshelltool;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MyClass {
    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        /**
         * 1.处理aar获取壳dex
         */
        File aarFile = new File("dexshelltool/resource/Shell-debug.aar");
        File fakeDex = new File("dexshelltool/resource/temp", "fakeDex");
        ZipUtil.upZip(aarFile, fakeDex);
        File[] files = fakeDex.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals("classes.jar");
            }
        });

        if (files == null ||files.length == 0) {
            throw new IllegalArgumentException("classes.jar file not found.");
        }

        File classesJar = files[0];
        File aarDex = new File(classesJar.getParentFile(), "classes.dex");
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("cmd /C dx --dex --output=" + aarDex.getAbsolutePath() + " " + classesJar.getAbsolutePath());

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("dx run failed.");
        }
        process.destroy();

        /**
         * 2.处理原apk，对dex进行加密
         */
        //解压apk
        File apkFile = new File("dexshelltool/resource/app-debug.apk");
        File fakeApk = new File("dexshelltool/resource/temp", "fakeApk");
        ZipUtil.upZip(apkFile, fakeApk);
        File[] dexFiles = fakeApk.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dex");
            }
        });

        if (dexFiles == null || dexFiles.length ==0) {
            throw new IllegalArgumentException("no dex file found.");
        }

        AESUtil.init("1122334455667788");
        byte[] mainDexData = null;
        File mainDexFile = null;
        for (File dexFile : dexFiles) {
            byte[] content = DexFileUtil.getBytes(dexFile);
            byte[] encryptedContent = AESUtil.encrypt(content);
            if (dexFile.getName().equals("classes.dex")) {
                mainDexFile = dexFile;
                mainDexData = encryptedContent;
            }
            FileOutputStream os = new FileOutputStream(dexFile);
            os.write(encryptedContent);
            os.flush();
            os.close();
        }

        /**
         * 3.创建新的主dex，classes.dex
         */
        byte[] aarData = DexFileUtil.getBytes(aarDex);
        int len = aarData.length + mainDexData.length + 4;
        byte[] lenArray = DexFileUtil.intToByteArray(len);
        byte[] newMainDex = new byte[len];

        System.arraycopy(aarData, 0, newMainDex, 0, aarData.length);
        System.arraycopy(mainDexData, 0, newMainDex, aarData.length, mainDexData.length);
        System.arraycopy(lenArray, 0, newMainDex, aarData.length+mainDexData.length, 4);
        DexFileUtil.modifyFileSize(newMainDex, lenArray);
        DexFileUtil.modifySignature(newMainDex);
        DexFileUtil.modifyCheckSum(newMainDex);

        FileOutputStream outputStream = new FileOutputStream(aarDex);
        outputStream.write(newMainDex);
        outputStream.flush();
        outputStream.close();

        /**
         * 4.签名
         */

        File unsignedApk = new File("dexshelltool/resource/outputs/app-unsigned.apk");
        unsignedApk.getParentFile().mkdirs();
        ZipUtil.toZip(fakeApk, unsignedApk);
        File signedApk = new File("dexshelltool/resource/outputs/app-signed.apk");
        SignatureUtil.signature(unsignedApk, signedApk);

    }
}
