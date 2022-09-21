package com.hoody.commonbase;

import android.app.Application;

import com.hoody.annotation.module.InitializerUtil;

/**
 * Created by cdm on 2021/10/21.
 */
public class BaseApplication extends Application {
    private static BaseApplication instance;

    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        //各个模块初始化
        InitializerUtil.initModules(this);
    }
}
