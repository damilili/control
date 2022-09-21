package com.hoody.commonbase.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * binder管理客户端，用户客户端获取指定的binder，从而访问对应的服务端
 */
public class BinderManager_Client {

    private static BinderManager_Client ourInstance;
    private com.hoody.commonbase.BinderManager binderManager;

    public static BinderManager_Client getInstance() {
        return ourInstance;
    }
    public static void bind2Service(Context context, String serviceAction) {
        if (ourInstance == null) {
            ourInstance = new BinderManager_Client(context,serviceAction);
        }
    }

    private BinderManager_Client(Context context, String serviceAction) {
        Intent intent = new Intent();
        intent.setAction(serviceAction);
        intent.setPackage("com.hoody.frame");
        //绑定的时候服务端自动创建
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binderManager = com.hoody.commonbase.BinderManager.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    public <T extends android.os.IInterface> T getBinderInstance(Class<T> interfaceClazz) {
        try {
            if (binderManager != null) {
                Class<?> stubClass = Class.forName(interfaceClazz.getCanonicalName() + "$Stub");
                Method asInterface = stubClass.getMethod("asInterface", IBinder.class);
                asInterface.setAccessible(true);
                T result = (T) asInterface.invoke(stubClass, binderManager.queryBinder(interfaceClazz.getCanonicalName()));
                return result;
            }
        } catch (RemoteException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
