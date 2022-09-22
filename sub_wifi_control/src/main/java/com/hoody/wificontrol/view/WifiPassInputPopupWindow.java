package com.hoody.wificontrol.view;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hoody.wificontrol.R;

public class WifiPassInputPopupWindow extends PopupWindow {
    private static final String TAG = "WifiListPopupWindow";

    public WifiPassInputPopupWindow(Context context) {
        View popView = View.inflate(context, R.layout.pop_wifi_pass, null);
        setContentView(popView);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        setOutsideTouchable(true);
        setFocusable(true);
    }
    public void setWifiName(String wifiName){
        ((TextView) getContentView().findViewById(R.id.tv_wifi_name)).setText(wifiName);
    }
}
