package com.hoody.wificontrol.view;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hoody.annotation.model.ModelManager;
import com.hoody.annotation.permission.Permissions;
import com.hoody.annotation.router.Router;
import com.hoody.annotation.router.RouterUtil;
import com.hoody.commonbase.customview.ChildMoveLayout;
import com.hoody.commonbase.customview.slidedecidable.SlideDecidableLayout;
import com.hoody.commonbase.customview.slidedecidable.SwipeBackLayout;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.commonbase.util.SharedPreferenceUtil;
import com.hoody.commonbase.util.ToastUtil;
import com.hoody.commonbase.view.fragment.SwipeBackFragment;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.model.wificontrol.IWifiObserver;
import com.hoody.wificontrol.R;
import com.hoody.model.wificontrol.SingleKey;
import com.hoody.wificontrol.model.WifiDeviceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@Router("wificontrol/wifi")
@Permissions({Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION})
public class WifiControlFragment extends SwipeBackFragment implements IWifiObserver {
    private static final String TAG = "MainFragment";
    public static final String Data_TAG = "MainFragment";
    private DevicePassSetPopupWindow mDevicePassSetPopupWindow;
    private DevicePassInputPopupWindow mDevicePassInputPopupWindow;
    private WifiListPopupWindow mWifiListPopupWindow;
    private DeviceWifiPassInputPopupWindow mDeviceWifiPassInputPopupWindow;
    private ChildMoveLayout mFreeMoveBase;
    private View.OnClickListener mOnKeyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mEditMode) {
                mEditMode = false;
                mFreeMoveBase.stopAnim();
                Object tag = v.getTag();
                if (tag instanceof SingleKey) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("keyBean", (SingleKey) tag);
                    RouterUtil.getInstance().navigateTo(getContext(), "wifi/keyset", bundle);
                }
            }
        }
    };
    private boolean mEditMode = false;

    @Override
    protected void onClose() {
        JSONObject jsonObject = new JSONObject();
        JSONArray datas = new JSONArray();
        try {
            for (SingleKey key : mKeys) {
                JSONObject jsonKey = new JSONObject();
                jsonKey.putOpt("id", key.getId());
                jsonKey.putOpt("name", key.getName());
                jsonKey.putOpt("pos_x", key.getPosX());
                jsonKey.putOpt("pos_y", key.getPosY());
                jsonKey.putOpt("width", key.getWidth());
                jsonKey.putOpt("height", key.getHeight());
                jsonKey.putOpt("data", key.getDataCode());
                datas.put(jsonKey);
            }
            jsonObject.put("keys", datas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferenceUtil.getInstance().saveSharedPreferences(Data_TAG, jsonObject.toString());
    }

    @Override
    protected View createContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_wifi_control, null);
    }

    private ArrayList<SingleKey> mKeys = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            String s = SharedPreferenceUtil.getInstance().readSharedPreferences(Data_TAG, "");
            if (TextUtils.isEmpty(s)) {
                s = "{\n" +
                        "    \"keys\": [\n" +
                        "        {\n" +
                        "            \"id\": \"1\",\n" +
                        "            \"name\": \"上\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": \"2\",\n" +
                        "            \"name\": \"下\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": \"3\",\n" +
                        "            \"name\": \"左\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": \"4\",\n" +
                        "            \"name\": \"右\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";
            }
            JSONObject jsonObject = new JSONObject(s);
            JSONArray keys = jsonObject.optJSONArray("keys");
            for (int i = 0; i < keys.length(); i++) {
                JSONObject jsonKey = keys.optJSONObject(i);
                String id = jsonKey.optString("id");
                String name = jsonKey.optString("name");
                int pos_x = jsonKey.optInt("pos_x");
                int pos_y = jsonKey.optInt("pos_y");
                int width = jsonKey.optInt("width");
                int height = jsonKey.optInt("height");
                String data = jsonKey.optString("data");
                SingleKey singleKey = new SingleKey(id, name, data);
                singleKey.setPosX(pos_x);
                singleKey.setPosY(pos_y);
                singleKey.setWidth(width);
                singleKey.setHeight(height);
                mKeys.add(singleKey);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initView();
        //填充数据
        for (SingleKey key : mKeys) {
            Button button = new Button(getContext());
            button.setSingleLine();
            bindKeyInfo(key, button);
            mFreeMoveBase.addView(button);
        }
        mObserverRegister.regist(this);
        //检查设备状态
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    private void bindKeyInfo(SingleKey key, Button button) {
        button.setTag(key);
        button.setOnClickListener(mOnKeyClickListener);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) button.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(ChildMoveLayout.SPLIT_LENGTH * key.getWidth(), ChildMoveLayout.SPLIT_LENGTH * key.getHeight());

        }
        layoutParams.width = ChildMoveLayout.SPLIT_LENGTH * key.getWidth();
        layoutParams.height = ChildMoveLayout.SPLIT_LENGTH * key.getHeight();
        button.setLayoutParams(layoutParams);

        button.setText(key.getName());
        button.setY(key.getPosY() * ChildMoveLayout.SPLIT_LENGTH);
        button.setX(key.getPosX() * ChildMoveLayout.SPLIT_LENGTH);
        button.setTextColor(key.getTextColor());
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, key.getTextSize());
        button.setBackgroundColor(key.getBackgroundColor());
        button.setPadding(0, 0, 0, 0);

    }

    private void initView() {
        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.addDecider(new SlideDecidableLayout.SlidableDecider(getView()) {
            @Override
            public boolean slidable(PointF curEvPos, SlideDecidableLayout.SlideOrientation slideOrientation) {
                for (int i = 0; i < mFreeMoveBase.getChildCount(); i++) {
                    View childAt = mFreeMoveBase.getChildAt(i);
                    if (isTouchPointInView(childAt, curEvPos.x, curEvPos.y)) {
                        return false;
                    }
                }
                return true;
            }
        });
        mFreeMoveBase = ((ChildMoveLayout) findViewById(R.id.free_move_base));
        mFreeMoveBase.setOnViewPositionChangeedListener(new ChildMoveLayout.OnViewPositionChangeedListener() {
            @Override
            public void onViewPositionChangeed(View positionChangedView) {
                float x = positionChangedView.getX();
                float y = positionChangedView.getY();
                Object tag = positionChangedView.getTag();
                if (tag instanceof SingleKey) {
                    ((SingleKey) tag).setPosX((int) x / ChildMoveLayout.SPLIT_LENGTH);
                    ((SingleKey) tag).setPosY((int) y / ChildMoveLayout.SPLIT_LENGTH);
                }
            }
        });
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
                                mEditMode = true;
                                mFreeMoveBase.startAnim();
                                ModelManager.getModel(IWifiDeviceModel.class).studyKey("123");
                                break;
                            case SettingPopupWindow.ITEM_ADD:
                                RouterUtil.getInstance().navigateTo(getContext(), "wifi/keyset", null);
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
            //开启wifi
            wifiManager.setWifiEnabled(true);
        }
        boolean b = wifiManager.startScan();
        Logger.i(TAG, "startScan: " + b);
    }

    @Override
    public void onNoFoundDevices() {
        ToastUtil.showToast(getContext(), "没有发现设备");
    }

    @Override
    public void onManagerPassSetSuccess() {
        ToastUtil.showToast(getContext(), "设置成功");
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
        ToastUtil.showToast(getContext(), "wifi连接失败，请重新选择");
        showWifiSet();
    }

    @Override
    public void onManagerLoginSuccess() {
        if (mDevicePassInputPopupWindow != null) {
            mDevicePassInputPopupWindow.dismiss();
        }
        ToastUtil.showToast(getContext(), "认证成功");
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    @Override
    public void onManagerLoginFail() {
        ToastUtil.showToast(getContext(), "登录认证失败");
    }

    @Override
    public void onSetWifiSuccess() {
        ToastUtil.showToast(getContext(), "wifi设置成功");
    }

    @Override
    public void onManagerPassResetFail() {
        ToastUtil.showToast(getContext(), "密码修改失败");
    }

    @Override
    public void onStudySuccess(String keyId, String data) {
        ToastUtil.showToast(getContext(), "学习成功 :" + data);
    }

    @Override
    public void onStudyFail(int code, String keyId) {
        if (code == WifiDeviceModel.Code.Code_study_key_err) {
            //按键输入错误
            ToastUtil.showToast(getContext(), "学习失败 :按键输入错误");
        } else if (code == WifiDeviceModel.Code.Code_being_study_other) {
            //正在学习其他按键
            ToastUtil.showToast(getContext(), "学习失败 :正在学习其他按键");
        } else if (code == WifiDeviceModel.Code.Code_study_outtime) {
            //按键学习超时
            ToastUtil.showToast(getContext(), "学习失败 :按键学习超时");
        }
    }

    @Override
    public void onManagerPassSetFail(int code) {
        ToastUtil.showToast(getContext(), "设置失败，密码长度8-20位");
    }

    @Override
    public void onModifyWifiPassSuccess() {
        ToastUtil.showToast(getContext(), "修改成功，等待设备重启");
    }

    @Override
    public void onModifyWifiPassFail() {
        ToastUtil.showToast(getContext(), "修改失败，密码长度8-20位");
    }

    @Override
    public void onKeyInfoChanged(SingleKey key) {
        for (int i = 0; i < mFreeMoveBase.getChildCount(); i++) {
            View child = mFreeMoveBase.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof SingleKey) {
                if (((SingleKey) tag).getId().equals(key.getId())) {
                    ((SingleKey) tag).copy(key);
                    if (child instanceof Button) {
                        bindKeyInfo(key, (Button) child);
                    }
                    return;
                }
            }
        }

        mKeys.add(key);
        Button button = new Button(getContext());
        button.setSingleLine();
        bindKeyInfo(key, button);
        mFreeMoveBase.addView(button);
    }
}
