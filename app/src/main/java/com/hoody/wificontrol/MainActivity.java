package com.hoody.wificontrol;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow(); //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //设置状态栏颜色
        window.setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE}, 9);
        initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initView() {
        FragmentFactory fragmentFactory = new FragmentFactory();
        ViewPager controller_pager = findViewById(R.id.controller_pager);
        FragmentStatePagerAdapter fragmentStateAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 2;
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                Fragment instantiate = fragmentFactory.instantiate(getClassLoader(), "com.hoody.wificontrol.ControllerFragment");
                Bundle args = new Bundle();
                args.putInt("position", position);
                instantiate.setArguments(args);
                return instantiate;
            }
        };
        controller_pager.setAdapter(fragmentStateAdapter);
        getApIp();
        WifiUtil.getCurrentChannel()
    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    String getApIp() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Log.e("TAGTAG", "=================");
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String IPAddress = intToIp(wifiInfo.getIpAddress());
        Log.e("TAGTAG", "IPAddress-->>" + IPAddress);
        DhcpInfo dhcpinfo = wifiManager.getDhcpInfo();
        String serverAddress = intToIp(dhcpinfo.serverAddress);
        Log.e("TAGTAG", "serverAddress-->>" + serverAddress);
        return serverAddress;
    }
    public static class WifiUtil {

        private Context context;

        public WifiUtil(Context context) {
            this.context = context;
        }

        // TODO: 2021/9/15 获取本机WIFI设备详细信息
        @SuppressLint("MissingPermission")
        public void getDetailsWifiInfo() {
            StringBuffer sInfo = new StringBuffer();
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int Ip = mWifiInfo.getIpAddress();
            String strIp = "" + (Ip & 0xFF) + "." + ((Ip >> 8) & 0xFF) + "." + ((Ip >> 16) & 0xFF) + "." + ((Ip >> 24) & 0xFF);
            sInfo.append("\n--BSSID : " + mWifiInfo.getBSSID());
            sInfo.append("\n--SSID : " + mWifiInfo.getSSID());
            sInfo.append("\n--nIpAddress : " + strIp);
            sInfo.append("\n--MacAddress : " + mWifiInfo.getMacAddress());
            sInfo.append("\n--NetworkId : " + mWifiInfo.getNetworkId());
            sInfo.append("\n--LinkSpeed : " + mWifiInfo.getLinkSpeed() + "Mbps");
            sInfo.append("\n--Rssi : " + mWifiInfo.getRssi());
            sInfo.append("\n--SupplicantState : " + mWifiInfo.getSupplicantState()+mWifiInfo);
            sInfo.append("\n\n\n\n");
            Log.d("getDetailsWifiInfo", sInfo.toString());
        }

        // TODO: 2021/9/15 获取附近wifi信号
        public List<String> getAroundWifiDeviceInfo() {
            StringBuffer sInfo = new StringBuffer();
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            List<ScanResult> scanResults = mWifiManager.getScanResults();//搜索到的设备列表
            List<ScanResult> newScanResultList = new ArrayList<>();
            for (ScanResult scanResult : scanResults) {
                int position = getItemPosition(newScanResultList,scanResult);
                if (position != -1){
                    if (newScanResultList.get(position).level < scanResult.level){
                        newScanResultList.remove(position);
                        newScanResultList.add(position,scanResult);
                    }
                }else {
                    newScanResultList.add(scanResult);
                }
            }
            List<String> stringList = new ArrayList<>();
            for (int i = 0; i <newScanResultList.size() ; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("设备名(SSID) ->" + newScanResultList.get(i).SSID + "\n");
                stringBuilder.append("信号强度 ->" + newScanResultList.get(i).level + "\n");
                stringBuilder.append("BSSID ->" + newScanResultList.get(i).BSSID + "\n");
                stringBuilder.append("level ->" + newScanResultList.get(i).level + "\n");
                stringBuilder.append("采集时间戳 ->" +System.currentTimeMillis() + "\n");
                stringBuilder.append("rssi ->" + (mWifiInfo != null && (mWifiInfo.getSSID().replace("\"", "")).equals(newScanResultList.get(i).SSID) ? mWifiInfo.getRssi() : null) + "\n");
                //是否为连接信号(1连接，默认为null)
                stringBuilder.append("是否为连接信号 ->" + (mWifiInfo != null && (mWifiInfo.getSSID().replace("\"", "")).equals(newScanResultList.get(i).SSID) ? 1: null) + "\n");
                stringBuilder.append("信道 - >" +getCurrentChannel(mWifiManager) + "\n");
                //1 为2.4g 2 为5g
                stringBuilder.append("频段 ->" + is24GOr5GHz(newScanResultList.get(i).frequency));
                stringList.add(stringBuilder.toString());
            }
            Log.d("getAroundWifiDeviceInfo", sInfo.toString());
            return stringList;
        }


        public static String is24GOr5GHz(int freq) {
            if (freq > 2400 && freq < 2500){
                return "1";
            }else if (freq > 4900 && freq < 5900){
                return "2";
            }else {
                return "无法判断";
            }
        }

        /**
         * 返回item在list中的坐标
         */
        private int getItemPosition(List<ScanResult>list, ScanResult item) {
            for (int i = 0; i < list.size(); i++) {
                if (item.SSID.equals(list.get(i).SSID)) {
                    return i;
                }
            }
            return -1;
        }

        public static int getCurrentChannel(WifiManager wifiManager) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();// 当前wifi连接信息
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult result : scanResults) {
                if (result.BSSID.equalsIgnoreCase(wifiInfo.getBSSID())
                        && result.SSID.equalsIgnoreCase(wifiInfo.getSSID()
                        .substring(1, wifiInfo.getSSID().length() - 1))) {
                    return getChannelByFrequency(result.frequency);
                }
            }
            return -1;
        }

        /**
         * 根据频率获得信道
         *
         * @param frequency
         * @return
         */
        public static int getChannelByFrequency(int frequency) {
            int channel = -1;
            switch (frequency) {
                case 2412:
                    channel = 1;
                    break;
                case 2417:
                    channel = 2;
                    break;
                case 2422:
                    channel = 3;
                    break;
                case 2427:
                    channel = 4;
                    break;
                case 2432:
                    channel = 5;
                    break;
                case 2437:
                    channel = 6;
                    break;
                case 2442:
                    channel = 7;
                    break;
                case 2447:
                    channel = 8;
                    break;
                case 2452:
                    channel = 9;
                    break;
                case 2457:
                    channel = 10;
                    break;
                case 2462:
                    channel = 11;
                    break;
                case 2467:
                    channel = 12;
                    break;
                case 2472:
                    channel = 13;
                    break;
                case 2484:
                    channel = 14;
                    break;
                case 5745:
                    channel = 149;
                    break;
                case 5765:
                    channel = 153;
                    break;
                case 5785:
                    channel = 157;
                    break;
                case 5805:
                    channel = 161;
                    break;
                case 5825:
                    channel = 165;
                    break;
            }
            return channel;
        }
    }
}