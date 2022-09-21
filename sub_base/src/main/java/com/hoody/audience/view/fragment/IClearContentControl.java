package com.hoody.audience.view.fragment;

/**
 * 清屏控制接口
 */
public interface IClearContentControl {
    /**
     * 清屏
     */
    void clearContent(boolean anim);

    /**
     * 清屏恢复
     */
    void unclearContent(boolean anim);

    /**
     * @return 是否处于清屏状态
     */
    boolean isContentCleared();
    /**
     * @return 清屏功能是否可用，在手势将滑动的时候将会调用这个方法
     */
    boolean clearContentEnable();

}
