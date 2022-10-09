package com.hoody.wificontrol;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hoody.annotation.router.Router;
import com.hoody.annotation.router.RouterHelper;
import com.hoody.annotation.router.RouterUtil;
import com.hoody.commonbase.util.ToastUtil;

@Router("config/test")
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow(); //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //设置状态栏颜色
        window.setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.IS_APPLICATION) {

        }
        findViewById(R.id.buttonPanel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RouterUtil.getInstance().navigateTo(MainActivity.this,"wificontrol/wifi",null);
            }
        });

    }
}