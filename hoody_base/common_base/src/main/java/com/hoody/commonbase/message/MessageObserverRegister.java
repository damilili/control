package com.hoody.commonbase.message;

import java.util.HashSet;
import java.util.Set;

/**
 * 消息观察者注册器
 */
public final class MessageObserverRegister {
    private Set<IMessageObserver> mMessageObservers = new HashSet<>();

    public <T extends IMessageObserver> void regist(T observer) {
        if (observer != null) {
            MessageObserverManager.getInstance().regist(observer);
            mMessageObservers.add(observer);
        }
    }

    public <T extends IMessageObserver> void unregist(T observer) {
        MessageObserverManager.getInstance().unregister(observer);
    }

    public void unregistAll() {
        for (IMessageObserver mMessageObserver : mMessageObservers) {
            MessageObserverManager.getInstance().unregister(mMessageObserver);
        }
        mMessageObservers.clear();
    }
}
