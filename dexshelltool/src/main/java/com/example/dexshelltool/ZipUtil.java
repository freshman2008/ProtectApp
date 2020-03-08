package com.example.dexshelltool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
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
//                if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF") || name.equals("META-INF/MANIFEST.MF")) {
                if (name.contains("META-INF/")) {
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
/*
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
    *//** 缓冲器大小 *//*
    private static final int BUFFER = 512;

    *//**压缩得到的文件的后缀名*//*
    private static final String SUFFIX=".zip";

    public static void compress(File dir, File zipFileName) {
        File[] fileList = dir.listFiles();

        byte[] buffer = new byte[BUFFER];
        ZipEntry zipEntry = null;
        int readLength = 0;     //每次读取出来的长度
        try {
            // 对输出文件做CRC32校验
            CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
                    zipFileName), new CRC32());
            ZipOutputStream zos = new ZipOutputStream(cos);
            for (File file : fileList) {
                if(file.isFile()){   //若是文件，则压缩文件

                    zipEntry=new ZipEntry(getRelativePath(file.getAbsolutePath(),file));  //
                    zipEntry.setSize(file.length());
                    zipEntry.setTime(file.lastModified());
                    zos.putNextEntry(zipEntry);

                    InputStream is=new BufferedInputStream(new FileInputStream(file));

                    while ((readLength=is.read(buffer,0,BUFFER))!=-1){
                        zos.write(buffer,0,readLength);
                    }
                    is.close();
                    System.out.println("file compress:"+file.getCanonicalPath());
                }else {     //若是空目录，则写入zip条目中
                    zipEntry=new ZipEntry(getRelativePath(file.getAbsolutePath(),file));
                    zos.putNextEntry(zipEntry);
                    System.out.println("dir compress: " + file.getCanonicalPath()+"/");
                }
            }
            zos.close();  //最后得关闭流，不然压缩最后一个文件会出错
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRelativePath(String dirPath,File file) {
        File dirFile = new File(dirPath);
        String relativePath = file.getName();

        while (true) {
            file = file.getParentFile();
            if (file == null) break;
            if (file.equals(dirFile)) {
                break;
            } else {
                relativePath = file.getName() + "/" + relativePath;
            }
        }
        return relativePath;
    }*/

    public static void toZip(File sourceFile, File zipFileName)
            throws RuntimeException{

        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;

        try {
            FileOutputStream out = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(out);
            compress(sourceFile,zos,sourceFile.getName(),true);
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) +" ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static final int  BUFFER_SIZE = 2 * 1024;
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean KeepDirStructure) throws Exception{
        byte[] buf = new byte[BUFFER_SIZE];
        if(sourceFile.isFile()){
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            //是文件夹
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(KeepDirStructure){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }

            }else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        String filename = null;
                        if (name.equals("fakeApk")) {
                            filename = file.getName();
                        } else {
                            filename =  name + "/" + file.getName();
                        }
                        compress(file, zos, filename, KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }

                }
            }
        }
    }
}
