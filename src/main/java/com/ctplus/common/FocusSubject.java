package com.ctplus.common;

import java.util.HashMap;

public abstract class FocusSubject {

    protected HashMap<String, StrategyObserver> observerList = new HashMap<>();

    public void attach(StrategyObserver observer){
        observerList.put(observer.getName(),observer);
    }

    public void detach(String observerName){
        observerList.remove(observerName);
    }

    public abstract void notifyObservers(double price, String ts);

    public HashMap<String,StrategyObserver> getObserverMap(){
        return observerList;
    }
}
