package com.hoody.commonbase.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class PermissionUtil {
    private static final String TAG = "PermissionUtil";
    private static final boolean DEBUG = false;
    private static ArrayList<String> PermissionKeys = new ArrayList<>();


    public static int getPermissionRequestCode(Class key) {
        String canonicalName = key.getCanonicalName();
        int result = PermissionKeys.indexOf(canonicalName);
        if (result < 0&&PermissionKeys.add(canonicalName)) {
            result = PermissionKeys.size() - 1;
        }
        return result;
    }

    public static boolean checkPermissionAndRequest(Activity activity, Class aClass, String[] permissions) {
        return checkPermissionAndRequest(activity, getPermissionRequestCode(aClass), permissions);
    }

    public static boolean checkPermissionAndRequest(Activity activity, int requestCode, String[] permissions) {
        if (requestCode < 0) {
            //requestCode 不能小于0
            if (DEBUG) {
                Log.d(TAG, "checkPermissionAndRequest() requestCode 不能小于0");
            }
            return false;
        }
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (permissions == null) {
            return true;
        }
        boolean hasPermission = true;
        for (String s : permissions) {
            hasPermission &= PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(s);
        }
        if (hasPermission) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            return false;
        }
    }
}
