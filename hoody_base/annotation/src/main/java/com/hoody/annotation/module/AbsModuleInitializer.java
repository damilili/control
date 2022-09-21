package com.hoody.annotation.module;

import android.content.Context;
import android.util.Log;

import com.hoody.annotation.ipc.IIPCCollector;
import com.hoody.annotation.ipc.IPCConfig;
import com.hoody.annotation.listshower.CollectorConfig;
import com.hoody.annotation.listshower.IListShowerCollector;
import com.hoody.annotation.model.IModelCollector;
import com.hoody.annotation.model.ModelConfig;
import com.hoody.annotation.router.IRouterCollector;
import com.hoody.annotation.router.RouterConfig;

import java.lang.ref.WeakReference;

/**
 * Created by cdm on 2021/11/16.
 */
public abstract class AbsModuleInitializer {
    public static WeakReference<Context> mContextReference;

    final void init(Context context) {
        mContextReference = new WeakReference<>(context.getApplicationContext());
        String moduleId = String.format("0x%02X", getModuleId());
        try {
            //收集ipc
            String ipcClassFullName = IPCConfig.packagerName + "." + IPCConfig.classBaseName + moduleId;
            Class<IIPCCollector> iipcCollectorClass = (Class<IIPCCollector>) Class.forName(ipcClassFullName);
            IIPCCollector iipcCollector = iipcCollectorClass.newInstance();
            iipcCollector.putInterfaceImplements();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        try {
            //收集路由表
            String routerClassFullName = RouterConfig.packagerName + "." + RouterConfig.classBaseName + moduleId;
            Class<IRouterCollector> iRouterCollectorClass = (Class<IRouterCollector>) Class.forName(routerClassFullName);
            IRouterCollector iRouterCollector = iRouterCollectorClass.newInstance();
            iRouterCollector.putPath();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        try {
            //收集模型模块
            String modelClassFullName = ModelConfig.packagerName + "." + ModelConfig.classBaseName + moduleId;
            Class<IModelCollector> iModelCollectorClass = (Class<IModelCollector>) Class.forName(modelClassFullName);
            IModelCollector iModelCollector = iModelCollectorClass.newInstance();
            iModelCollector.collect();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            //收集Listshower
            String routerClassFullName = CollectorConfig.packagerName + "." + CollectorConfig.classBaseName + moduleId;
            Class<IListShowerCollector> iListShowerCollectorClass = (Class<IListShowerCollector>) Class.forName(routerClassFullName);
            IListShowerCollector listShowerCollector = iListShowerCollectorClass.newInstance();
            listShowerCollector.collect();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        Log.d("AbsModuleInitializer", "init() called with: ModuleInitializer= [" + this + "]");
        onInit(context);
    }

    protected abstract void onInit(Context context);

    protected abstract int getModuleId();
}
