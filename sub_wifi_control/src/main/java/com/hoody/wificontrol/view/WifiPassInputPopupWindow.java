package com.hoody.wificontrol.view;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hoody.annotation.model.ModelManager;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.wificontrol.R;

public class WifiPassInputPopupWindow extends PopupWindow {
    private static final String TAG = "WifiListPopupWindow";

    public WifiPassInputPopupWindow(Context context) {
        View popView = View.inflate(context, R.layout.pop_wifi_pass, null);
        setContentView(popView);
        popView.findViewById(R.id.bt_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wifiName = ((TextView) popView.findViewById(R.id.tv_wifi_name)).getText().toString();
                String pass = ((EditText) popView.findViewById(R.id.et_wifi_pass)).getText().toString();
                ModelManager.getModel(IWifiDeviceModel.class).setWifiInfo(wifiName,pass);
                dismiss();
            }
        });
        setWidth(DeviceInfo.ScreenWidth() / 2);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
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
