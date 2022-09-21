package com.hoody.processor.subprocessor;

import com.hoody.annotation.router.Router;
import com.hoody.annotation.router.RouterConfig;
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

/**
 * 处理 注解 Router{@link Router}
 * 最终结果为com.hoody.annotation.routercollect.RouterUtil+moduleid
 */
public class RouterProcessor extends AbsSubProcessor {

    public RouterProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public boolean processAnnotation(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Router.class);
        if (elements.size() > 0) {
            Map<String, String> path2Activity = new HashMap<>(elements.size());
            for (Element element : elements) {
                if (element instanceof TypeElement) {
                    Router annotation = element.getAnnotation(Router.class);
                    if (annotation != null) {
                        String path = annotation.value();
                        String fullClassName = ((TypeElement) element).getQualifiedName().toString();
                        path2Activity.put(path, fullClassName + ".class");
                    }
                }
            }
            if (path2Activity.size() > 0) {
                saveRouterFile(path2Activity);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String[] getSupportTypes() {
        return new String[]{
                Router.class.getCanonicalName()
        };
    }

    private void saveRouterFile(Map<String, String> path2Activity) {
        FieldSpec fieldSpec = FieldSpec.builder(boolean.class, "called", Modifier.PRIVATE, Modifier.STATIC)
                .initializer("false")
                .build();
        //生成方法对象
        MethodSpec.Builder builder = MethodSpec.methodBuilder("putPath");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("if (called) return");
        builder.addStatement("called = true");
        for (String key : path2Activity.keySet()) {
            builder.addStatement(String.format("com.hoody.annotation.router.RouterHelper.getInstance().putPath(\"%s\", %s)", key, path2Activity.get(key)));
        }
        builder.returns(void.class);
        MethodSpec methodSpec = builder.build();
        //生成类对象
        ClassName iRouter = ClassName.get("com.hoody.annotation.router", "IRouterCollector");
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(RouterConfig.classBaseName + moduleId);
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(iRouter);
        classBuilder.addField(fieldSpec);
        classBuilder.addMethod(methodSpec);
        TypeSpec classTypeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder(RouterConfig.packagerName, classTypeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
