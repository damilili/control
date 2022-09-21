package com.hoody.annotation.viewcontroller;

import android.content.Context;
import android.view.View;


import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 视图控制器基类
 * Created by cdm on 2020/4/30.
 */
@ViewController
public abstract class AbsViewController {
    private boolean mJoinedControllerManager = true;
    protected View mBaseView;
    protected Context mContext;
    private boolean mReleased = false;

    /**
     * @param baseView 建议传入直播间根布局
     */
    protected AbsViewController(View baseView) {
        if (baseView == null) {
            throw new NullPointerException("baseView 不能传 null");
        }
        baseView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mJoinedControllerManager) {
                    throw new RuntimeException("请通过正确的途径创建ViewController实例，相关ViewController子类：" + AbsViewController.this.getClass().getName());
                }
            }
        }, 300);
        mBaseView = baseView;
        mContext = baseView.getContext();
    }

    protected abstract void init();


    public boolean isReleased() {
        return mReleased;
    }

    public final void release() {
        if (mReleased) {
            return;
        }
        onRelease();
        mReleased = true;
        mContext = null;
        mBaseView = null;
    }

    protected abstract void onRelease();

    /**
     * ViewController管理类的实现
     */
    public static class ViewControllerManagerImpl {
        protected Map<String, AbsViewController> mViewControllerCache = new LinkedHashMap<>();

        /**
         * 缓存ViewController
         */
        public void cacheViewController(AbsViewController viewController) {
            if (viewController != null) {
                viewController.init();
                mViewControllerCache.put(viewController.getClass().getName(), viewController);
                viewController.mJoinedControllerManager = true;
            }
        }

        public void releaseViewController(AbsViewController viewController) {
            if (viewController != null) {
                if (!viewController.isReleased()) {
                    viewController.release();
                }
                mViewControllerCache.remove(viewController.getClass().getName());
            }
        }

        /**
         * 获取指定的ViewController
         */
        public <T extends AbsViewController> T getViewController(Class<T> viewControllerClass) {
            if (viewControllerClass != null) {
                String viewControllerName = viewControllerClass.getName();
                AbsViewController viewController = mViewControllerCache.get(viewControllerName);
                return viewController == null ? null : (T) viewController;
            }
            return null;
        }

        /**
         * 释放所有还没有释放的ViewController
         * ViewController可以在这之前调release()，这里做了是否已经释放的判断
         */
        public void releaseAllViewController() {
            for (AbsViewController viewController : mViewControllerCache.values()) {
                if (viewController != null) {
                    if (!viewController.isReleased()) {
                        viewController.release();
                    }
                }
            }
            mViewControllerCache.clear();
        }
    }

}
