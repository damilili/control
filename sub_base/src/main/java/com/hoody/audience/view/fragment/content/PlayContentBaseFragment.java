package com.hoody.audience.view.fragment.content;

import android.graphics.Rect;

import com.hoody.audience.view.fragment.ILiveContent;
import com.hoody.commonbase.customview.slidedecidable.SlideDecidableLayout;

import org.json.JSONObject;

/**
 * 这是直播间播放功能容器，所有播放端都有而主播端没有的功能都要放到这里
 */
public abstract class PlayContentBaseFragment extends LiveContentBaseFragment implements ILiveContent {
    private static final String TAG = PlayContentBaseFragment.class.getSimpleName();
    protected SlideDecidableLayout.SlidableDecider mSlidableDecider;

    public PlayContentBaseFragment() {
        super();
    }



    @Override
    public void onVideoViewVisiableRectChanged(Rect rect) {

    }

    protected void addPubChatMessage(JSONObject result) {

    }

    @Override
    public boolean clearContentEnable() {
        return true;
    }

    @Override
    final public void beforeContentRemove() {

    }

}
