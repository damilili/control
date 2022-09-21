package com.hoody.commonbase.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppInfo {
    private static String VERSION_NAME;
    private static Context Context;
    public static boolean isDebug;

    public static String getVersionName() {
        return VERSION_NAME;
    }

    private static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    private static void initDebugInfo() {
        ApplicationInfo info = Context.getApplicationInfo();
        isDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    static void init(Context context) {
        Context = context;
        initDebugInfo();
        VERSION_NAME = getVersionName(context);
    }
}
