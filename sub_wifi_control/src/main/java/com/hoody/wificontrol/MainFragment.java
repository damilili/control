package com.hoody.wificontrol;

import android.Manifest;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hoody.annotation.permission.Permissions;
import com.hoody.annotation.router.Router;
import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.view.fragment.SwipeBackFragment;

@Router("wificontrol/main")
@Permissions({Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE})
public class MainFragment extends SwipeBackFragment {
    private static final String TAG = "MainFragment";

    @Override
    protected void onClose() {

    }

    @Override
    protected View createContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_main, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        String apIp = WifiUtil.getApIp(getContext());
        Logger.i(TAG, "apip = " + apIp);
    }

    private void initView() {
        FragmentFactory fragmentFactory = new FragmentFactory();
        ViewPager controller_pager = (ViewPager) findViewById(R.id.controller_pager);
        FragmentStatePagerAdapter fragmentStateAdapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public int getCount() {
                return 2;
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                Fragment instantiate = fragmentFactory.instantiate(getClass().getClassLoader(), "com.hoody.wificontrol.ControllerFragment");
                Bundle args = new Bundle();
                args.putInt("position", position);
                instantiate.setArguments(args);
                return instantiate;
            }
        };
        controller_pager.setAdapter(fragmentStateAdapter);
    }
}
