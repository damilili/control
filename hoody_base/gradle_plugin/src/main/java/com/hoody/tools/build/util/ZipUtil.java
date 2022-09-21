package com.hoody.tools.build.util;


import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {
    /**
     * 解压到指定目录
     */
    public static void unZipFiles(String zipPath, String descDir) throws IOException {
        unZipFiles(new File(zipPath), descDir);
    }

    /**
     * 解压文件到指定目录
     */
    @SuppressWarnings("rawtypes")
    public static void unZipFiles(File zipFile, String descDir) throws IOException {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        //解决zip文件中有中文目录或者中文文件
        ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));
        for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            System.out.println("zipEntryName =" + zipEntryName);
            String outPath = (descDir + File.separator + zipEntryName).replaceAll("\\*", File.separator);
            System.out.println("outPath =" + outPath);
            //判断路径是否存在,不存在则创建文件路径

            File file = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
            if (!file.exists()) {
                file.mkdirs();
            }
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if (new File(outPath).isDirectory()) {
                continue;
            }
            //输出文件路径信息
            System.out.println(outPath);
            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
        }
        System.out.println("******************解压完毕********************");
    }

    static int BUFFER=1024;

    public static void unzip(File zip, File unZipDir) throws IOException {
        if (!zip.exists()) {
            return;
        }
        if (!unZipDir.exists()) {
            unZipDir.mkdirs();
        }
        if (!unZipDir.isDirectory()) {
            return;
        }
        int fileCount = 0;
        ZipFile zipfile = new ZipFile(zip);
        BufferedOutputStream ds = null;
        BufferedInputStream is = null;
        ZipEntry entry = null;
        Enumeration entries = zipfile.entries();
        System.out.println("Start...");
        while (entries.hasMoreElements()) {
            try {
                entry = (ZipEntry) entries.nextElement();
                System.out.println((++fileCount) + ") Extracting: " + entry);
                File tempFile = new File(unZipDir.getAbsolutePath() + "/"
                        + entry.getName());
                if (entry.isDirectory()) {
                    tempFile.mkdirs();
                    continue;
                } else if (!tempFile.getParentFile().exists()) {
                    tempFile.getParentFile().mkdirs();
                }

                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(unZipDir
                        .getAbsolutePath()
                        + "/" + entry.getName());
                ds = new BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER)) != -1) {
                    ds.write(data, 0, count);
                }
                ds.flush();

            } catch (IOException ex) {
                throw ex;

            } finally {
                try {
                    if (ds != null)
                        ds.close();

                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    // ignore

                }
            }
        }
        System.out.println("Finished !");
    }
}


