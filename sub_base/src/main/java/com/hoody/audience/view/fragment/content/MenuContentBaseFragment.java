package com.hoody.audience.view.fragment.content;

import androidx.fragment.app.Fragment;

import com.hoody.audience.view.fragment.ILiveControl;
import com.hoody.audience.view.fragment.IMenuControl;
import com.hoody.audience.view.fragment.SlidableDeciderProvider;
import com.hoody.commonbase.view.fragment.BaseFragment;

public abstract class MenuContentBaseFragment extends BaseFragment implements IMenuControl, SlidableDeciderProvider, ILiveControl {

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
     @Override
    final public void openDrawer() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof IMenuControl) {
            ((IMenuControl) parentFragment).openDrawer();
        }
    }

    @Override
    final public void closeDrawer(boolean anim) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof IMenuControl) {
            ((IMenuControl) parentFragment).closeDrawer(anim);
        }
    }

    @Override
    final public boolean isDrawerOpen() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof IMenuControl) {
            return ((IMenuControl) parentFragment).isDrawerOpen();
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
