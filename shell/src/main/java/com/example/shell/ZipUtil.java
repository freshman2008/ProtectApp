package com.example.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static int BUFFERSIZE = 2 << 10;

    /**
     * 解压apk文件到指定目录下
     *
     * @param fileName
     * @param dir
     */
    public static void upZip(File fileName, File dir) {
        try {
            dir.delete();
            ZipFile zipFile = new ZipFile(fileName);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF") || name.equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                if (!zipEntry.isDirectory()) {
                    File file = new File(dir, name);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream os = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    int len = 1024;
                    byte[] buffer = new byte[1024];
                    while ((len=is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    is.close();
                    os.close();
                }
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void zip(File fakeApk, File unsignedApk) {
//
//    }
    public static void zip(File dir, File fileName)
    {

        ZipOutputStream zos = null;
        try
        {
            zos = new ZipOutputStream(new FileOutputStream(fileName));
            File[] files = dir.listFiles();
            for (File file:files) {
                String relativePath = file.getName();
                if(file.isDirectory()) {
                    relativePath += File.separator;
                }
                zipFile(file, relativePath, zos);
            }
//            for(String filePath : paths)
//            {
//                //递归压缩文件
//                File file = new File(filePath);
//                String relativePath = file.getName();
//                if(file.isDirectory())
//                {
//                    relativePath += File.separator;
//                }
//                zipFile(file, relativePath, zos);
//            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(zos != null)
                {
                    zos.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void zipFile(File file, String relativePath, ZipOutputStream zos)
    {
        InputStream is = null;
        try
        {
            if(!file.isDirectory())
            {
                ZipEntry zp = new ZipEntry(relativePath);
                zos.putNextEntry(zp);
                is = new FileInputStream(file);
                byte[] buffer = new byte[BUFFERSIZE];
                int length = 0;
                while ((length = is.read(buffer)) >= 0)
                {
                    zos.write(buffer, 0, length);
                }
                zos.flush();
                zos.closeEntry();
            }
            else
            {
                String tempPath = null;
                for(File f: file.listFiles())
                {
                    tempPath = relativePath + f.getName();
                    if(f.isDirectory())
                    {
                        tempPath += File.separator;
                    }
                    zipFile(f, tempPath, zos);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(is != null)
                {
                    is.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
