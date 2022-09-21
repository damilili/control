package com.hoody.commonbase.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.hoody.commonbase.log.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class FileUtils {
    private static final String TAG = "FileUtils";
    public static final String DOCUMENTS_DIR = "documents";

    /**
     * 读取文件
     *
     * @param file
     * @return
     */
    public static byte[] readFileByte(File file) {
        FileInputStream fis = null;
        FileChannel fc = null;
        byte[] data = null;
        try {
            InputStream in = new FileInputStream(file);
            fis = (FileInputStream) in;
            fc = fis.getChannel();
            data = new byte[(int) fc.size()];
            fc.read(ByteBuffer.wrap(data));
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            if (fc != null)
                try {
                    fc.close();
                } catch (IOException localIOException1) {
                }
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException localIOException2) {
                }
        } catch (Exception e) {
            e.printStackTrace();

            if (fc != null)
                try {
                    fc.close();
                } catch (IOException localIOException3) {
                }
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException localIOException4) {
                }
        } finally {
            if (fc != null)
                try {
                    fc.close();
                } catch (IOException localIOException5) {
                }
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException localIOException6) {
                }
        }
        return data;
    }

    /**
     * 写入文件
     *
     * @param bytes
     * @param file
     * @return
     */
    public static boolean writeByteFile(byte[] bytes, File file) {
        if (bytes == null) {
            return false;
        }

        boolean flag = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
            flag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            if (fos != null)
                try {
                    fos.flush();
                } catch (Exception localException1) {
                }
            try {
                fos.close();
            } catch (IOException localIOException1) {
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (fos != null)
                try {
                    fos.flush();
                } catch (Exception localException2) {
                }
            try {
                fos.close();
            } catch (IOException localIOException2) {
            }
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (Exception localException3) {
                }
                try {
                    fos.close();
                } catch (IOException localIOException3) {
                }
            }
        }
        return flag;
    }

    public static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            return null;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = context.getPackageName() + ".hoody.fileprovider";
            Logger.d(TAG, "getUriForFile() called with: authority = [" + authority);
            uri = FileProvider.getUriForFile(context, authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 通过文件全名称拿到文件名
     **/
    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('/');
        return filename.substring(index + 1);
    }

    /***
     *获取documents路径文件
     */
    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), DOCUMENTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    @Nullable
    public static File generateFileName(@Nullable String name, File directory) {
        if (name == null) {
            return null;
        }

        File file = new File(directory, name);

        if (file.exists()) {
            String fileName = name;
            String extension = "";
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int index = 0;

            while (file.exists()) {
                index++;
                name = fileName + '(' + index + ')' + extension;
                file = new File(directory, name);
            }
        }

        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return file;
    }

    /**
     * 通过uri保存文件
     */
    public static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            is.read(buf);
            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * 通过uri获取在documents新生成文件并且获取新文件的文件名
     * */
    public static String getDestinationPath(Context context, Uri uri, String destinationFileName) {
        //拿到documents文件
        File cacheDir = getDocumentCacheDir(context);
        //生成新文件
        File file = generateFileName(destinationFileName, cacheDir);
        //新文件的地址获取
        String destinationPath = null;
        if (file != null) {
            destinationPath = file.getAbsolutePath();
            saveFileFromUri(context, uri, destinationPath);
        }
        return destinationPath;
    }

}
