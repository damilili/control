package com.hoody.processor;

import com.google.auto.service.AutoService;
import com.hoody.processor.subprocessor.IPCProcessor;
import com.hoody.processor.subprocessor.InitializerProcessor;
import com.hoody.processor.subprocessor.ListShowerProcessor;
import com.hoody.processor.subprocessor.ModulProcessor;
import com.hoody.processor.subprocessor.RouterProcessor;
import com.hoody.processor.subprocessor.ViewControllerProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Created by cdm on 2021/10/21.
 */
@AutoService(javax.annotation.processing.Processor.class)
public class AnnoProcessor extends AbstractProcessor {
    private List<SubProcessor> mSubProcessors = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mSubProcessors.add(new ModulProcessor(processingEnv));
        mSubProcessors.add(new IPCProcessor(processingEnv));
        mSubProcessors.add(new ListShowerProcessor(processingEnv));
        mSubProcessors.add(new RouterProcessor(processingEnv));
        mSubProcessors.add(new ViewControllerProcessor(processingEnv));
        mSubProcessors.add(new InitializerProcessor(processingEnv));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        for (SubProcessor mSubProcessor : mSubProcessors) {
            for (String supportType : mSubProcessor.getSupportTypes()) {
                supportTypes.add(supportType);
            }
        }
        for (String supportType : supportTypes) {
            System.out.println("supportTypes = " + supportType);
        }
        return supportTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (SubProcessor mSubProcessor : mSubProcessors) {
            mSubProcessor.processAnnotation(roundEnv);
        }
        return false;
    }
}
