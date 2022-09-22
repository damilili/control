package com.hoody.wificontrol.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.hoody.wificontrol.R;
import com.hoody.wificontrol.WifiUtil;

import java.util.List;

public class WifiListPopupWindow extends PopupWindow {
    private static final String TAG = "WifiListPopupWindow";
    private ListView mWifiList;
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public WifiListPopupWindow(Context context) {
        View popView = View.inflate(context, R.layout.pop_wifi_list, null);
        mWifiList = popView.findViewById(R.id.ls_wifi);
        mWifiList.setAdapter(new WifiListAdapter());
        mWifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mAroundWifiDeviceInfo.get(position));
                }
            }
        });
        setContentView(popView);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        popView.getContext().registerReceiver(mReceiver, filter);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mReceiver != null) {
                    getContentView().getContext().unregisterReceiver(mReceiver);
                    mReceiver = null;
                }
            }
        });
        setOutsideTouchable(true);
    }

    private List<ScanResult> mAroundWifiDeviceInfo;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                mAroundWifiDeviceInfo = WifiUtil.getAroundWifiDeviceInfo(context);
                if (mAroundWifiDeviceInfo == null || mAroundWifiDeviceInfo.size() == 0) {
                    ((TextView) getContentView().findViewById(R.id.tv_loading)).setText("没有可用的wifi网络");
                } else {
                    getContentView().findViewById(R.id.tv_loading).setVisibility(View.GONE);
                }
                ListAdapter adapter = mWifiList.getAdapter();
                if (adapter instanceof WifiListAdapter) {
                    ((WifiListAdapter) adapter).notifyDataSetChanged();
                }
            }
        }
    };

    class WifiListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mAroundWifiDeviceInfo != null) {
                return mAroundWifiDeviceInfo.size();
            }
            return 0;
        }

        @Override
        public ScanResult getItem(int position) {
            if (mAroundWifiDeviceInfo != null) {
                return mAroundWifiDeviceInfo.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.item_wifi, null);
            }
            ((TextView) convertView.findViewById(R.id.tv_wifi_name)).setText(getItem(position).level + ":" + getItem(position).SSID);
            return convertView;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ScanResult scanResult);
    }
}
