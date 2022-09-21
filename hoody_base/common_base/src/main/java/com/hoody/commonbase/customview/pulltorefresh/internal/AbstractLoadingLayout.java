package com.hoody.commonbase.customview.pulltorefresh.internal;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public abstract class AbstractLoadingLayout extends FrameLayout {
    public AbstractLoadingLayout(@NonNull Context context) {
        super(context);
    }

    public abstract void setPullLabel(String pullLabel);


    public abstract void setRefreshingLabel(String refreshingLabel);

    public abstract void setReleaseLabel(String releaseLabel);

    public abstract void setTextColor(int color);

    public abstract void setTextVisibility(int visibility);

    //开始拖动
    public abstract void startPull();

    //继续拖动将触发refresh
    public abstract void pullToRefresh();

    //释放将触发refresh
    public abstract void releaseToRefresh();

    //正在刷新
    public abstract void refreshing();

    //复位
    public abstract void reset();

    /** 移动回调
     * @param dragY
     * @param byUser 用户手动拖动？
     * @return 是否处理了移动
     */
    public abstract boolean onMove(float dragY,boolean byUser);

}
