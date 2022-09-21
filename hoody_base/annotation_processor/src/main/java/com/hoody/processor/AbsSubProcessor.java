package com.hoody.processor;


import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * Created by cdm on 2021/10/21.
 */
public abstract class AbsSubProcessor implements SubProcessor {
     private static final String OPTION_KEY_moduleId = "moduleId";
     private static final String OPTION_KEY_packageName = "packageName";
     protected final String moduleId;
     protected final String packageName;
     protected final Filer mFiler;

     public AbsSubProcessor(ProcessingEnvironment processingEnv) {
          mFiler = processingEnv.getFiler();
          String moduleId = processingEnv.getOptions().get(OPTION_KEY_moduleId);
          this.moduleId = moduleId == null ? "" : moduleId;
          String packageName = processingEnv.getOptions().get(OPTION_KEY_packageName);
          this.packageName = packageName == null ? "" : packageName;
     }
}
