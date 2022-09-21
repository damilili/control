package com.hoody.config.mod;

import java.util.Map;
import java.util.Set;

public interface IConfig {

    String getStringConfig(ConfigKey key, String defaultValue);

    boolean getBooleanConfig(ConfigKey key, boolean defaultValue);

    int getIntConfig(ConfigKey key, int defaultValue);

    boolean putIntConfig(ConfigKey key, int value);

    boolean putStringConfig(ConfigKey key, String value);

    boolean putBooleanConfig(ConfigKey key, boolean value);

    boolean removeConfig(ConfigKey key);

    /**
     * 批量修改
     */
    void putConfigBatch(Map<ConfigKey, ?> config);

    /**
     * 批量删除
     */
    void removeConfigBatch(Set<ConfigKey> configKeys);

}
