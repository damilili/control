package com.hoody.model.wificontrol;

import com.hoody.commonbase.message.IMessageObserver;

public interface IWifiObserver extends IMessageObserver {

    default void onDeviceManagerPassNull() {
    }

    default void onDeviceManagerPassErr() {
    }

    default void onWifiNull() {
    }

    default void onWifiErr() {
    }

    default void onDeviceManagerPassANDWifiOk() {
    }

    default void onNoFoundDevices() {
    }

    default void onPassSetSuccess() {
    }

    default void onLoginSuccess() {
    }

    default void onLoginFail() {
    }

    default void onSetWifiSuccess() {
    }

    void onPassResetFail();

    void onStudySuccess(String keyId, int preCode, int userCode, int dataCode);

    void onStudyFail(String keyId);

}
