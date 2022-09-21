package com.hoody.commonbase.view.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.hoody.commonbase.view.fragment.CloseableFragment;
import com.hoody.commonbase.view.fragment.NoFullScreenFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractContentActivity extends ViewControllerActivity implements IFragmentControl {
    private static final String TAG = "AbstractContentActivity";

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        translucentActivity();
        super.onCreate(savedInstanceState);
        onCreateProxy(savedInstanceState);
    }

    protected abstract void onCreateProxy(Bundle savedInstanceState);

    @Override
    final public void showFragment(CloseableFragment closeableFragment) {
        if (closeableFragment == null) {
            return;
        }
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        String tag = closeableFragment.getClass().getName() + System.currentTimeMillis();
        fragmentTransaction.add(getFragmentViewId(), closeableFragment, tag);
        fragmentTransaction.show(closeableFragment);
        fragmentTransaction.commitNowAllowingStateLoss();
        List<Fragment> fragments = supportFragmentManager.getFragments();
        if (fragments.size() > 1) {
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (!(currentFragment instanceof NoFullScreenFragment)) {
                Fragment fragment = fragments.get(fragments.size() - 2);
                View view = fragment.getView();
                if (view != null) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    protected abstract int getFragmentViewId();

    @Override
    final public void closeFragment(String fragmentTag) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = supportFragmentManager.getFragments();
        Fragment fragmentByTag = null;
        for (int pos = fragments.size() - 1; pos >= 0; pos--) {
            Fragment fragment = fragments.get(pos);
            if (TextUtils.equals(fragment.getTag(), fragmentTag)) {
                fragmentByTag = fragment;
                if (pos > 0) {
                    Fragment nextFragment = fragments.get(pos - 1);
                    View view = nextFragment.getView();
                    if (view != null) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        if (fragmentByTag == null) {
            return;
        }
        if (fragmentByTag instanceof CloseableFragment) {
            try {
                Method onClose = CloseableFragment.class.getDeclaredMethod("onClose");
                onClose.setAccessible(true);
                onClose.invoke(fragmentByTag);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.remove(fragmentByTag);
        fragmentTransaction.commitNowAllowingStateLoss();
        fragments = supportFragmentManager.getFragments();
        if (fragments.size() == 0) {
            finish();
        }
    }

    @Override
    public CloseableFragment getTopFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = supportFragmentManager.getFragments();
        if (fragments.size() > 0) {
            return (CloseableFragment) fragments.get(fragments.size() - 1);
        }
        return null;
    }

    @Override
    final public void navigate2Fragment(String fragmentTag) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = supportFragmentManager.getFragments();
        for (int pos = fragments.size() - 1; pos >= 0; pos--) {
            Fragment fragment = fragments.get(pos);
            if (!TextUtils.equals(fragment.getTag(), fragmentTag)) {
                closeFragment(fragment.getTag());
            } else {
                break;
            }
        }
    }

    @Override
    final public void showPreFragmentView(String fragmentTag) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = supportFragmentManager.getFragments();
        for (int pos = fragments.size() - 1; pos >= 0; pos--) {
            Fragment fragment = fragments.get(pos);
            if (TextUtils.equals(fragment.getTag(), fragmentTag)) {
                if (pos > 0) {
                    Fragment nextFragment = fragments.get(pos - 1);
                    View view = nextFragment.getView();
                    if (view != null) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    final public void hidePreFragmentView(String fragmentTag) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = supportFragmentManager.getFragments();
        for (int pos = fragments.size() - 1; pos >= 0; pos--) {
            Fragment fragment = fragments.get(pos);
            if (TextUtils.equals(fragment.getTag(), fragmentTag)) {
                if (pos > 0) {
                    Fragment nextFragment = fragments.get(pos - 1);
                    View view = nextFragment.getView();
                    if (view != null) {
                        view.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    private void translucentActivity() {
        try {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));
            Method activityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
            activityOptions.setAccessible(true);
            Object options = activityOptions.invoke(this);

            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> aClass = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    aClass = clazz;
                }
            }
            Method method = Activity.class.getDeclaredMethod("convertToTranslucent",
                    aClass, ActivityOptions.class);
            method.setAccessible(true);
            method.invoke(this, null, options);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CloseableFragment topFragment = getTopFragment();
            if (topFragment != null) {
                if (topFragment.onKeyDown(keyCode, event)) {
                    return true;
                } else {
                    closeFragment(topFragment.getTag());
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }




}