package com.mocoo.hang.rtprinter.observable;

import java.util.Observable;

/**
 * Created by Administrator on 2015/6/10.
 * 当连接状态改变时通知各个观察者更新状态
 */
public class ConnStateObservable extends Observable{

    private static ConnStateObservable mConnStateObservable;

    private ConnStateObservable() {
    }

    public static ConnStateObservable getInstance(){
        if(mConnStateObservable == null){
            mConnStateObservable = new ConnStateObservable();
        }
        return mConnStateObservable;
    }

    public void setChanged(){
        super.setChanged();
    }

}
