package com.hoody.processor.subprocessor;

import com.hoody.annotation.module.Initializer;
import com.hoody.annotation.module.InitializerUtil;
import com.hoody.processor.AbsSubProcessor;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * 处理 注解 Initializer{@link com.hoody.annotation.module.Initializer}
 */
public class InitializerProcessor extends AbsSubProcessor {


    public InitializerProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public boolean processAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Initializer.class);
        System.out.println("path = " + elements.size());
        if (elements.size() > 0) {
            if (elements.size() > 1) {
                throw new IllegalStateException("同一个模块下只能有一个初始化器");
            }
            for (Element element : elements) {
                if (element instanceof TypeElement) {
                    Initializer annotation = element.getAnnotation(Initializer.class);
                    if (annotation != null) {
//                        String path = annotation.value();
//                        System.out.println("path = " + path);
                        String fullClassName = ((TypeElement) element).getQualifiedName().toString();
                        saveFile(annotation.priority(), fullClassName + ".class");
                        return true;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String[] getSupportTypes() {
        return new String[]{
                Initializer.class.getCanonicalName()
        };
    }

    private void saveFile(int priority, String initializer) {
        //生成方法对象
        FieldSpec.Builder builder = FieldSpec.builder(Class.class, InitializerUtil.FieldName, Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC);
        builder.initializer(initializer);
        //生成类对象
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(InitializerUtil.classBaseName + InitializerUtil.FieldNameSplit + priority + InitializerUtil.FieldNameSplit + System.currentTimeMillis());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addField(builder.build());
        TypeSpec classTypeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(InitializerUtil.packagerName, classTypeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
