package com.hoody.commonbase.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Author: dhl
 * Date: 2016-05-18
 * Desc:
 */
public class SharedPreferenceUtil {
    private static SharedPreferenceUtil SharedPreferenceUtil = null;

    private SharedPreferenceUtil() {
    }

    public static SharedPreferenceUtil getInstance() {
        if (SharedPreferenceUtil == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (SharedPreferenceUtil == null) {
                    SharedPreferenceUtil = new SharedPreferenceUtil();
                }
            }
        }
        return SharedPreferenceUtil;
    }

    private final static String FILE_NAME = "default_preference";
    private SharedPreferences prefernece;

    void init(Context context) {
        prefernece = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean saveSharedPreferences(String key, boolean value) {
        return saveSharedPreferences(prefernece, key, value);
    }

    public static boolean saveSharedPreferences(SharedPreferences prefernece, String key, boolean value) {
        SharedPreferences.Editor editor = prefernece.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public boolean readSharedPreferences(String key, boolean defValue) {
        return readSharedPreferences(prefernece, key, defValue);
    }

    public static boolean readSharedPreferences(SharedPreferences prefernece, String key, boolean defValue) {
        return prefernece.getBoolean(key, defValue);
    }

    public boolean saveSharedPreferences(String key, String value) {
        return saveSharedPreferences(prefernece, key, value);
    }

    public static boolean saveSharedPreferences(SharedPreferences prefernece, String key, String value) {
        SharedPreferences.Editor editor = prefernece.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public String readSharedPreferences(String key, String defValue) {
        return readSharedPreferences(prefernece, key, defValue);
    }

    public static String readSharedPreferences(SharedPreferences prefernece, String key, String defValue) {
        return prefernece.getString(key, defValue);
    }

    public boolean saveSharedPreferences(String key, int value) {
        return saveSharedPreferences(prefernece, key, value);
    }

    public static boolean saveSharedPreferences(SharedPreferences prefernece, String key, int value) {
        SharedPreferences.Editor editor = prefernece.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public int readSharedPreferences(String key, int defValue) {
        return readSharedPreferences(prefernece, key, defValue);
    }

    public static int readSharedPreferences(SharedPreferences prefernece, String key, int defValue) {
        return prefernece.getInt(key, defValue);
    }

    public boolean saveSharedPreferences(String key, float value) {
        return saveSharedPreferences(prefernece, key, value);
    }

    public static boolean saveSharedPreferences(SharedPreferences prefernece, String key, float value) {
        SharedPreferences.Editor editor = prefernece.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    public float readSharedPreferences(String key, float defValue) {
        return readSharedPreferences(prefernece, key, defValue);
    }

    public static float readSharedPreferences(SharedPreferences prefernece, String key, float defValue) {
        return prefernece.getFloat(key, defValue);
    }

    public boolean saveSharedPreferences(String key, long value) {
        return saveSharedPreferences(prefernece, key, value);
    }

    public static boolean saveSharedPreferences(SharedPreferences prefernece, String key, long value) {
        SharedPreferences.Editor editor = prefernece.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public long readSharedPreferences(String key, long defValue) {
        return readSharedPreferences(prefernece, key, defValue);
    }

    public static long readSharedPreferences(SharedPreferences prefernece, String key, long defValue) {
        return prefernece.getLong(key, defValue);
    }

    public boolean removeKey(String key) {
        return removeKey(prefernece, key);
    }

    public static boolean removeKey(SharedPreferences prefernece, String key) {
        SharedPreferences.Editor editor = prefernece.edit();
        editor.remove(key);
        return editor.commit();
    }

    public boolean removeAllKey() {
        return removeAllKey(prefernece);
    }

    public static boolean removeAllKey(SharedPreferences prefernece) {
        SharedPreferences.Editor editor = prefernece.edit();
        editor.clear();
        return editor.commit();
    }

}

