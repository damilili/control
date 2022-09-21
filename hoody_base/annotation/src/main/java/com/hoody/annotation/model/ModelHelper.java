package com.hoody.annotation.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by cdm on 2021/10/21.
 */
public class ModelHelper<T extends IModel> {
    private ModelHelper() {
    }

    private static ModelHelper instance = new ModelHelper();
    private List<Class<? extends T>> modelImpl = new ArrayList<>();

    public static ModelHelper getInstance() {
        return instance;
    }

    public void putModelImpl(Class<? extends T> implClazz) {
        if (!modelImpl.contains(implClazz)) {
            modelImpl.add(implClazz);
        }
    }

    Class<? extends T> getModel(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            return null;
        }
        for (Class<? extends T> aClass : modelImpl) {
            if (interfaceClass.isAssignableFrom(aClass)) {
                return aClass;
            }
        }
        return null;
    }
}
