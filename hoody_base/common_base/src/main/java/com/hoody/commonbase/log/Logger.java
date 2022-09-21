package com.hoody.commonbase.log;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志工具
 */
public class Logger {
    private static boolean enable = true;

    public static boolean enable() {
        return enable;
    }

    public static void setEnable(boolean enable) {
        Logger.enable = enable;
    }

    public static void init(Context context) {
        Loggers.add(new LogcatLogger());
        Loggers.add(new FileLogger(context));
    }

    private static String PACKAGE_NAME;
    private static final List<AbsLogger> Loggers = new ArrayList<>(4);

    public static void v(String tag, String msg) {
        if (enable()) {
            msg = getFileNameLineNum() + msg;
            for (AbsLogger iLogger : Loggers) {
                if (iLogger != null && iLogger.enable()) {
                    iLogger.v(tag, msg);
                }
            }
        }

    }

    public static void d(String tag, String msg) {
        if (enable()) {
            msg = getFileNameLineNum() + msg;
            for (AbsLogger iLogger : Loggers) {
                if (iLogger.enable()) {
                    iLogger.d(tag, msg);
                }
            }
        }
    }

    public static void i(String tag, String msg) {
        if (enable()) {
            msg = getFileNameLineNum() + msg;
            for (AbsLogger iLogger : Loggers) {
                if (iLogger.enable()) {
                    iLogger.i(tag, msg);
                }
            }
        }
    }

    public static void w(String tag, String msg) {
        if (enable()) {
            msg = getFileNameLineNum() + msg;
            for (AbsLogger iLogger : Loggers) {
                if (iLogger.enable()) {
                    iLogger.w(tag, msg);
                }
            }
        }
    }

    public static void e(String tag, String msg) {
        if (enable()) {
            msg = getFileNameLineNum() + msg;
            for (AbsLogger iLogger : Loggers) {
                if (iLogger.enable()) {
                    iLogger.e(tag, msg);
                }
            }
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (enable()) {
            msg = getFileNameLineNum() + msg;
            for (AbsLogger iLogger : Loggers) {
                if (iLogger.enable()) {
                    iLogger.e(tag, msg, e);
                }
            }
        }
    }

    /**
     * 获取输出日志的类文件名和行数
     */
    private static String getFileNameLineNum() {
        if (PACKAGE_NAME == null) {
            PACKAGE_NAME = LogcatLogger.class.getPackage().getName();
        }
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().contains(PACKAGE_NAME)) {
                continue;
            }
            return st.getFileName() + "[Line: " + st.getLineNumber() + "] ";
        }
        return null;
    }
}
