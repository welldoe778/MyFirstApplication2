package com.mocoo.hang.rtprinter.main;

import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.ToastUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsBluetoothPrintDriver;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.HsWifiPrintDriver;
import com.rtdriver.driver.LabelBluetoothPrintDriver;
import com.rtdriver.driver.LabelUsbPrintDriver;
import com.rtdriver.driver.LabelWifiPrintDriver;


public class MainActivity extends TabActivity implements Observer {

    private final String TAG = getClass().getSimpleName();
    private TabHost mTabHost;
    public static final String TAB_HS = "HEAT_SENSITIVE";
    public static final String TAB_LABLE = "LABLE";
    private RadioGroup mRadGroup;
    private TextView tvConnectState;//连接状态
    private BroadcastReceiver broadcastReceiver;
private static  Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        RTApplication.addActivity(this);
        findViewById();
        initView();
    }
    @Override
    protected void onNewIntent(Intent intent) { //重新连接，会触发此事件
        setIntent(intent);
        initBroadcast();
        if (RTApplication.currentMode==RTApplication.USB_MODE) {
         //  ToastUtil.show(mContext,"onNewIntent-connectUsb");
           connectUsb(null,true);
        }
    }
    private void findViewById() {
        mRadGroup = (RadioGroup) findViewById(R.id.main_radio_btn_group);
    }

    private void initView() {
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);

        mTabHost = getTabHost();

        Intent i_hs = new Intent(this, HeatSensitiveActivity.class);
        Intent i_lab = new Intent(this,LabelActivity.class);
        HeatSensitiveActivity.MainContext= this;
        mTabHost.addTab(mTabHost.newTabSpec(TAB_HS).setIndicator(TAB_HS)
                .setContent(i_hs));
        mTabHost.addTab(mTabHost.newTabSpec(TAB_LABLE)
                .setIndicator(TAB_LABLE).setContent(i_lab));

        mRadGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.main_tab_hs:
                                mTabHost.setCurrentTabByTag(TAB_HS);
                                RTApplication.mode=RTApplication.MODE_HS;
                                break;

                            case R.id.main_tab_lab:
                                mTabHost.setCurrentTabByTag(TAB_LABLE);
                                RTApplication.mode=RTApplication.MODE_LABEL;
                                break;
                            default:
                                break;
                        }
                        if (RTApplication.getConnState() != Contants.UNCONNECTED) {
                            disconnect();
                            ToastUtil.show(MainActivity.this, R.string.tip_disconnected);
                        }
                    }
                });
    }

    private void disconnect() {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver.getInstance().stop();
                LabelBluetoothPrintDriver.getInstance().stop();
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver.getInstance().stop();
                LabelUsbPrintDriver.getInstance().stop();
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver.getInstance().stop();
                LabelWifiPrintDriver.getInstance().stop();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvConnectState.setText(RTApplication.getConnStateString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
       // ToastUtil.show(mContext,"注销成功");
        ConnStateObservable.getInstance().deleteObserver(this);
        HsBluetoothPrintDriver.getInstance().stop();
        HsUsbPrintDriver.getInstance().stop();
        HsWifiPrintDriver.getInstance().stop();
        LabelBluetoothPrintDriver.getInstance().stop();
        LabelUsbPrintDriver.getInstance().stop();
        LabelWifiPrintDriver.getInstance().stop();
        RTApplication.removeActivity(this);
    }

    @Override
    public void update(Observable observable,final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectState.setText((CharSequence) data);
            }
        });
    }



    private void initBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                 //   ToastUtil.show(context,"接收到断开信息");
                    if (RTApplication.getConnState()==Contants.CONNECTED_BY_USB)
                    {
                        HsUsbPrintDriver.getInstance().stop();
                        LabelUsbPrintDriver.getInstance().stop();
                    }
                }
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
                //    ToastUtil.show(context,"插入USB");
                }
            }

        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    public static boolean connectUsb(UsbDevice usbDevice, boolean isReconnect) { //isReconnect：是否是重新连接
        boolean bIsSucc=false;
        UsbManager   mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
//        if (mUsbManager == null) {
//            mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
//        }
        HsUsbPrintDriver.getInstance().stop();
        LabelUsbPrintDriver.getInstance().stop();
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext.getApplicationInfo().packageName), 0);//
        if (!isReconnect && usbDevice!=null) { //点击按钮，直接连接usbDevice
            RTApplication.UsbDevame = mContext.getString(R.string.print_device) + usbDevice.getDeviceId();
            if (mUsbManager.hasPermission(usbDevice)) {
                if (RTApplication.mode == RTApplication.MODE_HS)
                    bIsSucc = HsUsbPrintDriver.getInstance().connect(usbDevice, mContext, mPermissionIntent);
                else
                    bIsSucc = LabelUsbPrintDriver.getInstance().connect(usbDevice, mContext, mPermissionIntent);
            }
                else
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
        }

        if (!bIsSucc) { //如果连接不成功能，要重新找设备,可能设备id已经变了
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            if (deviceList.size() > 0) {
                while (deviceIterator.hasNext()) {// 这里是if不是while，说明我只想支持一种device
                    final UsbDevice device = deviceIterator.next();
                    RTApplication.UsbDevame = mContext.getString(R.string.print_device) + device.getDeviceId();
                    if (mUsbManager.hasPermission(device)) {
                        if (HsUsbPrintDriver.getInstance().connect(device, mContext, mPermissionIntent)) {
                            bIsSucc = true;
                           break;
                        }
                    } else {
                        mUsbManager.requestPermission(device, mPermissionIntent);
                    }
                }

            }
        }
      return  bIsSucc;

    }
}
