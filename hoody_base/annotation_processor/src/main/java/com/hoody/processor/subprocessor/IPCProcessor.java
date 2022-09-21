package com.hoody.processor.subprocessor;

import com.hoody.annotation.ipc.IPCConfig;
import com.hoody.annotation.ipc.IPCServerId;
import com.hoody.processor.AbsSubProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * 处理 注解 IPCServerId{@link IPCServerId}
 */
public class IPCProcessor extends AbsSubProcessor {

    public IPCProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public boolean processAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(IPCServerId.class);
        if (elements.size() > 0) {
            Map<String, String> interface2implement = new HashMap<>(elements.size());
            for (Element element : elements) {
                if (element instanceof TypeElement) {
                    IPCServerId annotation = element.getAnnotation(IPCServerId.class);
                    if (annotation != null) {
                        TypeMirror superclass = ((TypeElement) element).getSuperclass();
                        String superClassName = superclass.toString();
                        String interfaceName = superClassName.substring(0, superClassName.lastIndexOf(".Stub"));
                        String implementName = ((TypeElement) element).getQualifiedName().toString();
                        System.out.println(String.format("interfaceName = %s ,implementName = %s", interfaceName, implementName));
                        interface2implement.put(interfaceName, implementName);
                    }
                }
            }
            if (interface2implement.size() > 0) {
                saveFile(interface2implement);
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public String[] getSupportTypes() {
        return new String[]{IPCServerId.class.getCanonicalName()};
    }

    private void saveFile(Map<String, String> interface2implement) {
        FieldSpec fieldSpec = FieldSpec.builder(boolean.class, "called", Modifier.PRIVATE, Modifier.STATIC)
                .initializer("false")
                .build();
        //生成方法对象
        MethodSpec.Builder builder = MethodSpec.methodBuilder("putInterfaceImplements");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("if (called) return");
        builder.addStatement("called = true");
        for (String key : interface2implement.keySet()) {
            builder.addStatement(String.format("com.hoody.annotation.ipc.IPCHelper.getInstance().putInterfaceImplement(\"%s\", %s)", key, interface2implement.get(key) + ".class"));
        }
        builder.returns(void.class);
        MethodSpec methodSpec = builder.build();
        //生成类对象
        ClassName iIPCCollector = ClassName.get("com.hoody.annotation.ipc", "IIPCCollector");
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(IPCConfig.classBaseName + moduleId);
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(iIPCCollector);
        classBuilder.addField(fieldSpec);
        classBuilder.addMethod(methodSpec);
        TypeSpec classTypeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(IPCConfig.packagerName, classTypeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
