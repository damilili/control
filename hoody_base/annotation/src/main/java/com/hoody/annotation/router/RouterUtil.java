package com.hoody.annotation.router;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;


/**
 * Created by cdm on 2021/10/21.
 */
public class RouterUtil {
    private static final String TAG = "RouterUtil";
    private boolean inited = false;
    public static final String STARTPARAM_STR_FRAGMENT_NAME = "StartParam_str_fragment_name";
    public static final String STARTPARAM_STR_FRAGMENT_OPT = "StartParam_str_fragment_opt";
    public static final String FRAGMENT_OPT_OPEN = "fragment_opt_open";
    public static final String FRAGMENT_OPT_CLOSE = "fragment_opt_close";

    private RouterUtil() {
    }

    private static RouterUtil instance = new RouterUtil();

    public static RouterUtil getInstance() {
        return instance;
    }

    public void init(Context context) {
        if (inited) {
            return;
        }
        inited = true;
        try {
            DexFile dexFile = new DexFile(context.getPackageCodePath());
            Enumeration<String> entries = dexFile.entries();
            List<String> classNames = new ArrayList<>();
            while (entries.hasMoreElements()) {
                String s = entries.nextElement();
                if (s.contains(RouterConfig.packagerName)) {
                    classNames.add(s);
                }
            }
            try {
                for (String className : classNames) {
                    Class<?> aClass = Class.forName(className);
                    if (IRouterCollector.class.isAssignableFrom(aClass)) {
                        IRouterCollector iRouter = (IRouterCollector) aClass.newInstance();
                        iRouter.putPath();
                    }
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void navigateTo(Context context, String path, Bundle data) {
        navigateTo(context, RouterConfig.DEFAULT_ACTIVITY_PATH, path, data);
    }

    public void navigateTo(Context context, String mainPath, String subPath, Bundle data) {
        navigateTo(context, mainPath, subPath, data, Integer.MIN_VALUE);
    }

    public void navigateTo(Context context, String mainPath, String subPath, Bundle data, int requestCode) throws IllegalArgumentException {
        Class<? extends Activity> activityClass = RouterHelper.getInstance().getActivity(mainPath);
        if (activityClass == null) {
            Log.d(TAG, "没有找到对应的页面 mainPath = " + mainPath + ", subPath = " + subPath);
            return;
        }
        Class<? extends Fragment> fragmentClass = null;
        if (!TextUtils.isEmpty(subPath)) {
            fragmentClass = RouterHelper.getInstance().getFragment(subPath);
            if (fragmentClass == null) {
                Log.d(TAG, "没有找到对应的页面 mainPath = " + mainPath + ", subPath = " + subPath);
                return;
            }
        }
        Intent intent = new Intent(context, activityClass);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (data == null) {
            data = new Bundle();
        }
        if (fragmentClass != null) {
            data.putString(STARTPARAM_STR_FRAGMENT_NAME, fragmentClass.getName());
            data.putString(STARTPARAM_STR_FRAGMENT_OPT, FRAGMENT_OPT_OPEN);
        }
        intent.putExtras(data);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        Log.d(TAG, "navigateTo: activity = " + componentName + " fragment = " + (fragmentClass == null ? "" : fragmentClass.getCanonicalName()));
        if (componentName != null) {
            if (requestCode == Integer.MIN_VALUE) {
                context.startActivity(intent);
            } else {
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, requestCode);
                } else {
                    throw new IllegalArgumentException("context 对象必须是Activity实例");
                }
            }
        }
    }


}
