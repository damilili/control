package com.hoody.processor;

import javax.annotation.processing.RoundEnvironment;

/**
 * Created by cdm on 2021/10/21.
 */
public interface SubProcessor {
     boolean processAnnotation(RoundEnvironment roundEnv);

     String[] getSupportTypes();
}
