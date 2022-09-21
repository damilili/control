package com.hoody.commonbase.ipc;

import android.content.Intent;
import android.os.IBinder;

public class SubProcessService extends AbsProcessService {

    @Override
    public IBinder onBind(Intent intent) {
        //这里是为同主进程通信做准备
        BinderManager_Client.bind2Service(this,"com.hoody.base.MainProcessService");
        return super.onBind(intent);
    }
}
