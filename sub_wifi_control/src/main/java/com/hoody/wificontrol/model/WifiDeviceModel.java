package com.hoody.wificontrol.model;

import android.text.TextUtils;

import com.hoody.annotation.model.ModelImpl;
import com.hoody.commonbase.BaseApplication;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.message.Messenger;
import com.hoody.commonbase.net.ReqeuestParam;
import com.hoody.commonbase.net.client.HttpClientWrapper;
import com.hoody.commonbase.util.SharedPreferenceUtil;
import com.hoody.commonbase.util.SynchronizeUtil;
import com.hoody.commonbase.util.ToastUtil;
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
    private static final int MAX_CHECK_STUDY_TIME = 10;

    public interface Code {
        int Code_ok = 0;
        int Code_no_regist = 1;    //未注册
        int Code_token_err = 2;     //令牌错误
        int Code_wifi_no_set = 3;    //未设置wifi
        int Code_wifi_err = 4;   //WiFi连接失败
        int Code_pass_err = 5;  //密码错误
        int Code_muti_regist = 6; //重复注册
        int Code_pass_format_err = 7;//秘密格式错误
    }

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
    public void setManagerPass(String pass) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            setManagerPass(serverIp, pass);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            setManagerPass(apIp, pass);
        }
    }

    public void loginManager(String pass) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            loginManager(serverIp, pass);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            loginManager(apIp, pass);
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

    @Override
    public void modifyManagerPass(String oldPass, String newPass) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            modifyPass(serverIp, oldPass, newPass);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            modifyPass(apIp, oldPass, newPass);
        }
    }

    @Override
    public void modifyWifiPass(String newPass) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            modifyWifiPass(serverIp, newPass);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            modifyWifiPass(apIp, newPass);
        }
    }

    private void modifyWifiPass(String serverIp, String newPass) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/modifywifipass", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        reqeuestParam.put("pass", newPass);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onModifyWifiPassSuccess();
                } else {
                    Messenger.sendTo(IWifiObserver.class).onModifyWifiPassFail();
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        modifyWifiPass(apIp, newPass);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private String studyKeyId;

    @Override
    public void studyKey(String keyId) {
        String serverIp = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_IP, "");
        if (!TextUtils.isEmpty(serverIp)) {
            studyKey(serverIp, keyId);
        } else {
            String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
            studyKey(apIp, keyId);
        }
    }

    @Override
    public void endStudyKey() {
        studyKeyId = "0";
    }

    private void studyKey(String serverIp, String keyId) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/study", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        reqeuestParam.put("keyid", keyId);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    checkStudyCount = 0;
                    studyKeyId = keyId;
                    getStudyResultDelay(serverIp);
                } else {
                    Messenger.sendTo(IWifiObserver.class).onStudyFail(keyId);
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        studyKey(apIp, keyId);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private void getStudyResult(String serverIp, String keyId) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/getStudyResult", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        reqeuestParam.put("keyid", keyId);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    endStudyKey();
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    endStudyKey();
                    int preCode = result.optInt("preCode");
                    int userCode = result.optInt("userCode");
                    int dataCode = result.optInt("dataCode");
                    Messenger.sendTo(IWifiObserver.class).onStudySuccess(keyId, preCode, userCode, dataCode);
                } else if (code == 7) {
                    getStudyResultDelay(serverIp);
                } else {
                    endStudyKey();
                    Messenger.sendTo(IWifiObserver.class).onStudyFail(keyId);
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        getStudyResult(apIp, keyId);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    int checkStudyCount = 0;

    private void getStudyResultDelay(String serverIp) {
        if (checkStudyCount > MAX_CHECK_STUDY_TIME) {
            endStudyKey();
            return;
        }
        SynchronizeUtil.runMainThreadDelay(new Runnable() {
            @Override
            public void run() {
                checkStudyCount++;
                getStudyResult(serverIp, studyKeyId);
            }
        }, 1000);
    }

    private void modifyPass(String serverIp, String oldPass, String newPass) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/modifymanagerpass", null);
        String token = SharedPreferenceUtil.getInstance().readSharedPreferences(KEY_DEVICE_SERVER_TOKEN, "");
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("token", token);
        reqeuestParam.put("oldPass", oldPass);
        reqeuestParam.put("newPass", newPass);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onManagerPassSetSuccess();
                } else {
                    Messenger.sendTo(IWifiObserver.class).onManagerPassResetFail();
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        modifyPass(apIp, oldPass, newPass);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
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
                String serverIp = result.optString("wifiIp");
                if (!TextUtils.isEmpty(serverIp)) {
                    SharedPreferenceUtil.getInstance().saveSharedPreferences(KEY_DEVICE_SERVER_IP, serverIp);
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onSetWifiSuccess();
                } else {
                    Messenger.sendTo(IWifiObserver.class).onWifiErr();
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
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private void loginManager(String serverIp, String pass) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/login", null);
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("pass", pass);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == 0) {
                    Messenger.sendTo(IWifiObserver.class).onManagerLoginSuccess();
                } else {
                    Messenger.sendTo(IWifiObserver.class).onManagerLoginFail();
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        loginManager(apIp, pass);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private void setManagerPass(String serverIp, String pass) {
        String http_url = UrlUtil.getHttp_Url(serverIp, "device/regist", null);
        ReqeuestParam reqeuestParam = new ReqeuestParam();
        reqeuestParam.put("pass", pass);
        HttpClientWrapper.getClient().post(http_url, null, reqeuestParam, new ResponseBase() {
            @Override
            public void onRequestSuccess(JSONObject result) {
                if (commonKeyParse(result)) {
                    return;
                }
                int code = result.optInt("code");
                if (code == Code.Code_ok) {
                    Messenger.sendTo(IWifiObserver.class).onManagerPassSetSuccess();
                } else {
                    //设置失败，秘密格式错误
                    Messenger.sendTo(IWifiObserver.class).onManagerPassSetFail(code);
                }
            }

            @Override
            public void onRequestFail(int errCode, String errDes) {
                String apIp = WifiUtil.getApIp(BaseApplication.getInstance());
                if (errCode == -1) {
                    Logger.i(TAG, "使用ap检查!");
                    if (!TextUtils.equals(serverIp, apIp)) {
                        setManagerPass(apIp, pass);
                    } else {
                        Logger.i(TAG, "请连接设备wifi!");
                        Messenger.sendTo(IWifiObserver.class).onNoFoundDevices();
                    }
                } else if (errCode == -2) {
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
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
                    SynchronizeUtil.runMainThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(BaseApplication.getInstance(), "数据解析异常");
                        }
                    });
                    Logger.i(TAG, "解析异常: ");
                }
            }
        });
    }

    private boolean commonKeyParse(JSONObject result) {
        String token = result.optString("token");
        if (!TextUtils.isEmpty(token)) {
            SharedPreferenceUtil.getInstance().saveSharedPreferences(KEY_DEVICE_SERVER_TOKEN, token);
        }
        int code = result.optInt("code");
        switch (code) {
            case Code.Code_no_regist:
                //服务端没有设备管理密码（没有token） --->请设置密码
                Messenger.sendTo(IWifiObserver.class).onDeviceManagerPassNull();
                Logger.i(TAG, "请输入设备管理密码: ");
                return true;
            case Code.Code_token_err:
                //存在密码但是token不正确--->请输入密码
                Messenger.sendTo(IWifiObserver.class).onDeviceManagerPassErr();
                Logger.i(TAG, "请输入正确的设备管理密码: ");
                return true;
            case Code.Code_wifi_no_set:
                //设备没有WiFi连接信息  --->请设置wifi
                Messenger.sendTo(IWifiObserver.class).onWifiNull();
                Logger.i(TAG, "请选择需要链接的wifi: ");
                return true;
        }
        return false;
    }
}
