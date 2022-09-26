package com.hoody.model.wificontrol;

import com.hoody.annotation.model.IModel;

public interface IWifiDeviceModel extends IModel {
    /**
     * 检查硬件状态
     */
    void checkDeviceStatus();

    void setManagerPass(String pass);

    void loginManager(String pass);

    void modifyManagerPass(String oldPass, String newPass);

    void modifyWifiPass(String newPass);

    void setWifiInfo(String wifiName, String pass);

    void studyKey(String keyId);

    void endStudyKey();
}
