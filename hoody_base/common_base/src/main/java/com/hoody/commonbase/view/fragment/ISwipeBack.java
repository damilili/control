package com.hoody.commonbase.view.fragment;


import com.hoody.commonbase.customview.slidedecidable.SwipeBackLayout;

public interface ISwipeBack {

    SwipeBackLayout getSwipeBackLayout();

    void setSwipeBackEnable(boolean enable);

    void scrollToClose();

}
