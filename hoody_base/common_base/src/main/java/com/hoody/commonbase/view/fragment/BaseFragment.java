package com.hoody.commonbase.view.fragment;

import android.view.KeyEvent;
import android.view.View;

import com.hoody.annotation.viewcontroller.AbsViewController;
import com.hoody.commonbase.message.MessageObserverRegister;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    protected final View findViewById(int viewId) {
        return getView().findViewById(viewId);
    }
    protected final MessageObserverRegister mObserverRegister=new MessageObserverRegister();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewControllerCacher.releaseAllViewController();
        mObserverRegister.unregistAll();
    }

    /**
     * ViewController存储器
     */
    private final AbsViewController.ViewControllerManagerImpl mViewControllerCacher = new AbsViewController.ViewControllerManagerImpl();

    /**
     * 有可能返回null,在调用这个方法时一定先判断null
     *
     * @param clazz          ViewController的子类
     * @param <T>            ViewController的子类
     * @param createItIfNull 如果没有是否需要创建一个
     * @return ViewController的子类对象
     */
    final public <T extends AbsViewController> T getViewController(Class<T> clazz, boolean createItIfNull) {
        T viewController = mViewControllerCacher.getViewController(clazz);
        if (createItIfNull && viewController == null) {
            viewController = produceViewController(clazz);
            if (viewController != null) {
                mViewControllerCacher.cacheViewController(viewController);
            }
        }
        return viewController;
    }

    protected <T extends AbsViewController> T produceViewController(Class<T> clazz) {
        return null;
    }

    final protected <T extends AbsViewController> void releaseViewController(Class<T> clazz) {
        T viewController = getViewController(clazz, false);
        if (viewController != null) {
            mViewControllerCacher.releaseViewController(viewController);
        }
    }
}
