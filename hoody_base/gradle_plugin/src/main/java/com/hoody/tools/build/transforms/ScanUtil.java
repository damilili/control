package com.hoody.tools.build.transforms;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScanUtil {
    private static final String annotationProduceInitializerUtil_pre = "com/hoody/annotation/initializer/InitializerUtil_";
    public static final String initializerCollector = "com/hoody/annotation/module/ModuleInitializer.class";

    public static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    public static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith(annotationProduceInitializerUtil_pre);
    }

    /**
     * scan jar file
     *
     * @param src      All jar files that are compiled into apk
     * @param destFile dest file after this transform
     * @return
     */
    public static HashSet<String> scanJar(File src, File destFile) {
        HashSet<String> result = new HashSet<>();
        if (src.exists()) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(src);
                Enumeration enumeration = jarFile.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                    String entryName = jarEntry.getName();
                    if (shouldProcessClass(entryName)) {
                        result.add(entryName);
                    } else {
                        if (initializerCollector.equals(entryName)) {
                            // mark this jar file contains LogisticsCenter.class
                            // After the scan is complete, we will generate register code into this file
                            InitializerTransform.fileContainsInitClass = destFile;
                        }
                    }
                }
                jarFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;

        }
        return result;
    }
}
