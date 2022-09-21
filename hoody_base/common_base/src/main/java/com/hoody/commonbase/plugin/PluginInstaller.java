package com.hoody.commonbase.plugin;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;

import com.hoody.annotation.module.InitializerUtil;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.util.FileUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

/**
 * 插件化工具类
 * 要求：插件放到目录data/data/(packageName)/plugin目录下
 */
public class PluginInstaller {
    private static final String TAG = "PluginUtil";
    public static PluginInstaller INSTANCE;
    private final String PLUGIN_DIR_NAME = "plugin";
    private final String PLUGIN_TEM_DIR_NAME ="tem";
    private String mPluginDir;
    private Map<String, Plugin> mPluginResources = new HashMap<>();
    private OnPluginInstalledListener mOnPluginInstalledListener;
    private Resources mResources;
    private PluginInstaller() {
    }

    public static PluginInstaller getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("请调用PluginUtil.init进行初始化操作");
        }
        return INSTANCE;
    }

    public static void init(Application context) {
        if (INSTANCE == null) {
            INSTANCE = new PluginInstaller();
            INSTANCE.initInternel(context);
        }
    }

    public Resources getResources() {
        return mResources;
    }


    private void initInternel(Application context) {
        if (context == null) {
            return;
        }
        mResources = context.getResources();
        File pluginDir = context.getDir(PLUGIN_DIR_NAME, Context.MODE_PRIVATE);
        if (!pluginDir.exists()) {
            return;
        }
        mPluginDir = pluginDir.getAbsolutePath();
        refreshInstalledPlugins(context);
        initInstalledPlugins(context);
    }

    private void refreshInstalledPlugins(Application context) {
        File pluginDir = new File(mPluginDir);
        File temPluginDir = new File(pluginDir, PLUGIN_TEM_DIR_NAME);
        String[] temDir = temPluginDir.list();
        if (temDir != null) {
            for (String needInstall : temDir) {
                File desFile = new File(pluginDir, needInstall);
                desFile.delete();
                File temFile = new File(temPluginDir, needInstall);
                FileUtils.saveFileFromUri(context,Uri.fromFile(temFile),desFile.getAbsolutePath());
                temFile.delete();
            }
        }
    }

    /**
     * 初始化已经安装的插件
     */
    private void initInstalledPlugins(Application context) {
        try {
            File[] pluginFiles = new File(mPluginDir).listFiles();
            if (pluginFiles == null || pluginFiles.length == 0) {
                return;
            }
            Class<BaseDexClassLoader> baseDexClassLoaderClass = BaseDexClassLoader.class;
            Field pathList = baseDexClassLoaderClass.getDeclaredField("pathList");
            pathList.setAccessible(true);

            Field dexElements = pathList.getType().getDeclaredField("dexElements");
            dexElements.setAccessible(true);

            ClassLoader currentLoader = context.getClassLoader();
            Object pathListV_old = pathList.get(currentLoader);
            Object[] old = (Object[]) dexElements.get(pathListV_old);
            Class<?> componentType = old.getClass().getComponentType();

            ArrayList<Object[]> pluginDexElements = new ArrayList<Object[]>(pluginFiles.length);
            int pluginDexElementSum = 0;
            for (File file : pluginFiles) {
                if (file.isDirectory()) {
                    continue;
                }
                //加载静态资源
                if (!mergePluginResources(context, file)) {
                    continue;
                }
                cachePluginInfo(context, file);
                DexClassLoader classLoader_plugin = new DexClassLoader(file.getAbsolutePath(), context.getCacheDir().getAbsolutePath(),
                        null, currentLoader);
                Object pathListV_plugin = pathList.get(classLoader_plugin);
                Object[] plugin = (Object[]) dexElements.get(pathListV_plugin);
                if (plugin != null) {
                    pluginDexElements.add(plugin);
                    pluginDexElementSum += plugin.length;
                }
            }
            Object[] result = (Object[]) Array.newInstance(componentType, old.length + pluginDexElementSum);
            System.arraycopy(old, 0, result, 0, old.length);
            int pos = old.length;
            for (Object[] pluginDexElement : pluginDexElements) {
                System.arraycopy(pluginDexElement, 0, result, pos, pluginDexElement.length);
                pos += pluginDexElement.length;
            }
            dexElements.set(pathListV_old, result);
            for (String pluginId : mPluginResources.keySet()) {
                Plugin plugin = mPluginResources.get(pluginId);
                if (plugin != null) {
                    InitializerUtil.initModules(context, plugin.path);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 安装插件
     */
    public void installPlugin(Application context, String pluginFilePath) {
        File pluginFile = new File(pluginFilePath);
        Plugin pluginInfo = getPluginInfo(context, pluginFile);
        Plugin installedPlugin = mPluginResources.get(pluginInfo.id);
        if (installedPlugin != null) {
            File tem = new File(mPluginDir + File.separator + PLUGIN_TEM_DIR_NAME, pluginInfo.id.replace(".", "") + ".plugin");
            tem.mkdirs();
            //转移插件包
            FileUtils.saveFileFromUri(context, Uri.fromFile(pluginFile), tem.getAbsolutePath());
            if (mOnPluginInstalledListener != null) {
                mOnPluginInstalledListener.OnPluginInstalled(pluginInfo.id, true);
            }
            return;
        }
        File dest = new File(mPluginDir, pluginInfo.id.replace(".", "") + ".plugin");
        //转移插件包
        FileUtils.saveFileFromUri(context, Uri.fromFile(pluginFile), dest.getAbsolutePath());
        installPluginInternal(context, pluginFilePath);
        if (mOnPluginInstalledListener != null) {
            mOnPluginInstalledListener.OnPluginInstalled(pluginInfo.id, false);
        }
    }

    /**
     * 安装缓存目录中的插件
     */
    private void installPluginInternal(Application context, String path) {
        if (context == null) {
            return;
        }
        try {
            File pluginDir = context.getDir(PLUGIN_DIR_NAME, Context.MODE_PRIVATE);
            if (!pluginDir.exists()) {
                return;
            }
            Class<BaseDexClassLoader> baseDexClassLoaderClass = BaseDexClassLoader.class;
            Field pathList = baseDexClassLoaderClass.getDeclaredField("pathList");
            pathList.setAccessible(true);

            Field dexElements = pathList.getType().getDeclaredField("dexElements");
            dexElements.setAccessible(true);

            ClassLoader currentLoader = context.getClassLoader();
            Object pathListV_old = pathList.get(currentLoader);
            Object[] old = (Object[]) dexElements.get(pathListV_old);
            Class<?> componentType = old.getClass().getComponentType();

            ArrayList<Object[]> pluginDexElements = new ArrayList<Object[]>();
            int pluginDexElementSum = 0;
            File file = new File(path);
            if (file.isDirectory()) {
                throw new IllegalArgumentException("插件路径错误");
            }
            //加载静态资源
            if (!mergePluginResources(context, file)) {
                throw new IllegalStateException("加载插件资源错误");
            }
            cachePluginInfo(context, file);
            DexClassLoader classLoader_plugin = new DexClassLoader(file.getAbsolutePath(), context.getCacheDir().getAbsolutePath(),
                    null, currentLoader);
            Object pathListV_plugin = pathList.get(classLoader_plugin);
            Object[] plugin = (Object[]) dexElements.get(pathListV_plugin);
            if (plugin != null) {
                pluginDexElements.add(plugin);
                pluginDexElementSum += plugin.length;
            }
            Object[] result = (Object[]) Array.newInstance(componentType, old.length + pluginDexElementSum);
            int pos = 0;
            for (Object[] pluginDexElement : pluginDexElements) {
                System.arraycopy(pluginDexElement, 0, result, pos, pluginDexElement.length);
                pos += pluginDexElement.length;
            }
            System.arraycopy(old, 0, result, pos, old.length);
            dexElements.set(pathListV_old, result);
            InitializerUtil.initModules(context, file.getAbsolutePath());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 缓存插件信息
     *
     */
    private void cachePluginInfo(Application context, File file) {
        Plugin pluginInfo = getPluginInfo(context, file);
        mPluginResources.put(pluginInfo.id, pluginInfo);
    }

    /**
     * 缓存插件信息
     *
     * @param context
     * @param file
     */
    private Plugin getPluginInfo(Application context, File file) {
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),
                PackageManager.GET_ACTIVITIES);
        Plugin plugin = new Plugin();
        plugin.id = packageInfo.packageName;
        plugin.path = file.getAbsolutePath();
        plugin.version = packageInfo.getLongVersionCode();
        plugin.packageInfo = packageInfo;
        return plugin;
    }

    /**
     */
    private boolean mergePluginResources(Application application, File pluginFile) {
        try {
            if (mResources == null) {
                // 创建一个新的 AssetManager 对象
                AssetManager newAssetManagerObj = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                // 塞入原来宿主的资源
                addAssetPath.invoke(newAssetManagerObj, application.getBaseContext().getPackageResourcePath());
                // 塞入插件的资源
                addAssetPath.invoke(newAssetManagerObj, pluginFile.getAbsolutePath());

                // ----------------------------------------------

                // 创建一个新的 Resources 对象
                Resources newResourcesObj = new Resources(newAssetManagerObj,
                        application.getBaseContext().getResources().getDisplayMetrics(),
                        application.getBaseContext().getResources().getConfiguration());
                Logger.d("newResourcesObj", newResourcesObj.toString());
                // ----------------------------------------------

                // 获取 ContextImpl 中的 Resources 类型的 mResources 变量，并替换它的值为新的 Resources 对象
                Field resourcesField = application.getBaseContext().getClass().getDeclaredField("mResources");
                resourcesField.setAccessible(true);
                resourcesField.set(application.getBaseContext(), newResourcesObj);

                // ----------------------------------------------

                // 获取 ContextImpl 中的 LoadedApk 类型的 mPackageInfo 变量
                Field packageInfoField = application.getBaseContext().getClass().getDeclaredField("mPackageInfo");
                packageInfoField.setAccessible(true);
                Object packageInfoObj = packageInfoField.get(application.getBaseContext());

                // 获取 mPackageInfo 变量对象中类的 Resources 类型的 mResources 变量，，并替换它的值为新的 Resources 对象
                // 注意：这是最主要的需要替换的，如果不需要支持插件运行时更新，只留这一个就可以了
                Field resourcesField2 = packageInfoObj.getClass().getDeclaredField("mResources");
                resourcesField2.setAccessible(true);
                resourcesField2.set(packageInfoObj, newResourcesObj);

                // ----------------------------------------------

                // 获取 ContextImpl 中的 Resources.Theme 类型的 mTheme 变量，并至空它
                // 注意：清理mTheme对象，否则通过inflate方式加载资源会报错, 如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
                Field themeField = application.getBaseContext().getClass().getDeclaredField("mTheme");
                themeField.setAccessible(true);
                themeField.set(application.getBaseContext(), null);
                mResources = newResourcesObj;
            } else {
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                // 塞入插件的资源
                addAssetPath.invoke(mResources.getAssets(), pluginFile.getAbsolutePath());
            }
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getPluginDir() {
        return mPluginDir;
    }

    public void setOnPluginInstalledListener(OnPluginInstalledListener onPluginInstalledListener) {
        this.mOnPluginInstalledListener = onPluginInstalledListener;
    }

    public static class Plugin {
        public String id;
        public String path;
        public long version;
        public PackageInfo packageInfo;
        public Resources resources;
    }

    public interface OnPluginInstalledListener {
        void OnPluginInstalled(String pluginId, boolean isUpdate);
    }
}
