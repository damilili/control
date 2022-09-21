package com.hoody.commonbase.log;

/**
 *日志抽象基类
 */
abstract class AbsLogger {

    int VERBOSE = 2;

    int DEBUG = 3;

    int INFO = 4;

    int WARN = 5;

    int ERROR = 6;

    int ASSERT = 7;
    private boolean enable = true;

    public boolean enable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    abstract void v(String tag, String msg);

    abstract void d(String tag, String msg);

    abstract void i(String tag, String msg);

    abstract void w(String tag, String msg);

    abstract void e(String tag, String msg);

    abstract void e(String tag, String msg, Throwable e);
}
