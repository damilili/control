package com.hoody.commonbase.util;

import android.content.Context;
import android.os.Build;
import android.os.Process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;

/**
 * Created by cdm on 2021/11/18.
 */
public class CrashUtil {
    private static final String CRASH_DIR_NAME = "crash";
    private static String CRASH_DIR;

    public static void init(Context context) {
        final File crashDir = context.getDir(CRASH_DIR_NAME, Context.MODE_PRIVATE);
        CRASH_DIR = crashDir.getAbsolutePath();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                String currentTimeInString = TimeUtils.getCurrentTimeInString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                if (!crashDir.exists()) {
                    crashDir.mkdirs();
                }
                File file = new File(crashDir, currentTimeInString);
                try {
                    PrintWriter printWriter = new PrintWriter(new FileOutputStream(file));
                    printWriter.println("crash information :");
                    printWriter.println("time :" + currentTimeInString);
//                    printWriter.println("VERSION_CODE = " + BuildConfig.VERSION_CODE);
//                    printWriter.println("VERSION_NAME = " + BuildConfig.VERSION_NAME);
                    printWriter.println("OS VERSION = " + Build.VERSION.RELEASE);
                    printWriter.println("Vendor = " + Build.MANUFACTURER);
                    printWriter.println("Model = " + Build.MODEL);
                    printWriter.println("CPU_ABI = " + Build.CPU_ABI);
                    printWriter.println("CPU_ABI2 = " + Build.CPU_ABI2);
                    printWriter.println();
                    e.printStackTrace(printWriter);
                    printWriter.println();
                    printWriter.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                Process.killProcess(Process.myPid());
            }
        });
    }

    public static String getCrashDir() {
        return CRASH_DIR;
    }
}
