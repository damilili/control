package com.hoody.wificontrol.view;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hoody.annotation.router.Router;
import com.hoody.commonbase.customview.ChildMoveLayout;
import com.hoody.commonbase.customview.slidedecidable.SlideDecidableLayout;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.message.Messenger;
import com.hoody.commonbase.view.fragment.SwipeBackFragment;
import com.hoody.model.wificontrol.IWifiObserver;
import com.hoody.wificontrol.R;
import com.hoody.model.wificontrol.SingleKey;

import java.io.Serializable;

import top.defaults.colorpicker.ColorPickerPopup;

@Router("wifi/keyset")
public class KeySetFragment extends SwipeBackFragment {
    private static final String TAG = "DevicePassInputPopupWindow";
    private EditText et_key_name;

    private ImageView iv_key_logo_left;
    private ImageView iv_key_logo_top;
    private ImageView iv_key_logo_right;
    private ImageView iv_key_logo_bottom;
    private SingleKey mKey;
    private SeekBar mSeekBarHeight;
    private SeekBar mSeekBarWidth;
    private SeekBar mSeekBarTextSize;
    private View bt_back_color;
    private View bt_text_color;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        configView();
        initData();
        refreshKeyByData();
    }

    private void initData() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            Serializable keyBean = arguments.getSerializable("keyBean");
            if (keyBean instanceof SingleKey) {
                mKey = ((SingleKey) keyBean);
            }
        }
        if (mKey == null) {
            mKey = new SingleKey(System.currentTimeMillis() + "", "", "");
            mKey.setPosX(9);
            mKey.setPosY(9);
            mKey.setWidth(3);
            mKey.setHeight(3);
            mKey.setTextSize(20);
            mKey.setBackgroundColor(0x77777777);
        }
    }

    private void configView() {
        et_key_name = (EditText) findViewById(R.id.et_key_name);
        iv_key_logo_left = (ImageView) findViewById(R.id.iv_key_logo_left);
        iv_key_logo_top = (ImageView) findViewById(R.id.iv_key_logo_top);
        iv_key_logo_right = (ImageView) findViewById(R.id.iv_key_logo_right);
        iv_key_logo_bottom = (ImageView) findViewById(R.id.iv_key_logo_bottom);
        mSeekBarHeight = ((SeekBar) findViewById(R.id.seek_key_height));
        mSeekBarWidth = ((SeekBar) findViewById(R.id.seek_key_width));
        mSeekBarTextSize = ((SeekBar) findViewById(R.id.seek_key_text_size));
        bt_back_color = findViewById(R.id.bt_back_color);
        bt_text_color = findViewById(R.id.bt_text_color);
        bt_back_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerPopup.Builder(getContext())
                        .enableAlpha(true)
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .initialColor(mKey.getBackgroundColor())
                        .showValue(true)
                        .build()
                        .show(new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                mKey.setBackgroundColor(color);
                                refreshKeyByData();
                            }
                        });
            }
        });
        bt_text_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerPopup.Builder(getContext())
                        .enableAlpha(true)
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .initialColor(mKey.getTextColor())
                        .showValue(true)
                        .build()
                        .show(new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                mKey.setTextColor(color);
                                refreshKeyByData();
                            }
                        });
            }
        });
        findViewById(R.id.bt_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = et_key_name.getText().toString();
                mKey.setName(s);
                Messenger.sendTo(IWifiObserver.class).onKeyInfoChanged(mKey);
                close();
            }
        });
        mSeekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mKey.setTextSize(progress + 5);
                et_key_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, mKey.getTextSize());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBarWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mKey.setWidth(progress + 2);
                ViewGroup.LayoutParams layoutParams = et_key_name.getLayoutParams();
                layoutParams.width = mKey.getWidth() * ChildMoveLayout.SPLIT_LENGTH;
                et_key_name.setLayoutParams(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        getSwipeBackLayout().addDecider(new SlideDecidableLayout.SlidableDecider(mSeekBarWidth) {
            @Override
            public boolean slidable(PointF curEvPos, SlideDecidableLayout.SlideOrientation slideOrientation) {
                if (isTouchPointInView(mSeekBarWidth, curEvPos.x, curEvPos.y)) {
                    return false;
                }
                return true;
            }
        });
        mSeekBarHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Logger.i(TAG, "progerss: " + progress);
                mKey.setHeight(progress + 2);
                ViewGroup.LayoutParams layoutParams = et_key_name.getLayoutParams();
                layoutParams.height = mKey.getHeight() * ChildMoveLayout.SPLIT_LENGTH;
                et_key_name.setLayoutParams(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void refreshKeyByData() {

        ViewGroup.LayoutParams layoutParams = et_key_name.getLayoutParams();
        layoutParams.height = mKey.getHeight() * ChildMoveLayout.SPLIT_LENGTH;
        layoutParams.width = mKey.getWidth() * ChildMoveLayout.SPLIT_LENGTH;

        mSeekBarHeight.setProgress(mKey.getHeight() - 2);
        mSeekBarWidth.setProgress(mKey.getWidth() - 2);
        mSeekBarTextSize.setProgress(mKey.getTextSize() - 5);

        et_key_name.setText(mKey.getName());
        et_key_name.setTextColor(mKey.getTextColor());
        et_key_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, mKey.getTextSize());
        et_key_name.setBackgroundColor(mKey.getBackgroundColor());
        et_key_name.setPadding(0, 0, 0, 0);
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
