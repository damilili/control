package com.hoody.wificontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiUtil {
    private static final String TAG = "WifiUtil";

    /**
     * 获取本机WIFI设备详细信息
     */
    @SuppressLint("MissingPermission")
    public static void getDetailsWifiInfo(Context context) {
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
        sInfo.append("\n--SupplicantState : " + mWifiInfo.getSupplicantState() + mWifiInfo);
        sInfo.append("\n\n\n\n");
        Log.d("getDetailsWifiInfo", sInfo.toString());
    }

    /**
     * 获取附近wifi信号
     */
    public static List<String> getAroundWifiDeviceInfo(Context context) {
        StringBuffer sInfo = new StringBuffer();
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        List<ScanResult> scanResults = mWifiManager.getScanResults();//搜索到的设备列表
        List<ScanResult> newScanResultList = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            int position = getItemPosition(newScanResultList, scanResult);
            if (position != -1) {
                if (newScanResultList.get(position).level < scanResult.level) {
                    newScanResultList.remove(position);
                    newScanResultList.add(position, scanResult);
                }
            } else {
                newScanResultList.add(scanResult);
            }
        }
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < newScanResultList.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("设备名(SSID) ->" + newScanResultList.get(i).SSID + "\n");
            stringBuilder.append("信号强度 ->" + newScanResultList.get(i).level + "\n");
            stringBuilder.append("BSSID ->" + newScanResultList.get(i).BSSID + "\n");
            stringBuilder.append("level ->" + newScanResultList.get(i).level + "\n");
            stringBuilder.append("采集时间戳 ->" + System.currentTimeMillis() + "\n");
            stringBuilder.append("rssi ->" + (mWifiInfo != null && (mWifiInfo.getSSID().replace("\"", "")).equals(newScanResultList.get(i).SSID) ? mWifiInfo.getRssi() : null) + "\n");
            //是否为连接信号(1连接，默认为null)
            stringBuilder.append("是否为连接信号 ->" + (mWifiInfo != null && (mWifiInfo.getSSID().replace("\"", "")).equals(newScanResultList.get(i).SSID) ? 1 : null) + "\n");
            stringBuilder.append("信道 - >" + getCurrentChannel(mWifiManager) + "\n");
            //1 为2.4g 2 为5g
            stringBuilder.append("频段 ->" + is24GOr5GHz(newScanResultList.get(i).frequency));
            stringList.add(stringBuilder.toString());
        }
        Log.d("getAroundWifiDeviceInfo", sInfo.toString());
        return stringList;
    }


    private static String is24GOr5GHz(int freq) {
        if (freq > 2400 && freq < 2500) {
            return "1";
        } else if (freq > 4900 && freq < 5900) {
            return "2";
        } else {
            return "无法判断";
        }
    }

    /**
     * 返回item在list中的坐标
     */
    private static int getItemPosition(List<ScanResult> list, ScanResult item) {
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
    private static int getChannelByFrequency(int frequency) {
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

    /**
     * 获取路由器ip
     */
    public static String getApIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String IPAddress = intToIp(wifiInfo.getIpAddress());
        Log.e(TAG, "IPAddress-->>" + IPAddress);
        DhcpInfo dhcpinfo = wifiManager.getDhcpInfo();
        String serverAddress = intToIp(dhcpinfo.serverAddress);
        Log.e(TAG, "serverAddress-->>" + serverAddress);
        return serverAddress;
    }

    private static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }
}
