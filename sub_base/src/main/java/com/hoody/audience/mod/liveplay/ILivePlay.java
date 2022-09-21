package com.hoody.audience.mod.liveplay;

import android.content.Context;

import com.hoody.annotation.model.IModel;
import com.hoody.audience.mod.liveplay.bean.EnterRoomInfo;
import com.hoody.audience.mod.liveplay.bean.LiveBaseInfo;
import com.hoody.audience.mod.liveplay.bean.LiveExtendInfo;

import java.util.ArrayList;

public interface ILivePlay extends IModel {
    void enterRoom(Context context, EnterRoomInfo enterRoomInfo);

    void dumpLiveInfo(String roomId);

    void clearAllCacheInfo();

    String getCurrentLiveId();

    void setCurrentLiveId(String roomId);

    LiveBaseInfo getLiveBaseInfo(String roomId);

    ArrayList<String> getLiveSwitchList();

    LiveExtendInfo getCurrentRoomExtendInfo();

}
