package com.hoody.wificontrol.view;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hoody.annotation.model.ModelManager;
import com.hoody.annotation.permission.Permissions;
import com.hoody.annotation.router.Router;
import com.hoody.commonbase.customview.slidedecidable.SlideDecidableLayout;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.commonbase.util.ToastUtil;
import com.hoody.commonbase.view.fragment.SwipeBackFragment;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.model.wificontrol.IWifiObserver;
import com.hoody.wificontrol.R;
import com.hoody.wificontrol.model.WifiDeviceModel;

@Router("wificontrol/main")
@Permissions({Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION})
public class MainFragment extends SwipeBackFragment implements IWifiObserver {
    private static final String TAG = "MainFragment";
    private DevicePassSetPopupWindow mDevicePassSetPopupWindow;
    private DevicePassInputPopupWindow mDevicePassInputPopupWindow;
    private WifiListPopupWindow mWifiListPopupWindow;
    private DeviceWifiPassInputPopupWindow mDeviceWifiPassInputPopupWindow;

    @Override
    protected void onClose() {

    }

    @Override
    protected View createContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_main, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        mObserverRegister.regist(this);
        //??????????????????
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    private void initView() {
        FragmentFactory fragmentFactory = new FragmentFactory();
        ViewPager controller_pager = (ViewPager) findViewById(R.id.controller_pager);
        FragmentStatePagerAdapter fragmentStateAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public int getCount() {
                return 2;
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                Fragment instantiate = fragmentFactory.instantiate(getClass().getClassLoader(), "com.hoody.wificontrol.view.TVControllerFragment");
                Bundle args = new Bundle();
                args.putInt("position", position);
                instantiate.setArguments(args);
                return instantiate;
            }
        };
        controller_pager.setAdapter(fragmentStateAdapter);
        getSwipeBackLayout().addDecider(SlideDecidableLayout.DeciderProduceUtil.getViewSlidableDecider(controller_pager));
        findViewById(R.id.bt_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPopupWindow settingPopupWindow = new SettingPopupWindow(getContext());
                settingPopupWindow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingPopupWindow.dismiss();
                        switch (((String) v.getTag())) {
                            case SettingPopupWindow.ITEM_SET_WIFI_PASS:
                                mDeviceWifiPassInputPopupWindow = new DeviceWifiPassInputPopupWindow(getContext());
                                mDeviceWifiPassInputPopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
                                break;
                            case SettingPopupWindow.ITEM_SET_WIFI:
                                showWifiSet();
                                break;
                            case SettingPopupWindow.ITEM_RESET_PASS:
                                DevicePassResetPopupWindow devicePassResetPopupWindow = new DevicePassResetPopupWindow(getContext());
                                devicePassResetPopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
                                break;
                            case SettingPopupWindow.ITEM_STUDY:
                                ModelManager.getModel(IWifiDeviceModel.class).studyKey("123");
                                break;
                        }
                    }
                });
                settingPopupWindow.showAsDropDown(v);
            }
        });
    }

    private void showWifiSet() {
        mWifiListPopupWindow = new WifiListPopupWindow(getContext());
        mWifiListPopupWindow.setWidth(DeviceInfo.ScreenWidth() / 2);
        mWifiListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWifiListPopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
        mWifiListPopupWindow.setOnItemClickListener(new WifiListPopupWindow.OnItemClickListener() {
            @Override
            public void onItemClick(ScanResult scanResult) {
                mWifiListPopupWindow.dismiss();
                WifiPassInputPopupWindow wifiPassInputPopupWindow = new WifiPassInputPopupWindow(getContext());
                wifiPassInputPopupWindow.setWifiName(scanResult.SSID);
                wifiPassInputPopupWindow.setWidth(DeviceInfo.ScreenWidth() / 2);
                wifiPassInputPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                wifiPassInputPopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
            }
        });
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            //??????wifi
            wifiManager.setWifiEnabled(true);
        }
        boolean b = wifiManager.startScan();
        Logger.i(TAG, "startScan: " + b);
    }

    @Override
    public void onNoFoundDevices() {
        ToastUtil.showToast(getContext(), "??????????????????");
    }

    @Override
    public void onManagerPassSetSuccess() {
        ToastUtil.showToast(getContext(), "????????????");
        if (mDevicePassSetPopupWindow != null) {
            mDevicePassSetPopupWindow.dismiss();
        }
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    @Override
    public void onDeviceManagerPassNull() {
        mDevicePassSetPopupWindow = new DevicePassSetPopupWindow(getContext());
        mDevicePassSetPopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
    }

    @Override
    public void onDeviceManagerPassErr() {
        mDevicePassInputPopupWindow = new DevicePassInputPopupWindow(getContext());
        mDevicePassInputPopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
    }

    @Override
    public void onWifiNull() {
        showWifiSet();
    }

    @Override
    public void onWifiErr() {
        ToastUtil.showToast(getContext(), "wifi??????????????????????????????");
        showWifiSet();
    }

    @Override
    public void onManagerLoginSuccess() {
        if (mDevicePassInputPopupWindow != null) {
            mDevicePassInputPopupWindow.dismiss();
        }
        ToastUtil.showToast(getContext(), "????????????");
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    @Override
    public void onManagerLoginFail() {
        ToastUtil.showToast(getContext(), "??????????????????");
    }

    @Override
    public void onSetWifiSuccess() {
        ToastUtil.showToast(getContext(), "wifi????????????");
    }

    @Override
    public void onManagerPassResetFail() {
        ToastUtil.showToast(getContext(), "??????????????????");
    }

    @Override
    public void onStudySuccess(String keyId, String data) {
        ToastUtil.showToast(getContext(), "???????????? :" + data);
    }

    @Override
    public void onStudyFail(int code, String keyId) {
        if (code == WifiDeviceModel.Code.Code_study_key_err) {
            //??????????????????
            ToastUtil.showToast(getContext(), "???????????? :??????????????????");
        } else if (code == WifiDeviceModel.Code.Code_being_study_other) {
            //????????????????????????
            ToastUtil.showToast(getContext(), "???????????? :????????????????????????");
        } else if (code == WifiDeviceModel.Code.Code_study_outtime) {
            //??????????????????
            ToastUtil.showToast(getContext(), "???????????? :??????????????????");
        }
    }

    @Override
    public void onManagerPassSetFail(int code) {
        ToastUtil.showToast(getContext(), "???????????????????????????8-20???");
    }

    @Override
    public void onModifyWifiPassSuccess() {
        ToastUtil.showToast(getContext(), "?????????????????????????????????");
    }

    @Override
    public void onModifyWifiPassFail() {
        ToastUtil.showToast(getContext(), "???????????????????????????8-20???");
    }
}
