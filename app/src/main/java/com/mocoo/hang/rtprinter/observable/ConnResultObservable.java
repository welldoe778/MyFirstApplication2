package com.mocoo.hang.rtprinter.observable;

import java.util.Observable;

/**
 * Created by Administrator on 2015/6/11.
 * 通知单次连接结果
 */
public class ConnResultObservable extends Observable {

    private static ConnResultObservable mConnResultObservable;

    private ConnResultObservable() {
    }

    public static ConnResultObservable getInstance(){
        if(mConnResultObservable == null){
            mConnResultObservable = new ConnResultObservable();
        }
        return mConnResultObservable;
    }

    public void setChanged(){
        super.setChanged();
    }

}
