package com.hoody.wificontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.hoody.annotation.model.ModelManager;
import com.hoody.annotation.router.Router;
import com.hoody.annotation.router.RouterHelper;
import com.hoody.annotation.router.RouterUtil;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.commonbase.util.ToastUtil;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.model.wificontrol.IWifiObserver;
import com.hoody.wificontrol.view.DevicePassInputPopupWindow;
import com.hoody.wificontrol.view.DevicePassResetPopupWindow;
import com.hoody.wificontrol.view.DevicePassSetPopupWindow;
import com.hoody.wificontrol.view.DeviceWifiPassInputPopupWindow;
import com.hoody.wificontrol.view.SettingPopupWindow;
import com.hoody.wificontrol.view.WifiListPopupWindow;
import com.hoody.wificontrol.view.WifiPassInputPopupWindow;

@Router("config/test")
public class MainActivity extends Activity implements IWifiObserver {
    private static final String TAG = "MainActivity";
    private DevicePassSetPopupWindow mDevicePassSetPopupWindow;
    private DevicePassInputPopupWindow mDevicePassInputPopupWindow;
    private WifiListPopupWindow mWifiListPopupWindow;
    private DeviceWifiPassInputPopupWindow mDeviceWifiPassInputPopupWindow;
    private View rl_base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow(); //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //设置状态栏颜色
        window.setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rl_base = findViewById(R.id.rl_base);
        if (BuildConfig.IS_APPLICATION) {

        }
        findViewById(R.id.buttonPanel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RouterUtil.getInstance().navigateTo(MainActivity.this,"wificontrol/wifi",null);
            }
        });
        findViewById(R.id.buttonPanel).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Bundle data = new Bundle();
                data.putInt("forset",1);
                RouterUtil.getInstance().navigateTo(MainActivity.this,"wificontrol/wifi", data);
                return true;
            }
        });
        findViewById(R.id.bt_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPopupWindow settingPopupWindow = new SettingPopupWindow(MainActivity.this);
                settingPopupWindow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingPopupWindow.dismiss();
                        switch (((String) v.getTag())) {
                            case SettingPopupWindow.ITEM_SET_WIFI_PASS:
                                mDeviceWifiPassInputPopupWindow = new DeviceWifiPassInputPopupWindow(MainActivity.this);
                                mDeviceWifiPassInputPopupWindow.showAtLocation(rl_base, Gravity.CENTER, 0, 0);
                                break;
                            case SettingPopupWindow.ITEM_SET_WIFI:
                                showWifiSet();
                                break;
                            case SettingPopupWindow.ITEM_RESET_PASS:
                                DevicePassResetPopupWindow devicePassResetPopupWindow = new DevicePassResetPopupWindow(MainActivity.this);
                                devicePassResetPopupWindow.showAtLocation(rl_base, Gravity.CENTER, 0, 0);
                                break;
                            case SettingPopupWindow.ITEM_STUDY:
//
//                                ModelManager.getModel(IWifiDeviceModel.class).studyKey("123");
                                break;
                            case SettingPopupWindow.ITEM_ADD:
                                RouterUtil.getInstance().navigateTo(MainActivity.this, "wifi/keyset", null);
                                break;
                        }
                    }
                });
                settingPopupWindow.showAsDropDown(v);
            }
        });
    }

    private void showWifiSet() {
        mWifiListPopupWindow = new WifiListPopupWindow(MainActivity.this);
        mWifiListPopupWindow.setWidth(DeviceInfo.ScreenWidth() / 2);
        mWifiListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWifiListPopupWindow.showAtLocation(rl_base, Gravity.CENTER, 0, 0);
        mWifiListPopupWindow.setOnItemClickListener(new WifiListPopupWindow.OnItemClickListener() {
            @Override
            public void onItemClick(ScanResult scanResult) {
                mWifiListPopupWindow.dismiss();
                WifiPassInputPopupWindow wifiPassInputPopupWindow = new WifiPassInputPopupWindow(MainActivity.this);
                wifiPassInputPopupWindow.setWifiName(scanResult.SSID);
                wifiPassInputPopupWindow.setWidth(DeviceInfo.ScreenWidth() / 2);
                wifiPassInputPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                wifiPassInputPopupWindow.showAtLocation(rl_base, Gravity.CENTER, 0, 0);
            }
        });
        WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            //开启wifi
            wifiManager.setWifiEnabled(true);
        }
        boolean b = wifiManager.startScan();
        Logger.i(TAG, "startScan: " + b);
    }

    @Override
    public void onManagerPassSetSuccess() {
        ToastUtil.showToast(MainActivity.this, "设置成功");
        if (mDevicePassSetPopupWindow != null) {
            mDevicePassSetPopupWindow.dismiss();
        }
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    @Override
    public void onDeviceManagerPassNull() {
        mDevicePassSetPopupWindow = new DevicePassSetPopupWindow(MainActivity.this);
        mDevicePassSetPopupWindow.showAtLocation(rl_base, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onDeviceManagerPassErr() {
        mDevicePassInputPopupWindow = new DevicePassInputPopupWindow(MainActivity.this);
        mDevicePassInputPopupWindow.showAtLocation(rl_base, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onWifiNull() {
        showWifiSet();
    }

    @Override
    public void onWifiErr() {
        ToastUtil.showToast(MainActivity.this, "wifi连接失败，请重新选择");
        showWifiSet();
    }

    @Override
    public void onManagerLoginSuccess() {
        if (mDevicePassInputPopupWindow != null) {
            mDevicePassInputPopupWindow.dismiss();
        }
        ToastUtil.showToast(MainActivity.this, "认证成功");
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    @Override
    public void onManagerLoginFail() {
        ToastUtil.showToast(MainActivity.this, "登录认证失败");
    }

    @Override
    public void onSetWifiSuccess() {
        ToastUtil.showToast(MainActivity.this, "wifi设置成功");
    }

    @Override
    public void onManagerPassResetFail() {
        ToastUtil.showToast(MainActivity.this, "密码修改失败");
    }

}