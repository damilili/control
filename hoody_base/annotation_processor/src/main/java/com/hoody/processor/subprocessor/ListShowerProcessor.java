package com.hoody.processor.subprocessor;

import com.hoody.annotation.listshower.CollectorConfig;
import com.hoody.annotation.listshower.ListShower;
import com.hoody.processor.AbsSubProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


/**
 * 处理 注解 ListShower{@link ListShower}
 * 最终结果为 com.hoody.annotation.listshower.Collector+moduleid
 */
public class ListShowerProcessor extends AbsSubProcessor {

    public ListShowerProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public boolean processAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ListShower.class);
        if (elements.size() > 0) {
            ArrayList<ShowerInfo> showerInfos = new ArrayList<>();
            for (Element element : elements) {
                if (element instanceof TypeElement) {
                    ListShower annotation = element.getAnnotation(ListShower.class);
                    if (annotation != null) {
                        String showerName = annotation.value();
                        String showerClassName = ((TypeElement) element).getQualifiedName().toString();
                        showerInfos.add(new ShowerInfo(showerName, 0, showerClassName));
                    }
                }
            }
            if (showerInfos.size() > 0) {
                saveCollectFile(showerInfos);
            }
            return true;
        } else {
            return false;
        }
    }

    private void saveCollectFile(ArrayList<ShowerInfo> showerInfos) {
        FieldSpec fieldSpec = FieldSpec.builder(boolean.class, "called", Modifier.PRIVATE, Modifier.STATIC)
                .initializer("false")
                .build();
        //生成方法对象
        MethodSpec.Builder builder = MethodSpec.methodBuilder("collect");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("if (called) return");
        builder.addStatement("called = true");
        for (ShowerInfo showerInfo : showerInfos) {
            builder.addStatement(String.format("com.hoody.annotation.listshower.ListShowerProfile.getInstance().collcet(\"%s\", %s)", showerInfo.showerName, showerInfo.showerClassName + ".class"));
        }
        builder.returns(void.class);
        MethodSpec methodSpec = builder.build();
        //生成类对象
        ClassName iListShowerCollector = ClassName.get("com.hoody.annotation.listshower", "IListShowerCollector");
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CollectorConfig.classBaseName + moduleId);
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(iListShowerCollector);
        classBuilder.addField(fieldSpec);
        classBuilder.addMethod(methodSpec);
        TypeSpec classTypeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(CollectorConfig.packagerName, classTypeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getSupportTypes() {
        return new String[]{
                ListShower.class.getCanonicalName()
        };
    }

    static class ShowerInfo {
        public ShowerInfo(String showerName, int showerLayoutId, String showerClassName) {
            this.showerName = showerName;
            this.showerLayoutId = showerLayoutId;
            this.showerClassName = showerClassName;
        }

        String showerName;
        int showerLayoutId;
        String showerClassName;
    }
}
