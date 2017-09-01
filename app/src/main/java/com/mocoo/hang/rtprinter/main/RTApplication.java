package com.mocoo.hang.rtprinter.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.observable.ConnResultObservable;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.CrashHandler;
import com.mocoo.hang.rtprinter.utils.LogUtils;

import java.util.LinkedList;
import java.util.List;

import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsBluetoothPrintDriver;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.HsWifiPrintDriver;
import com.rtdriver.driver.LabelBluetoothPrintDriver;
import com.rtdriver.driver.LabelUsbPrintDriver;
import com.rtdriver.driver.LabelWifiPrintDriver;

/**
 * Created by Administrator on 2015/4/13.
 */
public class RTApplication extends Application {

    public static final String SP_NAME_SETTING = "setting";
    public static final int MODE_HS = 0x00;
    public static final int MODE_LABEL = 0x01;
    private static final String TAG = "BaseApplication";
    public static int mode = 0x00;
    private static RTApplication mApplication;
    private static ConnStateObservable mConnStateObservable;
    private static ConnResultObservable mConnResultObservable;
    private static String connectedStr, unConnectedStr;
    private static ForegroundColorSpan connectedColorSpan, unConnectedColorSpan;
    private static SpannableString connStateConnectedString, connStateUnConnectedString;
    private static List<Activity> activitys = null;
    private static int mConnState = Contants.UNCONNECTED;

    private static final int PRINTFINISHHANDLER_FLAG = 7174;
    public static boolean isPrintFinish = true;
    private static PrintFinishHandler printFinishHandler;
    public static  String UsbDevame;
    public static int currentMode = 1;
    public static final int BLUETOOTH_MODE = 0x01;
    public static final int USB_MODE = 0x02;
    public static final int WIFI_MODE = 0x03;
    //标签设置
    public static String labelSizeStr = "30 * 15";//尺寸
    public static String labelWidth = "30", labelHeight = "15";//宽高
    public static String labelGap = "3";//间隔
    public static String labelCopies = "1";//份数
    public static String labelSpeed = "2";//速度
    public static String labelDensity = "0";//浓度
    public static String labelDirection = "0";//方向
    public static Uri uri;


    public static int getConnState() {
        return mConnState;
    }

    private void setConnState(int connectState) {
        if (mConnState != connectState) {
            mConnState = connectState;
            mConnStateObservable.setChanged();
            mConnStateObservable.notifyObservers(getConnStateString());
        }
    }

    public static CharSequence getConnStateString() {
        if (mConnState == Contants.UNCONNECTED) {
            unConnectedStr = mApplication.getResources().getString(R.string.unconnected);
            connStateUnConnectedString = new SpannableString(unConnectedStr);
            if (unConnectedColorSpan == null) {
                unConnectedColorSpan = new ForegroundColorSpan(Color.RED);
            }
            connStateUnConnectedString.setSpan(unConnectedColorSpan, 0, unConnectedStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return connStateUnConnectedString;
        } else {
            connectedStr = mApplication.getResources().getString(R.string.connected);
            connStateConnectedString = new SpannableString(connectedStr);
            if (connectedColorSpan == null) {
                connectedColorSpan = new ForegroundColorSpan(Color.GREEN);
            }
            connStateConnectedString.setSpan(connectedColorSpan, 0, connectedStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return connStateConnectedString;
        }
    }

    public static void addActivity(Activity activity) {
        LogUtils.v(TAG, "activity add");
        if (activitys != null && activitys.size() > 0) {
            if (!activitys.contains(activity)) {
                activitys.add(activity);
            }
        } else {
            activitys.add(activity);
        }
    }

    public static void removeActivity(Activity activity) {
        activitys.remove(activity);
    }

    public static void exit() {
        finishAll();
        System.exit(0);
    }

    public static void finishAll() {
        LogUtils.v(TAG, "activity finish");
        if (activitys != null && activitys.size() > 0) {
            for (Activity activity : activitys) {
                LogUtils.v(TAG, "activity name = " + activity.getLocalClassName());
                activity.finish();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        LogUtils.setDebug(true);
        mApplication = this;
        mConnStateObservable = ConnStateObservable.getInstance();
        mConnResultObservable = ConnResultObservable.getInstance();
        activitys = new LinkedList<>();
        initDriver();
    }

    public static void inquiryFinish() {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.StatusInquiryFinish(PRINTFINISHHANDLER_FLAG, printFinishHandler);
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.StatusInquiryFinish(PRINTFINISHHANDLER_FLAG, printFinishHandler);
                break;
        }
    }

    private void initDriver() {
        UsbManager usbManager = (UsbManager) mApplication.getSystemService(Context.USB_SERVICE);
        HsUsbPrintDriver.getInstance().setUsbManager(usbManager);
        LabelUsbPrintDriver.getInstance().setUsbManager(usbManager);

        ConnStateHandler connStateHandler = new ConnStateHandler();
        HsBluetoothPrintDriver.getInstance().setHandler(connStateHandler);
        HsUsbPrintDriver.getInstance().setHandler(connStateHandler);
        HsWifiPrintDriver.getInstance().setHandler(connStateHandler);
        LabelBluetoothPrintDriver.getInstance().setHandler(connStateHandler);
        LabelUsbPrintDriver.getInstance().setHandler(connStateHandler);
        LabelWifiPrintDriver.getInstance().setHandler(connStateHandler);

        printFinishHandler = new PrintFinishHandler();
    }

    private class ConnStateHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (data.getInt("flag")) {
                case Contants.FLAG_STATE_CHANGE:
                    setConnState(data.getInt("state"));
                    break;
                case Contants.FLAG_FAIL_CONNECT:
                    mConnResultObservable.setChanged();
                    mConnResultObservable.notifyObservers(Contants.FLAG_FAIL_CONNECT);
                    break;
                case Contants.FLAG_SUCCESS_CONNECT:
                    mConnResultObservable.setChanged();
                    mConnResultObservable.notifyObservers(Contants.FLAG_SUCCESS_CONNECT);
                    break;
            }
        }
    }

    private class PrintFinishHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (data.getInt("flag")) {
                case PRINTFINISHHANDLER_FLAG:
                    int inquiry_status =data.getInt("state", 0) & 0xFF;
                    Log.d("inquiry_status------",inquiry_status+"");
                    if (inquiry_status == 0x80) {
                        isPrintFinish = true;
                    } else {
                        isPrintFinish = false;
                    }
                    break;
            }
        }
    }
}
