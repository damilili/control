package com.hoody.wificontrol.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hoody.annotation.router.Router;
import com.hoody.commonbase.view.fragment.SwipeBackFragment;
import com.hoody.wificontrol.R;
import com.hoody.wificontrol.model.SingleKey;

@Router("wifi/keyset")
public class KeySetFragment extends SwipeBackFragment {
    private static final String TAG = "DevicePassInputPopupWindow";
    private EditText et_key_name;

    private ImageView iv_key_logo_left;
    private ImageView iv_key_logo_top;
    private ImageView iv_key_logo_right;
    private ImageView iv_key_logo_bottom;
    private ImageView iv_key_background;
    private SingleKey key;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        et_key_name = (EditText) findViewById(R.id.et_key_name);
        iv_key_logo_left = (ImageView) findViewById(R.id.iv_key_logo_left);
        iv_key_logo_top = (ImageView) findViewById(R.id.iv_key_logo_top);
        iv_key_logo_right = (ImageView) findViewById(R.id.iv_key_logo_right);
        iv_key_logo_bottom = (ImageView) findViewById(R.id.iv_key_logo_bottom);
        findViewById(R.id.bt_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    public void setKey(SingleKey key) {
        this.key = key;
        et_key_name.setText(key.getName());
        et_key_name.setHint(key.getName());
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected View createContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View popView = View.inflate(getContext(), R.layout.fragment_device_key_set, null);
        return popView;
    }
}
