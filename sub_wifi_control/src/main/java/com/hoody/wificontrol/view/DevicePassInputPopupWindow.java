package com.hoody.wificontrol.view;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hoody.commonbase.util.ToastUtil;
import com.hoody.wificontrol.R;

public class DevicePassInputPopupWindow extends PopupWindow {
    private static final String TAG = "WifiListPopupWindow";
    private final EditText et_wifi_pass;
    private final EditText et_wifi_pass_sure;

    public DevicePassInputPopupWindow(Context context) {
        View popView = View.inflate(context, R.layout.pop_device_pass, null);
        setContentView(popView);
        et_wifi_pass = popView.findViewById(R.id.et_wifi_pass);
        et_wifi_pass_sure = popView.findViewById(R.id.et_wifi_pass_sure);
        popView.findViewById(R.id.bt_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable text = et_wifi_pass.getText();
                if (text.toString().length() < 8) {
                    ToastUtil.showToast(v.getContext(), "访问密码不能小于8位");
                    return;
                }
                if (!et_wifi_pass.getText().toString().equals(et_wifi_pass_sure.getText().toString())) {
                    ToastUtil.showToast(v.getContext(), "两次输入的密码不一致");
                    return;
                }
                ToastUtil.showToast(v.getContext(), "设置成功");
            }
        });
        setOutsideTouchable(true);
        setFocusable(true);
    }
}
