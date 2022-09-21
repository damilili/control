package com.hoody.audience.view.fragment;

import com.hoody.audience.view.fragment.content.MenuContentBaseFragment;
import com.hoody.audience.view.fragment.content.PlayContentBaseFragment;

public interface ILivePlayContentProducer {
    /**
     * @return 竖屏直播间内容fragment
     */
    PlayContentBaseFragment getLivePortraitPlayContentFragment();

    /**
     * @return 横屏直播间内容fragment
     */
    PlayContentBaseFragment getLiveLandscapePlayContentFragment();

    /**
     * @return 直播间侧边栏内容fragment
     */
    MenuContentBaseFragment getLiveMenuContentFragment();
}
