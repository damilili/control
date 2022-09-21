package com.hoody.commonbase.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;


/**
 * 监听底部虚拟键盘变化的类
 */

public class KeyboardAndNavigationBarUtil {
    private static final String TAG = "NavigationStateListener";
    private WeakReference<View> reference;
    private int lastVisibleHeight;  //根视图的显示高度
    private int navigationBarHeight;
    private int keyBoardHeight;
    public static final String KEY_KEYBOARD_HEIGHT = "keyboardheight";

    private MyOnGlobalLayoutListener myOnGlobalLayoutListener;

    private static class SingletonHolder {
        private static final KeyboardAndNavigationBarUtil INSTANCE = new KeyboardAndNavigationBarUtil();
    }

    private KeyboardAndNavigationBarUtil() {
    }

    public static final KeyboardAndNavigationBarUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(View view) {
        this.navigationBarHeight = getNavigationBarHeight(view.getContext());
        reference = new WeakReference<>(view);
        myOnGlobalLayoutListener = new MyOnGlobalLayoutListener();
        view.getViewTreeObserver().addOnGlobalLayoutListener(myOnGlobalLayoutListener);
    }

    /**
     * 移除底部导航栏状态监听
     */
    public void removeNavigationListener() {
        View rootView = null;
        if (reference != null) {
            rootView = reference.get();
        }
        if (null != myOnGlobalLayoutListener) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                if (null != rootView) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(myOnGlobalLayoutListener);
                }
            } else {
                if (null != rootView) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(myOnGlobalLayoutListener);
                }
            }
        }
    }

    private class MyOnGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            //获取当前根视图在屏幕上显示的大小
            Rect r = new Rect();
            View rootView = null;
            if (reference != null) {
                rootView = reference.get();
            }
            getRootViewHeight();
            if (rootView == null) {
                return;
            }
            rootView.getWindowVisibleDisplayFrame(r);
            final int visibleHeight = r.height();
            if (lastVisibleHeight == 0) {
                lastVisibleHeight = visibleHeight;
                return;
            }
            //根视图显示高度没有变化，可以看作虚拟键盘盘显示／隐藏状态没有改变
            if (lastVisibleHeight == visibleHeight) {
                return;
            }

            // 可视区域变大
            if (visibleHeight > lastVisibleHeight) {
                if (visibleHeight - lastVisibleHeight == navigationBarHeight) {
                    //底部虚拟导航栏隐藏
                    if (keyBoardListener != null) {
                        keyBoardListener.navigationBarHide(Math.abs(lastVisibleHeight - visibleHeight));
                    }
                } else {
                    if (visibleHeight >= getRootViewHeight()) {
                        if (keyBoardListener != null) {
                            keyBoardListener.keyBoardHide(0);
                        }
                    } else {
                        keyBoardHeight = getRootViewHeight() - visibleHeight;
                        if (keyBoardHeight <= 5) {
                            //键盘收起时，部分三星手机这个值算出来不是0，所以这个地方做了这个处理
                            keyBoardHeight = 0;
                        }
                        setKeyboardHeight(keyBoardHeight);
                        if (keyBoardListener != null) {
                            keyBoardListener.keyBoardShow(keyBoardHeight);
                        }
                    }
                }
            } else {
                if (lastVisibleHeight - visibleHeight == navigationBarHeight) {
                    //底部虚拟导航栏显示
                    if (keyBoardListener != null) {
                        keyBoardListener.navigationBarShow(Math.abs(lastVisibleHeight - visibleHeight));
                    }
                } else if (lastVisibleHeight - visibleHeight != navigationBarHeight) {
                    keyBoardHeight = getRootViewHeight() - visibleHeight;
                    if (keyBoardHeight <= 5) {
                        //键盘收起时，部分三星手机这个值算出来不是0，所以这个地方做了这个处理
                        keyBoardHeight = 0;
                    }
                    setKeyboardHeight(keyBoardHeight);
                    if (keyBoardListener != null) {
                        keyBoardListener.keyBoardShow(keyBoardHeight);
                    }
                }
            }
            lastVisibleHeight = visibleHeight;
        }
    }

    private int getRootViewHeight() {
        View rootView = null;
        if (reference != null) {
            rootView = reference.get();
        }
        if (rootView == null) {
            return 0;
        }
        int height = rootView.getHeight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            height = height - getStatusBarHeight(rootView.getContext());
        }
        return height;
    }

    public int getKeyBoardHeight() {
        SharedPreferences sp = getConf();
        if (sp == null) {
            return 0;
        }
        return sp.getInt(KEY_KEYBOARD_HEIGHT, 0);
    }

    private void setKeyboardHeight(int height) {
        if (height <= 0) {
            return;
        }
        SharedPreferences sp = getConf();
        if (sp == null) {
            return;
        }
        sp.edit().putInt(KEY_KEYBOARD_HEIGHT, height).apply();
    }

    private SharedPreferences getConf() {
        Context context = null;
        if (reference != null) {
            View view = reference.get();
            context = view.getContext();
        }
        if (context == null) {
            return null;
        }
        return context.getSharedPreferences("keyboard", Context.MODE_PRIVATE);
    }

    public void setKeyBoardListener(KeyBoardListener keyBoardListener) {
        this.keyBoardListener = keyBoardListener;
    }

    /**
     * 强行关闭软键盘
     */
    public static boolean hideKeyboard(View windowView) {
        if (windowView == null) {
            return false;
        }
        Context context = windowView.getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(windowView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static boolean showKeyboard(final View windowView) {
        if (windowView == null) {
            return false;
        }
        if (!windowView.isFocused()) {
            windowView.requestFocus();
        }
        Context context = windowView.getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.showSoftInput(windowView, InputMethodManager.SHOW_IMPLICIT);
    }
    /**
     * 获取底部导航栏高度
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取顶部状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    private KeyBoardListener keyBoardListener;
    public interface KeyBoardListener {
        void keyBoardShow(int keyBoardHeight);

        void navigationBarShow(int barHeight);
        void navigationBarHide(int barHeight);
        void keyBoardHide(int keyBoardHeight);
    }

}
