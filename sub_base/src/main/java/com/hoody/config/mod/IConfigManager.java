package com.hoody.config.mod;

import com.hoody.annotation.model.IModel;

public interface IConfigManager extends IModel {

    IConfig getGlobalConfig();

    IUserConfig getCurrentUserConfig();
}
