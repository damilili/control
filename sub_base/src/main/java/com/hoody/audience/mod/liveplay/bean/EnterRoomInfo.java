package com.hoody.audience.mod.liveplay.bean;

public class EnterRoomInfo {
    private EnterRoomInfo() {
    }

    private String roomId;
    private String playUrl;
    private String backPicUrl;
    private boolean preShowView = false;
    private boolean slideSwitch = false;

    public String getRoomId() {
        return roomId;
    }

    public String getBackPicUrl() {
        return backPicUrl;
    }

    public boolean isPreShowView() {
        return preShowView;
    }
    public boolean isSlideSwitch() {
        return slideSwitch;
    }
    public String getPlayUrl() {
        return playUrl;
    }


    public static class Builder {
        EnterRoomInfo enterRoomInfo = new EnterRoomInfo();

        public Builder setPlayUrl(String playUrl) {
            enterRoomInfo.playUrl = playUrl;
            return this;
        }

        public Builder setRoomId(String roomId) {
            enterRoomInfo.roomId = roomId;
            return this;
        }

        public Builder setBackPicUrl(String backPicUrl) {
            enterRoomInfo.backPicUrl = backPicUrl;
            return this;
        }

        public Builder setPreShowView(boolean preShowView) {
            enterRoomInfo.preShowView = preShowView;
            return this;
        }
        public Builder setSlideSwitch(boolean slideSwitch) {
            enterRoomInfo.slideSwitch = slideSwitch;
            return this;
        }
        public EnterRoomInfo build() {
            return enterRoomInfo;
        }
    }
}
