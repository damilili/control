package com.hoody.commonbase.util;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SynchronizeUtil {
    private static final String TAG = "SynchronizeUtil";
    private static long mainThreadId;
    private static Handler mainThreadhandler;
    private static ExecutorService mExecutors;

    static void init() {
        mainThreadId = Looper.getMainLooper().getThread().getId();
        mainThreadhandler = new Handler(Looper.getMainLooper());
        mExecutors = Executors.newFixedThreadPool(4);
    }

    private SynchronizeUtil() {

    }

    public static void runSubThread(Runnable runnable) {
        mExecutors.submit(runnable);
    }

    public static void runMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            mainThreadhandler.post(runnable);
        }
    }

    public static void runMainThreadDelay(Runnable runnable, long delayTime) {
        mainThreadhandler.postDelayed(runnable, delayTime);
    }

    public static void removeTask(Runnable runnable) {
        mainThreadhandler.removeCallbacks(runnable);
    }

    private static boolean isMainThread() {
        return Thread.currentThread().getId() == mainThreadId;
    }


}
