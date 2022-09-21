package com.hoody.audience.view.fragment.content;

import androidx.fragment.app.Fragment;

import com.hoody.audience.view.fragment.IClearContentControl;
import com.hoody.audience.view.fragment.ILiveContent;
import com.hoody.audience.view.fragment.ILiveControl;
import com.hoody.audience.view.fragment.IMenuControl;
import com.hoody.commonbase.view.fragment.BaseFragment;


/**
 * Created by cdm on 2019/11/18.
 * 直播间公共业务集中在这里处理
 */
public abstract class LiveContentBaseFragment extends BaseFragment implements ILiveContent {
    private static final String TAG = "LiveContentBaseFragment";

    @Override
    public boolean onLiveCloseClick() {
        return false;
    }

    @Override
    final public void closeLiveRoom() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ILiveControl) {
            ((ILiveControl) parentFragment).closeLiveRoom();
        }
    }

    @Override
    final public void pauseLiveStream() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ILiveControl) {
            ((ILiveControl) parentFragment).pauseLiveStream();
        }
    }

    @Override
    final public boolean isLiveStreamPaused() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ILiveControl) {
            return ((ILiveControl) parentFragment).isLiveStreamPaused();
        }
        return false;
    }

    @Override
    final public void restartLiveStream() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ILiveControl) {
            ((ILiveControl) parentFragment).restartLiveStream();
        }
    }

    /**
     * 清屏
     */
    final public void clearContent(boolean anim) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof IClearContentControl) {
            ((IClearContentControl) parentFragment).clearContent(anim);
        }
    }


    /**
     * 清屏恢复
     */
    final public void unclearContent(boolean anim) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof IClearContentControl) {
            ((IClearContentControl) parentFragment).unclearContent(anim);
        }
    }

    /**
     * @return 是否处于清屏状态
     */
    final public boolean isContentCleared() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof IClearContentControl) {
            return ((IClearContentControl) parentFragment).isContentCleared();
        }
        return false;
    }

    @Override
    final public void openDrawer() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ILiveContent) {
            ((ILiveContent) parentFragment).openDrawer();
        }
    }

    @Override
    final public void closeDrawer(boolean anim) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ILiveContent) {
            ((ILiveContent) parentFragment).closeDrawer(anim);
        }
    }

    @Override
    final public boolean isDrawerOpen() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ILiveContent) {
            return ((ILiveContent) parentFragment).isDrawerOpen();
        }
        return false;
    }

    @Override
    final public void changeDrawerContent() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof IMenuControl) {
            ((IMenuControl) parentFragment).changeDrawerContent();
        }
    }
}
