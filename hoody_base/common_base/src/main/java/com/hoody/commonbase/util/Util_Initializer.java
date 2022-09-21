package com.hoody.commonbase.util;

import android.content.Context;

public class Util_Initializer {
    private static boolean isInited = false;

    private Util_Initializer() {
    }

    public static void init(Context context) {
        if (isInited) {
            return;
        }
        if (context == null) {
            throw new NullPointerException("context 不能传 null");
        }
        isInited = true;
        context = context.getApplicationContext();
        SynchronizeUtil.init();
        DeviceInfo.init(context);
        AppInfo.init(context);
        SharedPreferenceUtil.getInstance().init(context);
        //崩溃收集
        CrashUtil.init(context);
    }
}
