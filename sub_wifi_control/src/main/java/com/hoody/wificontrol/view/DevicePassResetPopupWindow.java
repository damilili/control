package com.hoody.wificontrol.view;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.hoody.annotation.model.ModelManager;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.commonbase.util.ToastUtil;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.wificontrol.R;

public class DevicePassResetPopupWindow extends PopupWindow {
    private static final String TAG = "WifiListPopupWindow";
    private final EditText et_device_old_pass;
    private final EditText et_device_pass;
    private final EditText et_device_pass_sure;

    public DevicePassResetPopupWindow(Context context) {
        View popView = View.inflate(context, R.layout.pop_device_pass_reset, null);
        setContentView(popView);
        setWidth(DeviceInfo.ScreenWidth() / 2);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        et_device_old_pass = popView.findViewById(R.id.et_device_old_pass);
        et_device_pass = popView.findViewById(R.id.et_device_pass);
        et_device_pass_sure = popView.findViewById(R.id.et_device_pass_sure);
        popView.findViewById(R.id.bt_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = et_device_old_pass.getText().toString();
                String newPass = et_device_pass.getText().toString();
                if (newPass.length() < 8) {
                    ToastUtil.showToast(v.getContext(), "访问密码不能小于8位");
                    return;
                }
                if (!newPass.equals(et_device_pass_sure.getText().toString())) {
                    ToastUtil.showToast(v.getContext(), "两次输入的密码不一致");
                    return;
                }
                ModelManager.getModel(IWifiDeviceModel.class).modifyPass(oldPass,newPass);
                dismiss();
            }
        });
        setOutsideTouchable(true);
        setFocusable(true);
    }
}
