package com.hoody.tools.build.util;

import org.gradle.api.Project;

public class Logger {
    public static org.gradle.api.logging.Logger logger;
    static private final String TAG = "Hoody::common >>> ";

    public static void init(Project project) {
        logger = project.getLogger();
    }

    public static void i(String info) {
        if (null != info && null != logger) {
            logger.info(TAG + info);
            System.out.println(TAG + info);
        }
    }

    public static void e(String error) {
        if (null != error && null != logger) {
            logger.error(TAG + error);
        }
        System.out.println(TAG + error);
    }

    public static void w(String warning) {
        if (null != warning && null != logger) {
            logger.warn(TAG + warning);
        }
        System.out.println(TAG + warning);
    }
}
