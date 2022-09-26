package com.hoody.wificontrol.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hoody.annotation.model.ModelManager;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.wificontrol.R;

public class DeviceWifiPassInputPopupWindow extends PopupWindow {
    private static final String TAG = "DevicePassInputPopupWindow";

    public DeviceWifiPassInputPopupWindow(Context context) {
        View popView = View.inflate(context, R.layout.pop_device_wifi_pass_input, null);
        setContentView(popView);
        setWidth(DeviceInfo.ScreenWidth() / 2);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popView.findViewById(R.id.bt_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = ((EditText) popView.findViewById(R.id.et_device_pass)).getText().toString();
                ModelManager.getModel(IWifiDeviceModel.class).modifyWifiPass(s);
                dismiss();
            }
        });
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        setOutsideTouchable(true);
        setFocusable(true);
    }
}
