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
        //解压aar文件到resource/tmp/fakeDex目录下
        File aarFile = new File("dexshelltool/resource/Shell-debug.aar");
        File fakeDex = new File("dexshelltool/resource/tmp", "fakeDex");
        if (fakeDex.getParentFile().exists()) {
            deleteDir(fakeDex.getParent());
        }
        System.out.println("壳aar文件[" + aarFile.getName() + "]解压中......");
        ZipUtil.upZip(aarFile, fakeDex);
        System.out.println("壳aar文件[" + aarFile.getName() + "]解压完成.");
        //获取classes.jar文件
        File[] files = fakeDex.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals("classes.jar");
            }
        });
        if (files == null ||files.length == 0) {
            throw new IllegalArgumentException("classes.jar file not found.");
        }

        //使用dx命令生成壳dex文件
        File classesJar = files[0];
        File aarDex = new File(classesJar.getParentFile(), "classes.dex");
        Runtime runtime = Runtime.getRuntime();
        System.out.println("使用壳jar文件[" + classesJar.getName() + "]生成dex文件中......");
        Process process = runtime.exec("cmd /C dx --dex --output=" + aarDex.getAbsolutePath() + " " + classesJar.getAbsolutePath());
        try {
            process.waitFor();
            if (process.exitValue() != 0) {
                throw new RuntimeException("dx run failed.");
            }
            process.destroy();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
        System.out.println("使用壳jar文件[" + classesJar.getName() + "]生成dex文件完成.");

        /**
         * 2.处理原apk，对dex进行加密
         */
        //解压apk到resource/tmp/fakeApk目录下
        File apkFile = new File("dexshelltool/resource/app-debug.apk");
        File fakeApk = new File("dexshelltool/resource/tmp", "fakeApk");
        System.out.println("apk文件[" + apkFile.getName() + "]解压中......");
        ZipUtil.upZip(apkFile, fakeApk);
        System.out.println("apk文件[" + apkFile.getName() + "]解压完成.");
        //获取dex文件数组
        File[] dexFiles = fakeApk.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dex");
            }
        });
        if (dexFiles == null || dexFiles.length ==0) {
            throw new IllegalArgumentException("no dex file found.");
        }

        //加密dex文件
        AESUtil.init(getPassword());
        byte[] mainDexData = null;
        File mainDexFile = null;
        for (File dexFile : dexFiles) {
            byte[] content = DexFileUtil.getBytes(dexFile);
            System.out.println("dex文件[" + dexFile.getName() + "]加密中......");
            byte[] encryptedContent = AESUtil.encrypt(content);
            if (dexFile.getName().equals("classes.dex")) {
                mainDexFile = dexFile;
                mainDexData = encryptedContent;
            }
            FileOutputStream os = new FileOutputStream(dexFile);
            os.write(encryptedContent);
            os.flush();
            os.close();
            System.out.println("dex文件[" + dexFile.getName() + "]加密完成.");
        }

        /**
         * 3.生成新的主dex，classes.dex
         */
        System.out.println("生成新的主dex文件中[classes.dex]......");
        byte[] aarData = DexFileUtil.getBytes(aarDex);
        int newMainDexLen = aarData.length + mainDexData.length + 4;
        byte[] newMainDexLenArray = DexFileUtil.intToByteArray(newMainDexLen);
//        int xxxx = DexFileUtil.byteArrayToInt(newMainDexLenArray);
        byte[] newMainDex = new byte[newMainDexLen];

        byte[] mainDexLenArray = DexFileUtil.intToByteArray(mainDexData.length);

        System.arraycopy(aarData, 0, newMainDex, 0, aarData.length);
        System.arraycopy(mainDexData, 0, newMainDex, aarData.length, mainDexData.length);
        System.arraycopy(mainDexLenArray, 0, newMainDex, aarData.length+mainDexData.length, 4);
        System.out.println("更新主dex文件header的FileSize域......");
        DexFileUtil.modifyFileSize(newMainDex, newMainDexLenArray);
        System.out.println("更新主dex文件header的Signature域......");
        DexFileUtil.modifySignature(newMainDex);
        System.out.println("更新主dex文件header的CheckSum域......");
        DexFileUtil.modifyCheckSum(newMainDex);

        FileOutputStream outputStream = new FileOutputStream(mainDexFile);
        outputStream.write(newMainDex);
        outputStream.flush();
        outputStream.close();
        System.out.println("新的主dex文件[classes.dex]生成完毕.");

        /**
         * 4.签名
         */

        File unsignedApk = new File("dexshelltool/resource/outputs/app-unsigned.apk");
        if (unsignedApk.getParentFile().exists()) {
            deleteDir(unsignedApk.getParent());
        }
        unsignedApk.getParentFile().mkdirs();
        System.out.println("生成新的未签名apk[" + unsignedApk.getName() + "]......");
        ZipUtil.toZip(fakeApk, unsignedApk);
        File signedApk = new File("dexshelltool/resource/outputs/app-signed.apk");
        System.out.println("生成新的签名apk[" + signedApk.getName() + "]......");
        SignatureUtil.signature(unsignedApk, signedApk);
    }

    private static String getPassword() {
        return "1122334455667788";
    }

    /**
     * 迭代删除文件夹
     * @param dirPath 文件夹路径
     */
    private static void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if(file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if(files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }
}
