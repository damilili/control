package com.hoody.annotation.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记数据模型操作实现类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ModelImpl {
}
