package com.hoody.model.wificontrol;

import com.hoody.annotation.model.IModel;

public interface IWifiDeviceModel extends IModel {
    /**
     * 检查硬件状态
     */
    void checkDeviceStatus();

    void setAccessPass(String pass);

    void login(String pass);

    void setWifiInfo(String wifiName, String pass);
}
