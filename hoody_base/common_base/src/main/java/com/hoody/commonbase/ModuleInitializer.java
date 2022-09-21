package com.hoody.commonbase;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.hoody.annotation.model.ModelManager;
import com.hoody.annotation.module.AbsModuleInitializer;
import com.hoody.annotation.module.Initializer;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.plugin.PluginInstaller;
import com.hoody.commonbase.util.Util_Initializer;

/**
 * Created by cdm on 2021/11/16.
 */
@Initializer(priority = 0)
public class ModuleInitializer extends AbsModuleInitializer {

    @Override
    public void onInit(Context context) {
        Log.d("ModuleInitializer", "init() called with: context user= [" + context + "]");
        //日志初始化
        Logger.init(context);
        //模块管理初始化
        ModelManager.setLogger(new ModelManager.Logger() {
            @Override
            public void log(String info) {
                Logger.i("IModel-LOG", info);
            }
        });
        //工具组初始化
        Util_Initializer.init(context);
        //图片加载初始化
        com.hoody.commonbase.image.Initializer.init(context,false);
        //插件化初始化
        PluginInstaller.init((Application) context);
    }

    @Override
    public int getModuleId() {
        return BuildConfig.MODULE_ID;
    }
}
