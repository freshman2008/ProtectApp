package com.example.dexshelltool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

public class DexFileUtil {
    /**
     * 获取文件内容
     * @param dexFile
     * @return
     */
    /*public static byte[] getBytes(File dexFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dexFile));
            String temp = null;
            StringBuffer sb = new StringBuffer();
            temp = br.readLine();
            while (temp != null) {
                sb.append(temp);
                temp = br.readLine();
            }
            return sb.toString().getBytes();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public static byte[] getBytes(File dexFile) {
        try {
            FileInputStream fis = new FileInputStream(dexFile);
            int len = -1;
            int fileLength = (int)dexFile.length();
            byte[] content = new byte[fileLength];
            byte[] buffer = new byte[1024];
            int current = 0;
            while ((len = fis.read(buffer, 0, 1024)) != -1 ) {
                System.arraycopy(buffer, 0, content, current, len);
                current += len;
            }
            fis.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * int到byte[] 由高位到低位
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
//        result[0] = (byte)((i >> 24) & 0xFF);
//        result[1] = (byte)((i >> 16) & 0xFF);
//        result[2] = (byte)((i >> 8) & 0xFF);
//        result[3] = (byte)(i & 0xFF);
        result[3] = (byte)((i >> 24) & 0xFF);
        result[2] = (byte)((i >> 16) & 0xFF);
        result[1] = (byte)((i >> 8) & 0xFF);
        result[0] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * byte[]转int
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;

//        value += bytes[0] & 0xFF;
//        value += (bytes[1] & 0xFF) << 8;
//        value += (bytes[2] & 0xFF) << 16;
//        value += (bytes[3] & 0xFF) << 24;
        value |= bytes[0] & 0xFF;
        value |= (bytes[1] & 0xFF) << 8;
        value |= (bytes[2] & 0xFF) << 16;
        value |= (bytes[3] & 0xFF) << 24;

        return value;
    }

    public static void modifyCheckSum(byte[] newMainDex) {
//        byte[] header = new byte[112];
//        System.arraycopy(newMainDex, 0, header, 0, 112);
        byte[] buffer = new byte[newMainDex.length - 12];
        System.arraycopy(newMainDex, 12, buffer, 0, newMainDex.length - 12);
        Adler32 adler32 = new Adler32();
        adler32.update(buffer);
        int checkSumValue = (int)adler32.getValue();
        byte[] checkSum = DexFileUtil.intToByteArray(checkSumValue);
        System.arraycopy(checkSum, 0, newMainDex, 8, 4);
    }

    public static void modifySignature(byte[] newMainDex) throws NoSuchAlgorithmException {
        byte[] buffer = new byte[newMainDex.length - 32];
        System.arraycopy(newMainDex, 32, buffer, 0, newMainDex.length - 32);

        byte[] signature = SHAUtil.sha1(buffer);
        System.arraycopy(signature, 0, newMainDex, 12, 20);
    }

    public static void modifyFileSize(byte[] newMainDex, byte[] lenArray) {
        System.arraycopy(lenArray, 0, newMainDex, 32, 4);
    }
}
