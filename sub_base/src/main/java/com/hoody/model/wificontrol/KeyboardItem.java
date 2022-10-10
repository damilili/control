package com.hoody.model.wificontrol;

import android.view.View;

import com.hoody.model.wificontrol.SingleKey;

public class KeyboardItem {
    public int spanSize;
    public int layoutId;
    protected OnKeyClickListener mOnKeyClickListener;

    public void setOnStudyListener(OnKeyClickListener onKeyClickListener) {
        mOnKeyClickListener = onKeyClickListener;
    }

    public interface OnKeyClickListener {
        void OnStudy(View v, SingleKey singleKey);
        void OnClick(View v, SingleKey singleKey);
    }
}
