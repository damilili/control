package com.hoody.processor.subprocessor;

import com.hoody.annotation.model.ModelConfig;
import com.hoody.annotation.model.ModelImpl;
import com.hoody.processor.AbsSubProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * 处理 注解 ModelImpl{@link ModelImpl}
 */
public class ModulProcessor extends AbsSubProcessor {

    public ModulProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public boolean processAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ModelImpl.class);
        System.out.println("path = " + elements.size());
        if (elements.size() > 0) {
            List<String> path2Activity = new ArrayList<>(elements.size());
            for (Element element : elements) {
                if (element instanceof TypeElement) {
                    ModelImpl annotation = element.getAnnotation(ModelImpl.class);
                    if (annotation != null) {
//                        String path = annotation.value();
//                        System.out.println("path = " + path);
                        String fullClassName = ((TypeElement) element).getQualifiedName().toString();
                        path2Activity.add(fullClassName + ".class");
                    }
                }
            }
            if (path2Activity.size() > 0) {
                saveFile(path2Activity);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String[] getSupportTypes() {
        return new String[]{
                ModelImpl.class.getCanonicalName()
        };
    }

    private void saveFile(List<String> path2Activity) {
        FieldSpec fieldSpec = FieldSpec.builder(boolean.class, "called", Modifier.PRIVATE, Modifier.STATIC)
                .initializer("false")
                .build();
        //生成方法对象
        MethodSpec.Builder builder = MethodSpec.methodBuilder("collect");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("if (called) return");
        builder.addStatement("called = true");
        for (String key : path2Activity) {
            builder.addStatement(String.format("com.hoody.annotation.model.ModelHelper.getInstance().putModelImpl(%s)", key));
        }
        builder.returns(void.class);
        MethodSpec methodSpec = builder.build();
        //生成类对象
        ClassName iRouter = ClassName.get("com.hoody.annotation.model", "IModelCollector");
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(ModelConfig.classBaseName + moduleId);
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(iRouter);
        classBuilder.addField(fieldSpec);
        classBuilder.addMethod(methodSpec);
        TypeSpec classTypeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(ModelConfig.packagerName, classTypeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
