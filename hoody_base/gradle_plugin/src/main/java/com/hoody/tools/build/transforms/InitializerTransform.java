package com.hoody.tools.build.transforms;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.hoody.tools.build.util.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import groovy.lang.Closure;

public class InitializerTransform extends Transform {
    private static final Set<String> INITIALIZERS = new HashSet<>();
    public static File fileContainsInitClass;

    private Project project;

    public InitializerTransform(Project project) {
        this.project = project;
        INITIALIZERS.clear();
    }

    @Override
    public String getName() {
        return "com.hoody.android.common";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Logger.i("Start scan  jar file.");
        final boolean leftSlash = File.separator.equals("/");
        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }
        for (TransformInput input : transformInvocation.getInputs()) {
            // scan all jars
            for (JarInput jarInput : input.getJarInputs()) {
                String destName = jarInput.getName();
                // rename jar files
                String hexName = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                // input file
                File src = jarInput.getFile();
                // output file
                File dest = transformInvocation
                        .getOutputProvider()
                        .getContentLocation(destName + "_" + hexName, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                //scan jar file to find classes
                if (ScanUtil.shouldProcessPreDexJar(src.getAbsolutePath())) {
                    HashSet<String> classes = ScanUtil.scanJar(src, dest);
                    INITIALIZERS.addAll(classes);
                }
                FileUtils.copyFile(src, dest);
            }
            // scan class files
            for (final DirectoryInput directoryInput : input.getDirectoryInputs()) {
                final File dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                String root = directoryInput.getFile().getAbsolutePath();
                if (!root.endsWith(File.separator))
                    root += File.separator;
                final String finalRoot = root;
                ResourceGroovyMethods.eachFileRecurse(directoryInput.getFile(), new Closure(project, this) {
                    @Override
                    public Object call(Object obj) {
                        File file = (File) obj;
                        String path = file.getAbsolutePath().replace(finalRoot, "");
                        if (!leftSlash) {
                            path = path.replaceAll("\\\\", "/");
                        }
                        if (file.isFile() && ScanUtil.shouldProcessClass(path)) {
                            INITIALIZERS.add(path);
//                            try {
//                                ScanUtil.scanClass(new FileInputStream(file));
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            }
                        }
                        try {
                            FileUtils.copyDirectory(directoryInput.getFile(), dest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            }
        }
        if (fileContainsInitClass != null && fileContainsInitClass.exists() && INITIALIZERS.size() > 0) {
            for (String initializer : INITIALIZERS) {
                Logger.i("initializer = " + initializer);
            }
            insertInitCodeTo();
        }
    }

    public void insertInitCodeTo() {
        Logger.i("fileContainsInitClass =" + fileContainsInitClass.getAbsolutePath());
        if (fileContainsInitClass.getName().endsWith(".jar")) {
            insertInitCodeIntoJarFile(fileContainsInitClass);
        }
    }

    private File insertInitCodeIntoJarFile(File jarFile) {
        if (jarFile != null && jarFile.exists()) {
            File optJar = new File(jarFile.getParent(), jarFile.getName() + ".opt");
            if (optJar.exists())
                optJar.delete();
            try {
                JarFile file = new JarFile(jarFile);
                Enumeration enumeration = file.entries();
                JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));

                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                    String entryName = jarEntry.getName();
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    InputStream inputStream = file.getInputStream(jarEntry);
                    jarOutputStream.putNextEntry(zipEntry);
                    if (ScanUtil.initializerCollector.equals(entryName)) {
                        byte[] bytes = referHackWhenInit(inputStream);
                        jarOutputStream.write(bytes);
                    } else {
                        jarOutputStream.write(IOUtils.toByteArray(inputStream));
                    }
                    inputStream.close();
                    jarOutputStream.closeEntry();
                }
                jarOutputStream.close();
                file.close();

                if (jarFile.exists()) {
                    jarFile.delete();
                }
                optJar.renameTo(jarFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jarFile;
    }

    private byte[] referHackWhenInit(InputStream inputStream) {
        ClassReader cr = null;
        try {
            cr = new ClassReader(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new MyClassVisitor(Opcodes.ASM5, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    class MyClassVisitor extends ClassVisitor {

        MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }

        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if ("getInitializerUtil".equals(name)) {
                mv = new RouteMethodVisitor(Opcodes.ASM5, mv);
            }
            return mv;
        }
    }

    class RouteMethodVisitor extends MethodVisitor {

        RouteMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            mv.visitFieldInsn(Opcodes.PUTFIELD, "com/hoody/annotation/module/ModuleInitializer", "all", "Ljava/util/ArrayList;");
            for (String className : INITIALIZERS) {
                className = className.replaceAll("/", ".");
                className = className.substring(0, className.length() - ".class".length());
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, "com/hoody/annotation/module/ModuleInitializer", "all", "Ljava/util/ArrayList;");
                mv.visitLdcInsn(className);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
                mv.visitInsn(Opcodes.POP);
            }
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/hoody/annotation/module/ModuleInitializer", "all", "Ljava/util/ArrayList;");
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(3, 1);
            mv.visitEnd();
            mv = null;
        }
    }
}
