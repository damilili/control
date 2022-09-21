package com.hoody.audience.view.fragment;

import android.graphics.Rect;

/**
 * 内容层需要实现的接口
 */
public interface ILiveContent extends IMenuControl, SlidableDeciderProvider, IClearContentControl, ILiveControl {

    /**
     * 视频区域的位置和尺寸变更
     *
     * @param rect 视频区域的矩形位置和尺寸
     */
    void onVideoViewVisiableRectChanged(Rect rect);

    /**
     * 关闭按钮的点击
     *
     * @return 是否消费了
     */
    boolean onLiveCloseClick();

    /**
     * 关闭内容页面
     * 之所以加了这个接口是因为：子fragment的回收有问题
     */
    void beforeContentRemove();
}