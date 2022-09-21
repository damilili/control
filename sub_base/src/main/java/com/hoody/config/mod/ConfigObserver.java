package com.hoody.config.mod;

import com.hoody.commonbase.message.IMessageObserver;

public interface ConfigObserver extends IMessageObserver {
    default void onConfigChanged(boolean isGlobal, ConfigKey... key) {
    }
}
