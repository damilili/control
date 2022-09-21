package com.hoody.user;


import com.hoody.commonbase.message.IMessageObserver;

/**
 * Created by cdm on 2021/11/19.
 */
public interface UserObserver extends IMessageObserver {
    default void onLogin(boolean success, String uid){};

    default void onLogout(String uid){};
}
