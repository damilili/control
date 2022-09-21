package com.hoody.config.mod;

public enum ConfigKey {
    /**
     * 是否开启直播间无限循环模式
     * true: 开启
     */
    LIVE_SWITCH_CYCLE_boolean("live_switch_cycle"),
    CURRENT_USER_ID_String("current_user_id");
    private String name;

    ConfigKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
