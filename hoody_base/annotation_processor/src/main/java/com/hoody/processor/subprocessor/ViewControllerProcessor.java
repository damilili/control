package com.hoody.processor.subprocessor;

import com.hoody.annotation.viewcontroller.ViewController;
import com.hoody.annotation.viewcontroller.ViewControllerConfig;
import com.hoody.processor.AbsSubProcessor;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
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
 * 处理 注解 ViewController{@link com.hoody.annotation.viewcontroller.ViewController}
 */
public class ViewControllerProcessor extends AbsSubProcessor {

    public ViewControllerProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public boolean processAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ViewController.class);
        if (elements.size() > 0) {
            List<String> viewController = new ArrayList<>(elements.size());
            for (Element element : elements) {
                if (element instanceof TypeElement) {
                    ViewController annotation = element.getAnnotation(ViewController.class);
                    if (annotation != null) {
                        String fullClassName = ((TypeElement) element).getQualifiedName().toString();
                        viewController.add(fullClassName);
                    }
                }
            }
            if (viewController.size() > 0) {
                saveFile(viewController);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String[] getSupportTypes() {
        return new String[]{
                ViewController.class.getCanonicalName()
        };
    }

    private void saveFile(List<String> path2Activity) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("Ids");
        for (int i = 0; i < path2Activity.size(); i++) {
            String controllerName = path2Activity.get(i);
            //生成类对象
            FieldSpec abc = FieldSpec.builder(String.class,
                    controllerName.substring(controllerName.lastIndexOf(".") + 1),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\"" + controllerName + "\"").build();
            classBuilder.addModifiers(Modifier.PUBLIC);
            classBuilder.addField(abc);
        }
        TypeSpec classTypeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(packageName.equals("") ? ViewControllerConfig.DefaultPackagerName : packageName, classTypeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
