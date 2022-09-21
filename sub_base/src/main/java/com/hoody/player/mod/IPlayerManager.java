package com.hoody.player.mod;

import android.util.Pair;
import android.view.Surface;

import com.hoody.annotation.model.IModel;


public interface IPlayerManager extends IModel {

    void playerPlay(String tag, String url);

    void setSurface(String tag, Surface surface);

    Pair<Integer,Integer> getVideoSize(String tag);

    void startPlayer(String tag);

    boolean isPlayerPlaying(String tag);

    void stopPlayer(String tag);

    void stopAllPlayer();

    void stopPlayerExclude(String tag);

    void setVolume(String playerTag, float volume);

    void requestAudioFocus();

    void releaseAudioFocus();
}
