package com.hoody.audience.view.fragment;

/**
 * 侧边栏控制接口
 */
public interface IMenuControl {
    /**
     * 打开侧边栏
     */
    void openDrawer();

    /**
     * 关闭侧边栏
     */
    void closeDrawer(boolean anim);

    /**
     * @return 侧边栏是否开启
     */
    boolean isDrawerOpen();

    /**
     * 更换侧边栏
     */
    void changeDrawerContent();
}
