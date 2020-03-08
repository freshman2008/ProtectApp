package com.example.dexshelltool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class SignatureUtil {
    /**
     * 对apk文件进行签名
     *
     * @param unSignedApk
     * @param signedApk
     * @return
     */
    public static boolean signature(File unSignedApk, File signedApk) {
        final String METHOD_NAME = "runCMD";
        String cmd[] = {
                "cmd",
                "/C",
                "jarsigner",
                "-sigalg", "MD5withRSA",
                "-digestalg", "SHA1",
                "-keystore", "dexshelltool/resource/mytest.jks",
                "-storepass", "test123456",
                "-keypass", "test123456",
                "-signedjar", signedApk.getAbsolutePath(),
                unSignedApk.getAbsolutePath(),
                "key0"
        };

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String readLine = br.readLine();
            StringBuilder builder = new StringBuilder();
            while (readLine != null) {
                readLine = br.readLine();
                builder.append(readLine);
            }
            System.out.println(METHOD_NAME + "#readLine: " + builder.toString());

            p.waitFor();
            int i = p.exitValue();
            System.out.println(METHOD_NAME + "#exitValue = " + i);
            if (i == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(METHOD_NAME + "#ErrMsg=" + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }


    public static boolean packApk(File sourceFile, File zipFileName) {
        final String METHOD_NAME = "runCMD";
        String cmd[] = {
                "cmd",
                "/C",
                "apktool",
                "b", sourceFile.getAbsolutePath(),
                "-o", zipFileName.getName(),
        };

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String readLine = br.readLine();
            StringBuilder builder = new StringBuilder();
            while (readLine != null) {
                readLine = br.readLine();
                builder.append(readLine);
            }
            System.out.println(METHOD_NAME + "#readLine: " + builder.toString());

            p.waitFor();
            int i = p.exitValue();
            System.out.println(METHOD_NAME + "#exitValue = " + i);
            if (i == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(METHOD_NAME + "#ErrMsg=" + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
}
