package com.hoody.player.mod;

import com.hoody.commonbase.message.IMessageObserver;

public interface PlayerObserver extends IMessageObserver {

    default boolean onInfo(String var1, int var2, int var3) {
        return false;
    }

    default boolean onError(String playerTag, int var2, int var3) {
        return false;
    }

    default void onVideoSizeChanged(String playerTag, int var2, int var3, int var4, int var5) {
    }

    default void onSeekComplete(String playerTag) {
    }

    default void onBufferingUpdate(String playerTag, int var2) {
    }

    default void onCompletion(String playerTag) {
    }

    default void onPrepared(String playerTag) {
    }
}
