package com.hoody.audience.mod.liveplay;


import com.hoody.commonbase.message.IMessageObserver;

public interface LivePlayObserver extends IMessageObserver {
    default void onLiveBaseInfoChanged(String roomId){};

    default void onSwitchListChanged(){};
}
