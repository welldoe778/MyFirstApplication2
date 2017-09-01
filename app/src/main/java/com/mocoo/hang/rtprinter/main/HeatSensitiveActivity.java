package com.mocoo.hang.rtprinter.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.dialog.BluetoothDeviceChooseDialog;
import com.mocoo.hang.rtprinter.dialog.UsbDeviceChooseDialog;
import com.mocoo.hang.rtprinter.dialog.WifiDeviceChooseDialog;
import com.mocoo.hang.rtprinter.interfaces.CustomDialogInterface;
import com.mocoo.hang.rtprinter.observable.ConnResultObservable;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.pdf.ChoosePDFActivity;
import com.mocoo.hang.rtprinter.print.BarcodeActivity;
import com.mocoo.hang.rtprinter.print.ImagePrintActivity;
import com.mocoo.hang.rtprinter.print.WebPagePrintActivity;
import com.mocoo.hang.rtprinter.receiver.UsbDeviceReceiver;
import com.mocoo.hang.rtprinter.templet.HsTempletActivity1;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.rtprinter.utils.ToastUtil;
import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsBluetoothPrintDriver;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.HsWifiPrintDriver;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2015/5/28.
 */
public class HeatSensitiveActivity extends Activity implements View.OnClickListener, Observer {

    private final String TAG = getClass().getSimpleName();
    private static final String CON_TYPE="heatSensitive";//连接种类，热敏


    private static final int REQUEST_ENABLE_BT = 0xf0;

    private byte flagCharacterMode = 0x00;//英文字体模式
    private byte flagAlignMode = 0x00;//0x00表示左对齐，0x01表示居中，0x02表示右对齐
    private byte flagUnderLineMode = 0x00;//0x00表示解除下划线，0x01下划线宽度为1，0x02下划线宽度为2
    private byte flagChineseCharacterMode = 0x00;//中文字体模式

    private boolean mUsbRegistered = false;//表示UsbDeviceReceiver是否已被注册
    private Context mContext;
    public static Context MainContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private UsbDeviceReceiver mUsbReceiver;
    private UsbDevice mUsbDevice;
    private IntentFilter usbIntentFilter;

    private TextView llConnectSpinner;//蓝牙，usb，wifi设置
    private TextView tvConnectState;
    private RadioGroup rgConnectMode;
    public  HeatSensitiveActivity heatSensitiveActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        //heatSensitiveActivity=this;

        setContentView(R.layout.acitivty_heat_sensitive);
        ConnResultObservable.getInstance().addObserver(this);
        RTApplication.addActivity(this);
        initView();
    }

    private void initView() {
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
        llConnectSpinner = (TextView) this.findViewById(R.id.heat_sensitive_setting_connect_spinner);
        llConnectSpinner.setHint(R.string.choose_bluetooth_device);
        rgConnectMode = (RadioGroup) this.findViewById(R.id.rg_heat_sensitive_setting_connect_mode);
        setListener();
    }
    private void disconnect() {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver.getInstance().stop();
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver.getInstance().stop();
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver.getInstance().stop();
                break;
        }

    }
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.tv_heat_sensitive_connect:
                if (RTApplication.getConnState() != Contants.UNCONNECTED) {
                    disconnect();
                    connect();
                }else{
                    connect();
                }
                break;
            case R.id.tv_heat_sensitive_print_self_test_page:
                printSelfTestPage();
                break;
            case R.id.tv_heat_sensitive_beeper_test:
                beeperTest();
                break;
            case R.id.tv_heat_sensitive_cash_box_test:
                StatusTest();
               // cashBoxTest();
                break;
            case R.id.tv_heat_sensitive_print_all_cut:
                AllCutterTest();
                break;
            case R.id.tv_heat_sensitive_print_half_cut:
                halfCutterTest();
                break;
            case R.id.tv_heat_sensitive_text_print:
                textPrint();
                break;
            case R.id.tv_heat_sensitive_barcode_print:
                intent.setClass(this, BarcodeActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_heat_sensitive_templet_print:
                intent.setClass(this, HsTempletActivity1.class);
                startActivity(intent);
                break;
            case R.id.tv_heat_sensitive_picture_print:
                intent.setClass(this, ImagePrintActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_heat_sensitive_web_page_print:
                intent.setClass(this, WebPagePrintActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_heat_sensitive_wifi_set:
                if (RTApplication.getConnState() != Contants.UNCONNECTED) {
                    intent.setClass(this, WifiSettingActivity.class);
                    //传入当前模式
                    intent.putExtra("currentMode",RTApplication.currentMode);
                    intent.putExtra("CON_TYPE",CON_TYPE);
                    startActivity(intent);
                } else {
                    ToastUtil.show(mContext, "unConnection");
                }
                break;
            case R.id.tv_heat_sensitive_net_set:
                if (RTApplication.getConnState() != Contants.UNCONNECTED) {
                    intent.setClass(this, WifiSettingNetActivity.class);
                    //传入当前模式
                    intent.putExtra("currentMode",RTApplication.currentMode);
                    intent.putExtra("CON_TYPE",CON_TYPE);
                    startActivity(intent);
                } else {
                    ToastUtil.show(mContext, "unConnection");
                }
                break;
            case R.id.tv_heat_sensitive_pdf_print:
                intent.setClass(this, ChoosePDFActivity.class);
                startActivity(intent);

               // ToastUtil.show(mContext,"pdf print");
                break;



        }

    }

    /**
     * 全切
     */
    private void AllCutterTest() {
        switch (RTApplication.getConnState()) {
            case Contants.UNCONNECTED:
                ToastUtil.show(mContext, R.string.tip_connect);
                break;
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();

                hsBluetoothPrintDriver.CutPaper();
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();

                hsUsbPrintDriver.CutPaper();
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();

                hsWifiPrintDriver.CutPaper();
                break;
        }
    }

    /**
     * 半切
     */
    private void halfCutterTest() {
        switch (RTApplication.getConnState()) {
            case Contants.UNCONNECTED:
                ToastUtil.show(mContext, R.string.tip_connect);
                break;
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();

                hsBluetoothPrintDriver.PartialCutPaper();
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();

                hsUsbPrintDriver.PartialCutPaper();
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();

                hsWifiPrintDriver.PartialCutPaper();
                break;
        }
    }

    /**
     * 打印测试页
     */
    private void printSelfTestPage() {
        switch (RTApplication.getConnState()) {
            case Contants.UNCONNECTED:
                ToastUtil.show(mContext, R.string.tip_connect);
                break;
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.SelftestPrint();
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();
                hsUsbPrintDriver.SelftestPrint();
                break;
            case Contants.CONNECTED_BY_WIFI:
                ToastUtil.show(this,"wifiprint");
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();
                hsWifiPrintDriver.SelftestPrint();
                break;
        }
    }

    /**
     * 蜂鸣
     */
    private void beeperTest() {
        switch (RTApplication.getConnState()) {
            case Contants.UNCONNECTED:
                ToastUtil.show(mContext, R.string.tip_connect);
                break;
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.Beep((byte) 0x01, (byte) 0x03);
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();
                hsUsbPrintDriver.Beep((byte) 0x01, (byte) 0x03);
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();
                hsWifiPrintDriver.Beep((byte) 0x01, (byte) 0x03);
                break;
        }
    }
 private void  StatusTest(){
     HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
    // hsBluetoothPrintDriver.Begin();
  //   hsBluetoothPrintDriver.SetDefaultSetting();
    // byte irtn = hsBluetoothPrintDriver.StatusInquiry((byte)1);
     int i=hsBluetoothPrintDriver.getState();
     if (hsBluetoothPrintDriver.IsNoConnection())//{
         ToastUtil.show(mContext, "IsNoConnection=True"+"断开="+Integer.toString(i));
     else
       ToastUtil.show(mContext, "IsNoConnection=false"+"连接"+String.valueOf(i));
   //  ToastUtil.show(mContext, irtn);


 }
    /**
     * 钱箱
     */
    private void cashBoxTest() {
        switch (RTApplication.getConnState()) {
            case Contants.UNCONNECTED:
                ToastUtil.show(mContext, R.string.tip_connect);
                break;
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.OpenDrawer((byte) 0x00, (byte) 0x05, (byte) 0x00);
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();
                hsUsbPrintDriver.OpenDrawer((byte) 0x00, (byte) 0x05, (byte) 0x00);
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();
                hsWifiPrintDriver.OpenDrawer((byte) 0x00, (byte) 0x05, (byte) 0x00);
                break;
        }
    }

    private void textPrint() {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.SetAlignMode(flagAlignMode);
                hsBluetoothPrintDriver.SetCharacterPrintMode(flagCharacterMode);
                hsBluetoothPrintDriver.SetUnderline(flagUnderLineMode);
                hsBluetoothPrintDriver.SelChineseCodepage();
                hsBluetoothPrintDriver.SetChineseCharacterMode(flagChineseCharacterMode);
                String metContent ="123abc一二三";
                hsBluetoothPrintDriver.BT_Write(metContent);
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();

                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();
                hsUsbPrintDriver.SetAlignMode(flagAlignMode);
                hsUsbPrintDriver.SetCharacterPrintMode(flagCharacterMode);
                hsUsbPrintDriver.SetUnderline(flagUnderLineMode);
                //hsUsbPrintDriver.SelChineseCodepage();
                //hsUsbPrintDriver.SetChineseCharacterMode(flagChineseCharacterMode);
                hsUsbPrintDriver.setCharsetName("GBK");
                hsUsbPrintDriver.USB_Write("123abc一二三");

                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();

                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();
                hsWifiPrintDriver.SetAlignMode(flagAlignMode);
                hsWifiPrintDriver.SetCharacterPrintMode(flagCharacterMode);
                hsWifiPrintDriver.SetUnderline(flagUnderLineMode);
                //hsWifiPrintDriver.SelChineseCodepage();
                //hsWifiPrintDriver.SetChineseCharacterMode(flagChineseCharacterMode);
                hsWifiPrintDriver.setCharsetName("GBK");
                hsWifiPrintDriver.SetCharacterFont((byte) 0);
                hsWifiPrintDriver.WIFI_Write("123abc一二三");
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.SetCharacterFont((byte) 1);
                hsWifiPrintDriver.WIFI_Write("123abc一二三");
                byte byt2 = hsWifiPrintDriver.StatusInquiryFinish();
                int b=0;
                b = byt2 & 0xFF;

                Log.e("wifi Status", "b===" + b);

                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        ConnResultObservable.getInstance().deleteObserver(this);
        if (mUsbRegistered) {
           // unregisterReceiver(mUsbReceiver);
            mUsbRegistered = false;
        }
        RTApplication.removeActivity(this);
    }


    private void setListener() {

        rgConnectMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_heat_sensitive_setting_bluetooth:
                        RTApplication.currentMode = RTApplication.BLUETOOTH_MODE;
                        notifyModeChanged();
                        break;
                    case R.id.rb_heat_sensitive_setting_usb:
                        RTApplication.currentMode = RTApplication.USB_MODE;
                        notifyModeChanged();
                        break;
                    case R.id.rb_heat_sensitive_setting_wifi:
                        RTApplication.currentMode = RTApplication.WIFI_MODE;
                        notifyModeChanged();
                        break;
                }
            }
        });
        llConnectSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (RTApplication.currentMode) {

                    case RTApplication.BLUETOOTH_MODE:
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            ToastUtil.show(mContext, R.string.device_does_not_support_bluetooth);
                            return;
                        }
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            return;
                        }
                        showBluetoothDeviceChooseDialog();
                        break;

                    case RTApplication.USB_MODE:
                        showUSBDeviceChooseDialog();
                        break;
                    case RTApplication.WIFI_MODE:
                        showWifiDeviceChooseDialog();
                        break;

                }


            }
        });

    }

    private void showBluetoothDeviceChooseDialog() {
        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(new BluetoothDeviceChooseDialog.onDeviceItemClickListener() {
            @Override
            public void onDeviceItemClick(BluetoothDevice device) {
                if (TextUtils.isEmpty(device.getName())) {
                    llConnectSpinner.setText(device.getAddress());
                } else {
                    llConnectSpinner.setText(device.getName());
                }
//                LogUtils.d(TAG,"device.getType() = " + device.getType());
                LogUtils.d(TAG, "device.getBluetoothClass() = " + device.getBluetoothClass());
                mBluetoothDevice = device;
            }
        });
        bluetoothDeviceChooseDialog.show(getFragmentManager(), null);
    }

    private void showUSBDeviceChooseDialog() {

        if (!mUsbRegistered) {
           // registerUsbReceiver();
            mUsbRegistered = true;
        }
        final UsbDeviceChooseDialog usbDeviceChooseDialog = new UsbDeviceChooseDialog();
        usbDeviceChooseDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUsbDevice = (UsbDevice) parent.getAdapter().getItem(position);
                llConnectSpinner.setText(getString(R.string.print_device)+mUsbDevice.getDeviceId()); //+ (position + 1));
                usbDeviceChooseDialog.dismiss();
            }
        });
        usbDeviceChooseDialog.show(getFragmentManager(), null);
    }

    private void registerUsbReceiver() {//不要用此方法
        if (mUsbReceiver == null) {
            mUsbReceiver = new UsbDeviceReceiver(new UsbDeviceReceiver.CallBack() {
                @Override
                public void onPermissionGranted(UsbDevice usbDevice) {
                  //  connectUsb(usbDevice);
                }

                @Override
                public void onDeviceAttached(UsbDevice usbDevice) {

                }

                @Override
                public void onDeviceDetached(UsbDevice usbDevice) {
                    HsUsbPrintDriver.getInstance().stop();
                    mUsbDevice = null;
                    llConnectSpinner.setText(null);
                }
            });
        }
        if (usbIntentFilter == null) {
            usbIntentFilter = new IntentFilter();
            usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            usbIntentFilter.addAction(UsbDeviceReceiver.ACTION_USB_PERMISSION);
        }
        registerReceiver(mUsbReceiver, usbIntentFilter);
        mUsbRegistered = true;
    }

    private void showWifiDeviceChooseDialog() {

        WifiDeviceChooseDialog wifiDeviceChooseDialog = new WifiDeviceChooseDialog();
        Bundle args = new Bundle();
        args.putString(WifiDeviceChooseDialog.BUNDLE_KEY_ADDRESS, llConnectSpinner.getText().toString());
        wifiDeviceChooseDialog.setArguments(args);
        wifiDeviceChooseDialog.setOnClickListener(new CustomDialogInterface.onPositiveClickListener() {
            @Override
            public void onDialogPositiveClick(String text) {
                llConnectSpinner.setText(text);
            }
        });
        wifiDeviceChooseDialog.show(getFragmentManager(), null);
    }

    private void notifyModeChanged() {
        switch (RTApplication.currentMode) {
            case RTApplication.BLUETOOTH_MODE:
                llConnectSpinner.setHint(R.string.choose_bluetooth_device);
                break;
            case RTApplication.USB_MODE:
                llConnectSpinner.setHint(R.string.choose_usb_device);
                break;
            case RTApplication.WIFI_MODE:
                llConnectSpinner.setHint(R.string.choose_wifi_device);
                break;
        }
    }

    private void connect() {
        switch (RTApplication.currentMode) {
            case RTApplication.BLUETOOTH_MODE:
                if (TextUtils.isEmpty(llConnectSpinner.getText().toString())) {
                    ToastUtil.show(mContext, R.string.tip_choose_bluetooth_device);
                    return;
                }
                connectBluetooth();
                break;
            case RTApplication.USB_MODE:
                if (TextUtils.isEmpty(llConnectSpinner.getText().toString())) {
                    ToastUtil.show(mContext, R.string.tip_choose_usb_device);
                    return;
                }
                 MainActivity.connectUsb(mUsbDevice,true);
                break;
            case RTApplication.WIFI_MODE:
                if (TextUtils.isEmpty(llConnectSpinner.getText().toString())) {
                    ToastUtil.show(mContext, R.string.tip_choose_wifi_device);
                    return;
                }
                connectWifi();
                break;
        }
    }

    private void connectBluetooth() {
        HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
        hsBluetoothPrintDriver.start();
        LogUtils.d(TAG, "mBluetoothDevice getAddress = " + mBluetoothDevice.getAddress());
        hsBluetoothPrintDriver.connect(mBluetoothDevice);
    }

//    private void connectUsb(UsbDevice usbDevice) {
//        HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
//        hsUsbPrintDriver.connect(usbDevice);
//    }



    private void connectWifi() {
        final String[] address = llConnectSpinner.getText().toString().trim().split(":");
        //连接wifi
        new Thread(new Runnable() {
            @Override
            public void run() {
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                //ip-----address[0],port----address[1]
                hsWifiPrintDriver.WIFISocket(address[0], Integer.valueOf(address[1]));
            }
        }).start();
        //pingIp
        new Thread(new Runnable() {
            @Override
            public void run() {
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                boolean isNoCon = true;
                do {
                    isNoCon = hsWifiPrintDriver.IsNoConnection(address[0]);
                    Log.d(TAG,"ping的地址:"+ address[0]);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"ping的结果:"+ isNoCon+"");
                } while (!isNoCon);
                if (hsWifiPrintDriver.mysocket != null) {
                    try {
                        hsWifiPrintDriver.mysocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    hsWifiPrintDriver.mysocket = null;
                }
            }
        }).start();
    }


    @Override
    public void update(Observable observable, final Object data) {
        if (RTApplication.mode != RTApplication.MODE_HS) {
            return;
        }
        if (observable == ConnStateObservable.getInstance()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvConnectState.setText((CharSequence) data);
                }
            });
        } else if (observable == ConnResultObservable.getInstance()) {
            switch ((int) data) {
                case Contants.FLAG_FAIL_CONNECT:
                    ToastUtil.show(mContext, getString(R.string.fail_connect));
                    break;
                case Contants.FLAG_SUCCESS_CONNECT:
                    llConnectSpinner.setText(RTApplication.UsbDevame);
                    ToastUtil.show(mContext, getString(R.string.success_connect));
                    break;
            }
        }
    }


}
