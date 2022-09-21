package com.hoody.commonbase.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import static android.view.View.NO_ID;

/**
 * 设备信息
 */
public final class DeviceInfo {
    //Android ID
    private static String ANDROID_ID;
    // Android ID 36进制
    private static String ANDROID_ID36;
    // 手机IMEI号
    public static String DEVICE_ID;
    // 手机IMEI号 36进制
    public static String DEVICE_ID36;

    // 以字节为单位
    public static long TOTAL_MEMORY;

    // 以M为单位
    public static long TOTAL_MEMORY_MB;

    // CPU频率
    public static long CPU_MAX_FREQ;

    // 屏幕宽度（像素）WelcomeActivity onResume之后才有值
    public static int WIDTH;

    // 屏幕高度（像素）WelcomeActivity onResume之后才有值
    public static int HEIGHT;
    /**
     * 状态栏高度（像素）首页显示后才有值
     */
    public static int STATUSBAR_HEIGHT;

    // 屏幕密度（0.75 / 1.0 / 1.5）WelcomeActivity onResume之后才有值
    public static float DENSITY;

    // 屏幕密度DPI（120 / 160 / 240）WelcomeActivity onResume之后才有值
    public static int DENSITY_DPI;

    public static float SCALED_DENSITY;

    public static boolean isInit;
    public static boolean isInitScreenInfo;
    /**
     * 是否连接了耳机，true：连接，false：未连接
     */
    public static boolean isHasHeadset;

    private static boolean IS_ADUIO_PAY; //是否是电台开启的充值

    private static String MODEL;//机型
    private static String HARDWARE;//手机平台（CPU类型）
    private static int barHeight;

    // 小米的MIUI？
    public static final boolean IS_MIUI = false;

    private static int isFlyme4Above = -1;//-1代表未知 ,0 false 1 true
    private static int isFlyme6Above = -1;
    private static int isAndroidMOrAbove = -1;
    // 小米的MIUI系统版本
    private static int mMiuiVersion = -1;
    private static int mRomType = -1;
    //机器型号
    public static final int UNKNOWN = 0;

    public static final int VIVO = 1;

    public static final int OPPO = 2;

    public static final int XIAOMI = 3;

    public static final int MEIZU = 4;

    public static final int SAMSUNG = 5;

    public static final int HUAWEI = 6;

    public static boolean isAduioPay() {
        return IS_ADUIO_PAY;
    }

    public static void setIsAduioPay(boolean isAduioPay) {
        IS_ADUIO_PAY = isAduioPay;
    }

    static void init(Context context) {
        if (isInit) {
            return;
        }
        TOTAL_MEMORY = getTotalMemory();
        TOTAL_MEMORY_MB = (int) (TOTAL_MEMORY * 1.0 / 1024 / 1024);
        CPU_MAX_FREQ = getMaxCpuFreq();
        isInit = true;
        initScreenInfo(context);
        STATUSBAR_HEIGHT = getStatusBarHeight(context);
    }

    private static void initScreenInfo(Context context) {
        if (isInitScreenInfo) {
            return;
        }

        try {
            DisplayMetrics dm = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
            } else {
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
            }
            WIDTH = Math.min(dm.widthPixels, dm.heightPixels);
            HEIGHT = Math.max(dm.widthPixels, dm.heightPixels);
            DENSITY = dm.density;
            DENSITY_DPI = dm.densityDpi;
            SCALED_DENSITY = dm.scaledDensity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        isInitScreenInfo = true;
    }

    public static String DeviceId() {
        return DEVICE_ID;
    }

    public static String DeviceId36() {
        return DEVICE_ID36;
    }

    public static long TotalMemory() {
        return TOTAL_MEMORY;
    }

    public static long TotalMemoryMB() {
        return TOTAL_MEMORY_MB;
    }

    public static int ScreenWidth() {
        return WIDTH;
    }

    public static int ScreenHeight() {
        return HEIGHT;
    }

    public static float ScreenDensity() {
        return DENSITY;
    }

    public static int ScreenDensityDPI() {
        return DENSITY_DPI;
    }

    public static float ScreenScaledDensity() {
        return SCALED_DENSITY;
    }

    public static long CpuMaxFreq() {
        return CPU_MAX_FREQ;
    }

    public static int CpuCoreNum() {// 获取cpu核心数
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }

        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }


    public static String CpuCoreNumReadableText()// 转换核心数
    {
        int coreNum = CpuCoreNum();
        String result = "";
        if (coreNum == 1) {
            result = "单核";
        } else if (coreNum == 2) {
            result = "双核";
        } else if (coreNum == 4) {
            result = "四核";
        } else {
            result = "你手机为劣质手机,无法检测!";
        }

        return result;
    }

    public static String getDeviceId(Context context) {
        String deviceId = "";

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {//手机系统版本大于28后，无法再获取getDeviceId
            deviceId = "";
        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            deviceId = ((TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        DEVICE_ID = deviceId;
        DEVICE_ID36 = DeviceIDConvert36(deviceId);
        return deviceId;
    }

    //获取一个ANDROID_ID用来和之前的设备ID做对应
    public static String getAndroidId() {
        if (!TextUtils.isEmpty(ANDROID_ID)) {
            return ANDROID_ID;
        }
        ANDROID_ID = randDeviceId();
        return ANDROID_ID;
    }

    //获取一个ANDROID_ID用来和之前的设备ID做对应
    public static String getAndroidId36() {
        if (!TextUtils.isEmpty(ANDROID_ID36)) {
            return ANDROID_ID36;
        }
        if (TextUtils.isEmpty(ANDROID_ID)) {
            getAndroidId();
        }
        ANDROID_ID36 = Convert16_36(ANDROID_ID);
        return ANDROID_ID36;
    }


    private static String randDeviceId() {
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        int i = 0;
        i = rand.nextInt(5);
        i = i == 0 ? 1 : i;
        i *= 10000;
        i = rand.nextInt(i) + i;
        sb.append(i);

        i = rand.nextInt(5) + 5;
        i *= 100000;
        i = rand.nextInt(i) + i;
        sb.append(i);
        return sb.toString();
    }

    private static SharedPreferences getConfig(Context ctx) {
        return ctx.getSharedPreferences("deviceinfo", Context.MODE_PRIVATE);
    }

    private static long getTotalMemory() {
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileInputStream fileInputStream = new FileInputStream("/proc/meminfo");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader localBufferedReader = new BufferedReader(
                    inputStreamReader, 1024);
            try {
                String firstLine = localBufferedReader.readLine();
                if (firstLine == null) return initial_memory;
                arrayOfString = firstLine.split("\\s+");
                initial_memory = (long) (Integer.parseInt(arrayOfString[1])) * 1024;
            } finally {
                localBufferedReader.close();
            }
        } catch (Throwable e) {
        }
        return initial_memory;
    }

    public static int getMaxCpuFreq() {// 获取cpu最大频率, GHZ为单位
//        String result = "";
//        ProcessBuilder cmd;
//        try {
//            String[] args = {"/system/bin/cat",
//                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
//            cmd = new ProcessBuilder(args);
//            Process process = cmd.start();
//            InputStream is = process.getInputStream();
//            byte[] te = new byte[24];
//            while (is.read(te) != -1) {
//                result += StringUtils.byteToStr(te);
//            }
//            float core = Float.valueOf(result);
//            core = core / 1000 / 1000;  //把khz转为GHz
//            result = (int) core + "";
//            is.close();
//        } catch (Error e) {
//            result = "1";
//        } catch (Exception e) {
//            result = "1";
//        }
//        try {
//            return Integer.parseInt(result);
//        } catch (Exception e) {
//            return 1;
//        }
        return 1;
    }
   /* public static String GetHostIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        } catch (Exception e) {
        }
        return null;
    }
*/


    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @returnMemoryUtils
     */
    public static String getLocalIpAddress(Context context) {
        try {
            NetworkInfo info = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ipAddress = int2ip(wifiInfo.getIpAddress());//得到IPV4地址
                    return ipAddress;
                }
            } else {
                //当前无网络连接,请在设置中打开网络
                return "";
            }
        } catch (Exception ex) {
//            return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
            return "";
        }
        return "";

    }

    public static void checkHeadset(Context context) {
        AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (localAudioManager.isWiredHeadsetOn() || localAudioManager.isBluetoothA2dpOn()
                || localAudioManager.isBluetoothScoOn()) {
            isHasHeadset = true;
        } else {
            isHasHeadset = false;
        }
    }

    /**
     * 获取手机型号
     */
    public static String getModel() {
        if (TextUtils.isEmpty(MODEL)) {
            MODEL = Build.MODEL;
        }
        return MODEL;
    }


    /**
     * 获取CPU类型
     *
     * @return
     */
    public static String getHardware() {
        if (TextUtils.isEmpty(HARDWARE)) {
            HARDWARE = Build.HARDWARE;
        }
        return HARDWARE;
    }

    /**
     * 系统是否处于低内存运行
     */
    public static boolean displayLowMemory(Context c) {
        ActivityManager activityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);

        return info.lowMemory;

    }

    /**
     * 获取状态栏高度（像素）首页显示后才有值
     */
    public static int getStatusbarHeight() {
        return STATUSBAR_HEIGHT;
    }

    /**
     * 转36进制
     *
     * @param id BigInteger类型
     * @return
     */
    private static String DeviceIDConvert36(String id) {
        String result = id;
        try {
            BigInteger bigInteger = new BigInteger(id);
            result = bigInteger.toString(36);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 16转36进制
     *
     * @param id BigInteger类型
     * @return
     */
    private static String Convert16_36(String id) {
        String result = id;
        try {
            BigInteger bigInteger = new BigInteger(id, 16);
            result = bigInteger.toString(36);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取底部导航栏高度
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        if (barHeight > 0) {
            return barHeight;
        } else {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            barHeight = resources.getDimensionPixelSize(resourceId);
            return barHeight;
        }
    }

    private static final String NAVIGATION = "navigationBarBackground";

    /**
     * 检查虚拟键盘是否存在的方法
     * <p>
     * 备注：该方法需要在View完全被绘制出来之后才能正确判断
     *
     * @param activity
     * @return
     */
    public static boolean isNavigationBarExist(@NonNull Activity activity) {
        ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
        if (vp != null) {
            for (int i = 0; i < vp.getChildCount(); i++) {
                vp.getChildAt(i).getContext().getPackageName();
                if (vp.getChildAt(i).getId() != NO_ID && NAVIGATION.equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getBuildProperty(String key, String defaultValue) {
        String val = defaultValue;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val = getSystemProperty(key, defaultValue);  //使用反射FileNotFoundException: /system/build.prop (Permission denied)处理异常情况
        } else {
            Properties property = new Properties();
            try {
                property.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                val = property.getProperty(key, defaultValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return val;
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    /**
     * 判断手机型号
     *
     * @return
     */
    public static int getRomType(Context context) {
        if (mRomType != -1) {
            return mRomType;
        }
        //先判断桌面情况
        int type = checkIsHuaWei(context);
        if (type == HUAWEI) {
            mRomType = HUAWEI;
            return HUAWEI;
        }
        //然后判断rom情况
        Properties property = new Properties();
        try {
            property.load(new FileInputStream(new File(Environment
                    .getRootDirectory(), "build.prop")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String xiaomiRomKey = property.getProperty("ro.miui.ui.version.name");
        if (xiaomiRomKey != null) {
            mRomType = XIAOMI;
            return XIAOMI;
        }
        String vivoRomKey1 = property.getProperty("ro.vivo.rom");
        String vivoRomKey2 = property.getProperty("ro.vivo.rom.version");
        if (vivoRomKey1 != null || vivoRomKey2 != null) {
            mRomType = VIVO;
            return VIVO;
        }
        String oppoRomKey = property.getProperty("ro.build.version.opporom");
        if (oppoRomKey != null) {
            mRomType = OPPO;
            return OPPO;
        }
        if (Build.DISPLAY.toLowerCase(Locale.getDefault()).contains("flyme")) {
            mRomType = MEIZU;
            return MEIZU;
        }
        //最后判断机器情况
        String brand = Build.BRAND.toLowerCase(Locale.getDefault());
        if (!TextUtils.isEmpty(brand)) {
            if (brand.contains("meizu")) {
                mRomType = MEIZU;
                return MEIZU;
            } else if (brand.contains("huawei") || brand.contains("honor")) {
                mRomType = HUAWEI;
                return HUAWEI;
            } else if (brand.contains("xiaomi")) {
                mRomType = XIAOMI;
                return XIAOMI;
            } else if (brand.contains("sam")) {
                mRomType = SAMSUNG;
                return SAMSUNG;
            } else if (brand.contains("vivo")) {
                mRomType = VIVO;
                return VIVO;
            } else if (brand.contains("oppo")) {
                mRomType = OPPO;
                return OPPO;
            }
        }
        mRomType = UNKNOWN;
        return UNKNOWN;
    }

    /**
     * 判断是否是华为桌面
     *
     * @return
     */
    private static int checkIsHuaWei(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo("com.huawei.android.launcher", 0);

            if (info.versionCode > 0) {
                return HUAWEI;
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean isAndroidO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 是否可以处理指定的intent
     */
    public static boolean isExtraOutput(Context context, Intent intent) {
        ResolveInfo reInfo = context.getPackageManager()
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (reInfo == null) {
            return false;
        }
        return true;
    }

    public static float dp2px(Context context, float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }
}
