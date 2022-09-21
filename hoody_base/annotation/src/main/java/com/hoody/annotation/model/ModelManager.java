package com.hoody.annotation.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;


public class ModelManager {
    private static ModelManager INSTANCE;
    private static Logger mLogger;
    private ModelManager(){}

    private Map<Class<? extends IModel>, IModel> mModelCache = new HashMap<>();

    public static <T extends IModel> T getModel(Class<T> modelInterfaceClass) {
        if (INSTANCE == null) {
            synchronized (ModelManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ModelManager();
                }
            }
        }
        return INSTANCE.getModelInner(modelInterfaceClass);
    }

    public static void setLogger(Logger logger) {
        mLogger = logger;
    }

    private <T extends IModel> T getModelInner(Class<T> modelInterfaceClass) {
        if (modelInterfaceClass == null) {
            return null;
        }
        if (!modelInterfaceClass.isInterface()) {
            throw new IllegalArgumentException("modelInterfaceClass 必须是继承IModel的接口");
        }
        T modelImpl = (T) mModelCache.get(modelInterfaceClass);
        if (modelImpl != null) {
            return modelImpl;
        }
        final Class<? extends T> model = ModelHelper.getInstance().getModel((Class<T>) modelInterfaceClass);
        if (model == null) {
            return null;
        }

        modelImpl = (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{modelInterfaceClass}, new InvocationHandler() {
            T instance = null;
            final String TAG = "IModel-LOG";

            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                StringBuilder methodInfoBuilder = new StringBuilder();
                methodInfoBuilder.append(method.getName()).append("(");
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length > 0) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        methodInfoBuilder.append(parameterTypes[i].getSimpleName()).append(parameterTypes.length - 1 == i ? "" : ",");
                    }
                }
                methodInfoBuilder.append(") of ");
                methodInfoBuilder.append(method.getDeclaringClass());
                String methodInfo = methodInfoBuilder.toString();
                String params = "";
                if (objects != null) {
                    StringBuffer paramsBuffer = new StringBuffer();
                    for (int i = 0; i < objects.length; i++) {
                        paramsBuffer.append(parameterTypes[i].getSimpleName()).append("=").append(objects[i]).append(parameterTypes.length - 1 == i ? "" : ",");
                    }
                    params = paramsBuffer.toString();
                }
                try {
                    if (instance == null) {
                        instance = model.newInstance();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
                Object result = method.invoke(instance, objects);
                if (mLogger != null) {
                    mLogger.log(methodInfo + "\ncalled with params : [" + params + "]" + "\nresult : " + result);
                }
                return result;
            }
        });
        mModelCache.put(modelInterfaceClass, modelImpl);

        return modelImpl;
    }

    public interface Logger {
        void log(String info);
    }
}