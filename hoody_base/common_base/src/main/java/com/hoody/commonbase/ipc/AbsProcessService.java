package com.hoody.commonbase.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.hoody.annotation.ipc.IPCHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cdm on 2021/10/28.
 */
abstract class AbsProcessService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new MainProcessService.BinderManagerImpl();
    }

    static class BinderManagerImpl extends com.hoody.commonbase.BinderManager.Stub {
        private Map<String, IBinder> binderMap = new HashMap<>();

        @Override
        public IBinder queryBinder(String iInterfaceName) throws RemoteException {
            IBinder result = binderMap.get(iInterfaceName);
            if (result != null) {
                return result;
            }
            Class interfaceImplement = IPCHelper.getInstance().getInterfaceImplement(iInterfaceName);
            try {
                IBinder iBinder = (IBinder) interfaceImplement.newInstance();
                binderMap.put(iInterfaceName, iBinder);
                return iBinder;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
