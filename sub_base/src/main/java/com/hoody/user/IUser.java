package com.hoody.user;

import com.hoody.annotation.model.IModel;

/**
 * Created by cdm on 2021/11/12.
 */
public interface IUser extends IModel {
    void loginPassword(String name, String pass);
    boolean isLogin();
}
