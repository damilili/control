package com.hoody.audience.view.fragment;

/**
 * 直播控制
 */
public interface ILiveControl {

    void closeLiveRoom();

    void pauseLiveStream();

    boolean isLiveStreamPaused();

    void restartLiveStream();
}
