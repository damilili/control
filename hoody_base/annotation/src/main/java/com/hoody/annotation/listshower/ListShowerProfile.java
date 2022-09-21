package com.hoody.annotation.listshower;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列表条目配置
 */
public class ListShowerProfile<T extends IShowerData,BS extends BaseRecyclerShower<T>>{
    private ListShowerProfile() {
    }

    private static final ListShowerProfile instance = new ListShowerProfile();
    private final Map<String, String> mShowerName2ShowerClassName = new HashMap<>();
    private final List<String> mShowerNames = new ArrayList<>();
    private final Map<String, Class<BS>> mShowerName2ShowerClassCache = new HashMap<>();

    public static ListShowerProfile getInstance() {
        return instance;
    }

    public synchronized void collcet(String showerName, Class<? extends BaseRecyclerShower> clazz) {
        if (!mShowerName2ShowerClassName.containsKey(showerName)) {
            mShowerName2ShowerClassName.put(showerName, clazz.getCanonicalName());
            mShowerNames.add(showerName);
        }
    }

    public synchronized int getShowerId(String showerName) {
        return mShowerNames.indexOf(showerName);
    }

    public synchronized String getShowerName(int showerId) {
        return mShowerNames.get(showerId);
    }

    public synchronized Class<?extends BaseRecyclerShower<T>> findShowerById(int showerId) {
        return (Class<? extends BaseRecyclerShower<T>>) get(mShowerNames.get(showerId));
    }

    public synchronized Class<?extends BaseRecyclerShower<T>> findShowerByName(String showerName) {
        return (Class<? extends BaseRecyclerShower<T>>) get(showerName);
    }

    private Class<BS> get(String showerName) {
        Class<BS> showerClass = mShowerName2ShowerClassCache.get(showerName);
        if (showerClass != null) {
            return showerClass;
        }
        String className = mShowerName2ShowerClassName.get(showerName);
        if (!TextUtils.isEmpty(className)) {
            try {
                showerClass = (Class<BS>) Class.forName(className);
                if (showerClass != null) {
                    mShowerName2ShowerClassCache.put(showerName, showerClass);
                }
                return showerClass;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
