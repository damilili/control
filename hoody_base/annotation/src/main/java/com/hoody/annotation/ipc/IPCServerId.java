package com.hoody.annotation.ipc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by cdm on 2021/10/20.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface IPCServerId {
}
