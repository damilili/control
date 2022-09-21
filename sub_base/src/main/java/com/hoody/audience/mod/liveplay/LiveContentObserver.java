package com.hoody.audience.mod.liveplay;


import com.hoody.commonbase.message.IMessageObserver;

public interface LiveContentObserver extends IMessageObserver {
    default void onRoomExtendInfoChanged(String roomId){};
}
