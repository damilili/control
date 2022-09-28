package com.hoody.wificontrol.model;

import android.view.View;

public class KeyboardItem {
    public int spanSize;
    public int layoutId;
    protected OnStudyListener mOnStudyListener;

    public void setOnStudyListener(OnStudyListener onStudyListener) {
        mOnStudyListener = onStudyListener;
    }

    public interface OnStudyListener {
        void OnStudy(View v, SingleKey singleKey);
        void OnClick(View v, SingleKey singleKey);
    }
}
