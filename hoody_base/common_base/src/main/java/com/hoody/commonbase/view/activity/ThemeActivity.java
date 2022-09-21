package com.hoody.commonbase.view.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.hoody.commonbase.plugin.PluginInstaller;

public class ThemeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    @Override
    public Resources getResources() {
        Resources flatResources = PluginInstaller.getInstance().getResources();
        if (flatResources != null) {
            return flatResources;
        }
        return super.getResources();
    }

}
