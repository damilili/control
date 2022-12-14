package com.hoody.commonbase.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hoody.annotation.permission.Permissions;
import com.hoody.annotation.router.RouterUtil;
import com.hoody.commonbase.R;
import com.hoody.commonbase.message.MessageObserverRegister;
import com.hoody.commonbase.util.KeyboardAndNavigationBarUtil;
import com.hoody.commonbase.util.PermissionUtil;
import com.hoody.commonbase.view.fragment.CloseableFragment;

import java.util.List;

public abstract class ContentActivity extends AbstractContentActivity {
    private static final String TAG = "ContentActivity";
    private Intent mCurrentIntent;
    private String primaryFragment = "com.hoody.audience.view.fragment.LivePlayFragement";
    protected MessageObserverRegister mObserverRegister = new MessageObserverRegister();
    @Override
    protected void onCreateProxy(Bundle savedInstanceState) {
        setContentView(R.layout.activity_content);
        mCurrentIntent = getIntent();
        handleIntent();
        KeyboardAndNavigationBarUtil instance = KeyboardAndNavigationBarUtil.getInstance();
        instance.init(getWindow().getDecorView().findViewById(Window.ID_ANDROID_CONTENT));
        instance.setKeyBoardListener(new KeyboardAndNavigationBarUtil.KeyBoardListener() {
            @Override
            public void keyBoardShow(int keyBoardHeight) {
            }

            @Override
            public void navigationBarShow(int barHeight) {
            }

            @Override
            public void navigationBarHide(int barHeight) {
            }

            @Override
            public void keyBoardHide(int keyBoardHeight) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyboardAndNavigationBarUtil instance = KeyboardAndNavigationBarUtil.getInstance();
        instance.removeNavigationListener();
    }

    @Override
    protected int getFragmentViewId() {
        return R.id.content_detail;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent() called with: intent = [" + (intent == getIntent()) + "]");
        super.onNewIntent(intent);
        if (mCurrentIntent == null) {
            mCurrentIntent = intent;
            handleIntent();
        }
    }

    private void handleIntent() {
        Log.d(TAG, "handleIntent() called");
        if (mCurrentIntent != null) {
            String fragmentOpt = mCurrentIntent.getStringExtra(RouterUtil.STARTPARAM_STR_FRAGMENT_OPT);
            if (fragmentOpt != null) {
                switch (fragmentOpt) {
                    case RouterUtil.FRAGMENT_OPT_CLOSE:
                        closeContentFragment(mCurrentIntent);
                        mCurrentIntent = null;
                        break;
                    case RouterUtil.FRAGMENT_OPT_OPEN:
                        if (!showContentFragment(mCurrentIntent)) {
                            return;
                        }
                        mCurrentIntent = null;
                        break;
                }
            }
            Log.d(TAG, "Intent ??????????????????");
            mCurrentIntent = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mObserverRegister.unregistAll();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "requestCode = " + requestCode);
        Log.d(TAG, "permissions = " + permissions);
        Log.d(TAG, "grantResults = " + grantResults.length);
        if (grantResults.length > 0) {
            boolean hasAllPermission = true;
            for (int i = 0; i < permissions.length && hasAllPermission; i++) {
                hasAllPermission &= (grantResults[i] == PackageManager.PERMISSION_GRANTED);
            }
            Log.d(TAG, "hasAllPermission = " + hasAllPermission);
            if (hasAllPermission) {
                handleIntent();
            } else {
                if (getSupportFragmentManager().getFragments().size() == 0) {
                    finish();
                }
                mCurrentIntent = null;
            }
        }
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    private void closeContentFragment(Intent intent) {
        String fragmentClass = intent.getStringExtra(RouterUtil.STARTPARAM_STR_FRAGMENT_NAME);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.getClass().equals(fragmentClass)) {
                closeFragment(fragment.getTag());
                return;
            }
        }
    }

    /**
     * @param intent
     * @return ??????????????????
     */
    private boolean showContentFragment(Intent intent) {
        String showFragment = intent.getStringExtra(RouterUtil.STARTPARAM_STR_FRAGMENT_NAME);
        String errMsg = "";
        int errCode = 0;
        try {
            Class<CloseableFragment> fragmentClass = (Class<CloseableFragment>) Class.forName(showFragment);
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Bundle extras = intent.getExtras();
            extras.remove(RouterUtil.STARTPARAM_STR_FRAGMENT_NAME);
            for (Fragment fragment : fragments) {
                if (fragment.getClass().equals(fragmentClass)) {
                    if (fragment.getClass().getName().equals(primaryFragment)) {
                        fragment.setArguments(extras);
                        navigate2Fragment(fragment.getTag());
                        return true;
                    } else {
                        CloseableFragment newInstance = fragmentClass.newInstance();
                        newInstance.setArguments(extras);
                        showFragment(newInstance);
                        //????????????????????????????????????????????????
                        closeFragment(fragment.getTag());
                        return true;
                    }
//                    navigate2Fragment(fragment.getTag());
                }
            }
            if (!requestPermission(fragmentClass)) {
                errMsg = "????????????????????????,???????????????";
                return false;
            }
            CloseableFragment fragment = fragmentClass.newInstance();
            fragment.setArguments(extras);
            showFragment(fragment);
        } catch (ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            errCode = -1;
            Log.d(TAG, "????????? " + showFragment + " ????????????");
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            errCode = -1;
            Log.d(TAG, showFragment + " ????????????????????????????????????");
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            errCode = -1;
            Log.d(TAG, showFragment + " ?????????????????????????????????");
            return false;
        } catch (ClassCastException e) {
            e.printStackTrace();
            errCode = -1;
            Log.d(TAG, "Start_Param_Fragment ???????????? com.hoody.commonbase.view.fragment.CloseableFragment ?????????");
            return false;
        } finally {
            if (errCode < 0) {
                if (!TextUtils.isEmpty(errMsg)) {
                    Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
                }
                if (getSupportFragmentManager().getFragments().size() == 0) {
                    finish();
                }
            } else {
                if (!TextUtils.isEmpty(errMsg)) {
                    Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    private boolean requestPermission(Class<? extends CloseableFragment> aClass) {
        Permissions permissions = aClass.getAnnotation(Permissions.class);
        if (permissions != null) {
            String[] permissionsNeeded = permissions.value();
            if (permissionsNeeded.length > 0) {
                if (!PermissionUtil.checkPermissionAndRequest(ContentActivity.this, aClass, permissionsNeeded)) {
                    //?????????????????????
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected final void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IActivityResultHelper.processorsgResult(this,requestCode, resultCode, data);
    }
}
