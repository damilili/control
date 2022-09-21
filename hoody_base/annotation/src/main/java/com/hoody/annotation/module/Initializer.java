package com.hoody.annotation.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模块初始化标记
 *  priority 权重，用于确定初始化顺序
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Initializer {
    int priority() default 0;
}
