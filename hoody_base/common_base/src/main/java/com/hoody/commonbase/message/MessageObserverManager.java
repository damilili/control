package com.hoody.commonbase.message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Created by cdm on 2021/11/18.
 */
final class MessageObserverManager {
    private final Set<IMessageObserver> observers = new HashSet<>();

    private static MessageObserverManager Instances;

    private MessageObserverManager() {
        Executors.newFixedThreadPool(4);
    }

    static MessageObserverManager getInstance() {
        if (Instances == null) {
            Instances = new MessageObserverManager();
        }
        return Instances;
    }

    synchronized <T extends IMessageObserver> List<T> getObservers(Class<T> sendTo) {
        ArrayList<T> result = new ArrayList<>();
        for (IMessageObserver observer : observers) {
            if (sendTo.isAssignableFrom(observer.getClass())) {
                result.add((T) observer);
            }
        }
        return result;
    }

    synchronized <T extends IMessageObserver> void unregister(T observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    synchronized <T extends IMessageObserver> void regist(T observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

}
