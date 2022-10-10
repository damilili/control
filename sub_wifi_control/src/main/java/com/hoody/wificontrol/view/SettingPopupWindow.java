package com.hoody.wificontrol.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hoody.annotation.model.ModelManager;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.wificontrol.R;

public class SettingPopupWindow extends PopupWindow {
    private static final String TAG = "DevicePassInputPopupWindow";

    public static final String ITEM_SET_WIFI_PASS = "ITEM_SET_WIFI_PASS";
    public static final String ITEM_RESET_PASS = "ITEM_RESET_PASS";
    public static final String ITEM_SET_WIFI = "ITEM_SET_WIFI";
    public static final String ITEM_STUDY = "ITEM_STUDY";
    public static final String ITEM_ADD = "ITEM_ADD";
    private View.OnClickListener mOnClickListener;
    private View.OnClickListener mListenerProxy = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(v);
            }
        }
    };

    public SettingPopupWindow(Context context) {
        View popView = View.inflate(context, R.layout.pop_setting, null);
        setContentView(popView);
        setWidth(DeviceInfo.ScreenWidth() / 2);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        View resetWifiPass = popView.findViewById(R.id.bt_set_wifi_pass);
        resetWifiPass.setTag(ITEM_SET_WIFI_PASS);
        resetWifiPass.setOnClickListener(mListenerProxy);

        View resetPass = popView.findViewById(R.id.bt_reset_manage_pass);
        resetPass.setTag(ITEM_RESET_PASS);
        resetPass.setOnClickListener(mListenerProxy);

        View set_wifi = popView.findViewById(R.id.bt_set_wifi);
        set_wifi.setTag(ITEM_SET_WIFI);
        set_wifi.setOnClickListener(mListenerProxy);

        View study = popView.findViewById(R.id.bt_study);
        study.setTag(ITEM_STUDY);
        study.setOnClickListener(mListenerProxy);

        View bt_add = popView.findViewById(R.id.bt_add);
        bt_add.setTag(ITEM_ADD);
        bt_add.setOnClickListener(mListenerProxy);
        setOutsideTouchable(true);
        setFocusable(true);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setWifiName(String wifiName) {
        ((TextView) getContentView().findViewById(R.id.tv_wifi_name)).setText(wifiName);
    }
}
