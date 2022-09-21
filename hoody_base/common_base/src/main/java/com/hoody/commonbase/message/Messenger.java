package com.hoody.commonbase.message;

import android.os.Handler;
import android.os.Looper;

import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.util.SynchronizeUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息信使
 */
public final class Messenger {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final static Messenger INSTANCE = new Messenger();
    private static Map<String, IMessageObserver> mSendCache = new HashMap<>();

    private Messenger() {
    }

    private static Messenger getInstance() {
        return INSTANCE;
    }

    public static <T extends IMessageObserver> T sendTo(final Class<T> sendTo) {
        return getInstance().sendToInner(sendTo);
    }

    private <T extends IMessageObserver> T sendToInner(final Class<T> sendTo) {
        if (!sendTo.isInterface()) {
            throw new IllegalArgumentException("sendTo 必须是Interface,错误入参： " + sendTo.getCanonicalName());
        }
        if (sendTo == IMessageObserver.class) {
            throw new IllegalArgumentException("不可以直接将 IMessageObserver 作为参数 ");
        }
        IMessageObserver iMessageObserver = mSendCache.get(sendTo.getCanonicalName());
        if (iMessageObserver == null) {
            iMessageObserver = (IMessageObserver) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{sendTo}, new InvocationHandler() {
                final String TAG = "Messenger-LOG";
                @Override
                public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                    SynchronizeUtil.runSubThread(new Runnable() {
                        @Override
                        public void run() {
                            final List<T> observers = MessageObserverManager.getInstance().getObservers(sendTo);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        StringBuilder logInfoBuilder = null;
                                        if (Logger.enable()) {
                                            //日志信息拼接
                                            logInfoBuilder = new StringBuilder();
                                            logInfoBuilder.append(method.getName()).append("(");
                                            Class<?>[] parameterTypes = method.getParameterTypes();
                                            if (parameterTypes.length > 0) {
                                                for (int i = 0; i < parameterTypes.length; i++) {
                                                    logInfoBuilder.append(parameterTypes[i].getSimpleName()).append(parameterTypes.length - 1 == i ? "" : ",");
                                                }
                                            }
                                            logInfoBuilder.append(") of ");
                                            logInfoBuilder.append(method.getDeclaringClass());
                                            String params = null;
                                            if (args != null) {
                                                StringBuilder paramsBuilder = new StringBuilder();
                                                for (int i = 0; i < args.length; i++) {
                                                    paramsBuilder.append(parameterTypes[i].getSimpleName()).append("=").append(args[i]).append(parameterTypes.length - 1 == i ? "" : ",\n\t\t\t\t\t\t\t");
                                                    params = paramsBuilder.toString();
                                                }

                                            }
                                            logInfoBuilder.append("\nsend message with params :[\t").append(params).append("\t] \nto observers:[\t");
                                        }
                                        if (observers != null && observers.size() > 0) {
                                            for (int i = 0; i < observers.size(); i++) {
                                                if (logInfoBuilder != null) {
                                                    logInfoBuilder.append(observers.get(i).getClass().getCanonicalName());
                                                    if (observers.size() - 1 != i) {
                                                        logInfoBuilder.append(",\n\t\t\t\t");
                                                    } else {
                                                        logInfoBuilder.append("\t]");
                                                    }
                                                }
                                                method.invoke(observers.get(i), args);
                                            }
                                        } else {
                                            logInfoBuilder.append("]");
                                        }
                                        if (logInfoBuilder != null) {
                                            Logger.i(TAG, logInfoBuilder.toString());
                                        }
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }

                    if (method.getReturnType().equals(float.class)) {
                        return 0;
                    }
                    if (method.getReturnType().equals(double.class)) {
                        return 0;
                    }
                    if (method.getReturnType().equals(int.class)) {
                        return 0;
                    }
                    if (method.getReturnType().equals(long.class)) {
                        return 0;
                    }
                    if (method.getReturnType().equals(char.class)) {
                        return 0;
                    }
                    if (method.getReturnType().equals(byte.class)) {
                        return 0;
                    }
                    if (method.getReturnType().equals(short.class)) {
                        return 0;
                    }
                    return null;
                }
            });
            mSendCache.put(sendTo.getCanonicalName(), iMessageObserver);
        }
        return (T) iMessageObserver;
    }
}
