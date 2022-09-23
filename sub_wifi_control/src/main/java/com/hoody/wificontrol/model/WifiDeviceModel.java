package com.hoody.wificontrol.model;

import android.text.TextUtils;

import com.hoody.annotation.model.ModelImpl;
import com.hoody.commonbase.BaseApplication;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.message.Messenger;
import com.hoody.commonbase.net.ReqeuestHeader;
import com.hoody.commonbase.net.ReqeuestParam;
import com.hoody.commonbase.net.client.HttpClientWrapper;
import com.hoody.commonbase.util.SharedPreferenceUtil;
import com.hoody.commonbase.util.UrlUtil;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.model.ResponseBase;
import com.hoody.model.wificontrol.IWifiObserver;
import com.hoody.wificontrol.WifiUtil;

import org.json.JSONObject;

/**
 * 客户端状态说明：
 * 状态0： 没有 token  没有serverIp
 */
@ModelImpl
public class WifiDeviceModel implements IWifiDeviceModel {
    private static final String TAG = "WifiDeviceModel";
    private static final String KEY_DEVICE_SERVER_IP = "KEY_DEVICE_SERVER_IP";
    private static final String KEY_DEVICE_SERVER_TOKEN = "KEY_DEVICE_SERVER_TOKEN";

    @Override
    public void checkDeviceStatus() {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            checkDeviceStatus(serverIp);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            checkDeviceStatus(apIp);
        }
    }

    @Override
    public void setAccessPass(String pass) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            setAccessPass(serverIp, pass);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            setAccessPass(apIp, pass);
        }
    }

    public void login(String pass) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            checkAccessPass(serverIp, pass);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            checkAccessPass(apIp, pass);
        }
    }

    @Override
    public void setWifiInfo(String wifiName, String pass) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            setWifi(serverIp, wifiName, pass);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            setWifi(apIp, wifiName, pass);
        }
    }

    private void setWifi(String serverIp, String wifiName, String pass) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/wifi", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        reqeuestParam.put("wifiName", wifiName);
        reqeuestParam.put("pass", pass);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onSetWifiSuccess();
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        setWifi(apIp, wifiName, apIp);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private void checkAccessPass(String serverIp, String pass) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/login", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        reqeuestParam.put("pass", pass);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onLoginSuccess();
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        setAccessPass(apIp, apIp);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private void setAccessPass(String serverIp, String pass) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/setpass", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        reqeuestParam.put("pass", pass);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onPassSetSuccess();
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        setAccessPass(apIp, apIp);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private void checkDeviceStatus(String serverIp) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/status", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        ReqeuestHeader reqeuestHeader = new ReqeuestHeader();
        reqeuestHeader.put("token", token);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onDeviceManagerPassANDWifiOk();
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        checkDeviceStatus(apIp);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private boolean commonKeyParse(JSONObject result) {
        String serverIp = result.optString("serverIp");
        String token = result.optString("token");
        if (!TextUtils.isEmpty(token)) {
            SharedPreferenceUtil.getInstance().saveSharedPreferences(KEY_DEVICE_SERVER_TOKEN, token);
        }
        if (!TextUtils.isEmpty(serverIp)) {
            SharedPreferenceUtil.getInstance().saveSharedPreferences(KEY_DEVICE_SERVER_IP, serverIp);
        }
        int code = result.optInt("code");
        switch (code) {
            case 1:
                //服务端没有设备管理密码（没有token） --->请设置密码
                Messenger.sendTo(IWifiObserver.class).onDeviceManagerPassNull();
                Logger.i(TAG, "请输入设备管理密码: ");
                return true;
            case 2:
                //存在密码但是token不正确--->请输入密码
                Messenger.sendTo(IWifiObserver.class).onDeviceManagerPassErr();
                Logger.i(TAG, "请输入正确的设备管理密码: ");
                return true;
            case 3:
                //设备没有WiFi连接信息  --->请设置wifi
                Messenger.sendTo(IWifiObserver.class).onWifiNull();
                Logger.i(TAG, "请选择需要链接的wifi: ");
                return true;
            case 4:
                //wifi链接信息不可用 --->请设置wifi
                Messenger.sendTo(IWifiObserver.class).onWifiErr();
                Logger.i(TAG, "请选择正确链接的wifi: ");
                return true;
        }
        return false;
    }
}
