package com.hoody.commonbase.view.activity;

import com.hoody.annotation.viewcontroller.AbsViewController;

public class ViewControllerActivity extends ThemeActivity {

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mViewControllerCacher.releaseAllViewController();
        }
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
