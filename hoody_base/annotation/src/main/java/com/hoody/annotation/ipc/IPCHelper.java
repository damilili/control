package com.hoody.annotation.ipc;


import android.os.Binder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by cdm on 2021/10/21.
 */
public class IPCHelper {
    private IPCHelper() {
    }

    private static IPCHelper instance = new IPCHelper();
    private Map<String, Class<Binder>> interfaceImplement = new HashMap<>();

    public static IPCHelper getInstance() {
        return instance;
    }

    public void putInterfaceImplement(String path, Class clazz) {
        if (Binder.class.isAssignableFrom(clazz)) {
            if (!interfaceImplement.containsKey(path)) {
                interfaceImplement.put(path, clazz);
            }
        } else {
            Log.d("IPCHelper", "error putInterfaceImplement()  called with: path = [" + path + "], clazz = [" + clazz + "]");
        }
    }

    public Class getInterfaceImplement(String path) {
        return interfaceImplement.get(path);
    }
}
