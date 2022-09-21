package com.hoody.annotation.module;

import android.content.Context;
import android.util.Log;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Initializer 的配置
 */
public final class InitializerUtil {
    public static final String packagerName = "com.hoody.annotation.initializer";
    public static final String classBaseName = "InitializerUtil";
    public static final String FieldName = "InitialerClass";
    public static final String FieldNameSplit = "_";
    private static boolean inited = false;
    private static final String TAG = "InitializerUtil";

    public static void initModules(Context context) {
        if (inited) {
            return;
        }
        inited = true;
        initModules(context, context.getPackageCodePath());
    }

    public static void initModules(Context context, String packagePath) {
        try {
            List<String> classNames = null;
            ArrayList<String> initializerUtil = new ModuleInitializer().getInitializerUtil();
            if (initializerUtil != null) {
                classNames = initializerUtil;
            } else {
                classNames = new ArrayList<>();
                DexFile dexFile = new DexFile(packagePath);
                Enumeration<String> entries = dexFile.entries();
                while (entries.hasMoreElements()) {
                    String s = entries.nextElement();
                    if (s.contains(packagerName)) {
                        classNames.add(s);
                    }
                }
            }
            Comparator<String> comparator = new Comparator<String>() {
                @Override
                public int compare(String str1, String str2) {
                    int priorty1 = Integer.parseInt(str1.split(FieldNameSplit)[1]);
                    int priorty2 = Integer.parseInt(str2.split(FieldNameSplit)[1]);
                    return priorty1 - priorty2;
                }
            };
            Collections.sort(classNames, comparator);
            try {
                for (String className : classNames) {
                    Log.d("InitializerUtil", "initModule() called with: context = [" + className + "]");
                    Class<?> aClass = Class.forName(className);
                    Field field = aClass.getField(FieldName);
                    Class initialerClass = (Class) field.get(aClass);
                    if (AbsModuleInitializer.class.isAssignableFrom(initialerClass)) {
                        AbsModuleInitializer iModuleInitializer = (AbsModuleInitializer) initialerClass.newInstance();
                        iModuleInitializer.init(context);
                    }
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
                Log.d(TAG, "initModules() called with: Exception, Cause message :" + e.getCause().getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
