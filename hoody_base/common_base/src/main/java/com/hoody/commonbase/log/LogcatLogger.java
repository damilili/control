package com.hoody.commonbase.log;

import android.util.Log;

/**
 * 控制台输出
 */
class LogcatLogger extends AbsLogger {

    @Override
    void e(String tag, String msg, Throwable e) {
        if (enable())
            Log.e(tag, msg, e);
    }


    @Override
    void v(String tag, String msg) {
        if (enable())
            Log.v(tag, msg);
    }

    @Override
    void d(String tag, String msg) {
        if (enable())
            Log.d(tag, msg);
    }

    @Override
    void i(String tag, String msg) {
        if (enable())
            Log.i(tag, msg);
    }

    @Override
    void w(String tag, String msg) {
        if (enable())
            Log.w(tag, msg);
    }

    @Override
    void e(String tag, String msg) {
        if (enable())
            Log.e(tag, msg);
    }
}

