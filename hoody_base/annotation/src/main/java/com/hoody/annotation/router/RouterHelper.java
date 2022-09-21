package com.hoody.annotation.router;


import android.app.Activity;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;

/**
 * Created by cdm on 2021/10/21.
 */
public class RouterHelper {
    private RouterHelper() {
    }

    private static RouterHelper instance = new RouterHelper();
    private Map<String, String> path2Activity = new HashMap<>();
    private Map<String, String> path2Fragment = new HashMap<>();

    public static RouterHelper getInstance() {
        return instance;
    }

    public void putPath(String path, Class clazz) {
        if (!path2Activity.containsKey(path)) {
            if (Activity.class.isAssignableFrom(clazz)) {
                path2Activity.put(path, clazz.getCanonicalName());
            } else if (Fragment.class.isAssignableFrom(clazz)) {
                path2Fragment.put(path, clazz.getCanonicalName());
            }
        }
    }

    public Class<? extends Activity> getActivity(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String className = path2Activity.get(path.trim());
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        Class<? extends Activity> activityClass = null;
        try {
            activityClass = (Class<? extends Activity>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return activityClass;
    }

    public Class<? extends Fragment> getFragment(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String className = path2Fragment.get(path.trim());
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        Class<? extends Fragment> fragmentClass = null;
        try {
            fragmentClass = (Class<? extends Fragment>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return fragmentClass;
    }
}
