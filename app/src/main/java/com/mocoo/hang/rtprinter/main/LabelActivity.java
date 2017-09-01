package com.mocoo.hang.rtprinter.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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
import com.mocoo.hang.rtprinter.dialog.CustomProcessDialog;
import com.mocoo.hang.rtprinter.dialog.UsbDeviceChooseDialog;
import com.mocoo.hang.rtprinter.dialog.WifiDeviceChooseDialog;
import com.mocoo.hang.rtprinter.interfaces.CustomDialogInterface;
import com.mocoo.hang.rtprinter.observable.ConnResultObservable;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.print.BarcodeActivity;
import com.mocoo.hang.rtprinter.print.ImagePrintActivity;
import com.mocoo.hang.rtprinter.print.WebPagePrintActivity;
import com.mocoo.hang.rtprinter.receiver.UsbDeviceReceiver;
import com.mocoo.hang.rtprinter.templet.LabelTempletActivity1;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.rtprinter.utils.ToastUtil;
import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.LabelBluetoothPrintDriver;
import com.rtdriver.driver.LabelUsbPrintDriver;
import com.rtdriver.driver.LabelWifiPrintDriver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
//import com.example.tscdll.TSCActivity

/**
 * Created by Administrator on 2015/5/28.
 */
public class LabelActivity extends Activity implements View.OnClickListener, Observer, RadioGroup.OnCheckedChangeListener {

    private final String TAG = getClass().getSimpleName();
    private static final String CON_TYPE = "label";//连接种类，标签

    private static final int BLUETOOTH_MODE = 0x01;
    private static final int USB_MODE = 0x02;
    private static final int WIFI_MODE = 0x03;
    private static final int REQUEST_ENABLE_BT = 0xf0;

    private byte flagAlignMode = 0x00;//0x00表示左对齐，0x01表示居中，0x02表示右对齐

    private int currentMode = 1;
    private boolean mUsbRegistered = false;//表示UsbDeviceReceiver是否已被注册

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private UsbDeviceReceiver mUsbReceiver;
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
  //  private IntentFilter usbIntentFilter;
    private CustomProcessDialog mDialog;

    private TextView llConnectSpinner, tvConnectState;//蓝牙，usb，wifi设置
    private RadioGroup rgConnectMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);
        mContext = this;
        ConnResultObservable.getInstance().addObserver(this);
        RTApplication.addActivity(this);
        initView();
        setListener();
    }

    public void initView() {
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
        llConnectSpinner = (TextView) findViewById(R.id.ll_label_connect_spinner);
        llConnectSpinner.setHint(R.string.choose_bluetooth_device);
        rgConnectMode = (RadioGroup) findViewById(R.id.rg_label_connect_mode);

        setListener();
    }

    private void setListener() {
        rgConnectMode.setOnCheckedChangeListener(this);
        llConnectSpinner.setOnClickListener(this);
    }

    private void notifyModeChanged() {
        switch (currentMode) {
            case BLUETOOTH_MODE:
                llConnectSpinner.setHint(R.string.choose_bluetooth_device);
                break;
            case USB_MODE:
                llConnectSpinner.setHint(R.string.choose_usb_device);
                break;
            case WIFI_MODE:
                llConnectSpinner.setHint(R.string.choose_wifi_device);
                break;
        }

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
                mBluetoothDevice = device;
            }
        });
        bluetoothDeviceChooseDialog.show(getFragmentManager(), null);
    }

    private void showUSBDeviceChooseDialog() {

        if (!mUsbRegistered) {
          //  registerUsbReceiver();
            mUsbRegistered = true;
        }
        final UsbDeviceChooseDialog usbDeviceChooseDialog = new UsbDeviceChooseDialog();
        usbDeviceChooseDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mUsbDevice = (UsbDevice) parent.getAdapter().getItem(position);
                llConnectSpinner.setText(getString(R.string.print_device) + mUsbDevice.getDeviceId());
                usbDeviceChooseDialog.dismiss();
            }
        });
        usbDeviceChooseDialog.show(getFragmentManager(), null);
    }

    private void registerUsbReceiver() {
        if (mUsbReceiver == null) {
            mUsbReceiver = new UsbDeviceReceiver(new UsbDeviceReceiver.CallBack() {
                @Override
                public void onPermissionGranted(UsbDevice usbDevice) {
                //    connectUsb(usbDevice,true);
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
//        if (usbIntentFilter == null) {
//            usbIntentFilter = new IntentFilter();
//            usbIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//            usbIntentFilter.addAction(UsbDeviceReceiver.ACTION_USB_PERMISSION);
//        }
    //    registerReceiver(mUsbReceiver, usbIntentFilter);
      //  mUsbRegistered = true;
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

    private void disconnect() {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                LabelBluetoothPrintDriver.getInstance().stop();
                break;
            case Contants.CONNECTED_BY_USB:
                LabelUsbPrintDriver.getInstance().stop();
                break;
            case Contants.CONNECTED_BY_WIFI:
                LabelWifiPrintDriver.getInstance().stop();
                break;
        }
    }

    private void connect() {
        switch (currentMode) {
            case BLUETOOTH_MODE:
                if (TextUtils.isEmpty(llConnectSpinner.getText().toString())) {
                    ToastUtil.show(mContext, R.string.tip_choose_bluetooth_device);
                    return;
                }
                connectBluetooth();
                break;
            case USB_MODE:
                if (TextUtils.isEmpty(llConnectSpinner.getText().toString())) {
                    ToastUtil.show(mContext, R.string.tip_choose_usb_device);
                    return;
                }
                MainActivity.connectUsb(mUsbDevice,false);
//                if (mUsbManager == null) {
//                    mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
//                }
//                if (mUsbManager.hasPermission(mUsbDevice)) {
//                    connectUsb(mUsbDevice);
//                } else {
//                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(mUsbReceiver.ACTION_USB_PERMISSION), 0);
//                    mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
//                }
                break;
            case WIFI_MODE:
                if (TextUtils.isEmpty(llConnectSpinner.getText().toString())) {
                    ToastUtil.show(mContext, R.string.tip_choose_wifi_device);
                    return;
                }
                connectWifi();
                break;
        }
    }

    private void connectBluetooth() {
        LabelBluetoothPrintDriver labelBluetoothPrintDriver = LabelBluetoothPrintDriver.getInstance();
        labelBluetoothPrintDriver.start();
        labelBluetoothPrintDriver.connect(mBluetoothDevice);
    }

//    private void connectUsb(UsbDevice usbDevice) {
//        LabelUsbPrintDriver labelUsbPrintDriver = LabelUsbPrintDriver.getInstance();
//        labelUsbPrintDriver.connect(usbDevice);
//    }


    private void connectWifi() {
        final String[] address = llConnectSpinner.getText().toString().trim().split(":");
        new Thread(new Runnable() {
            @Override
            public void run() {
                LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
                labelWifiPrintDriver.WIFISocket(address[0], Integer.valueOf(address[1]));
            }
        }).start();

        //pingIp
        new Thread(new Runnable() {
            @Override
            public void run() {
                LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
                boolean isNoCon = true;
                do {

                    isNoCon = labelWifiPrintDriver.IsNoConnection(address[0]);//address[0]
                    Log.d("ping的地址", address[0]);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("ping的结果", isNoCon + "");
                } while (!isNoCon);
                if (labelWifiPrintDriver.mysocket != null) {
                    try {
                        labelWifiPrintDriver.mysocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    labelWifiPrintDriver.mysocket = null;
                }
            }
        }).start();
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.ll_label_connect_spinner:
                switch (currentMode) {
                    case BLUETOOTH_MODE:
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
                    case USB_MODE:
                        showUSBDeviceChooseDialog();
                        break;
                    case WIFI_MODE:
                        showWifiDeviceChooseDialog();
                        break;
                }
                break;
            case R.id.tv_label_connect:
                if (RTApplication.getConnState() != Contants.UNCONNECTED) {
                    disconnect();
                    connect();
                } else {
                    connect();
                }
                break;
            case R.id.tv_label_print_self_test_page:
                printSelfTestPage();
                break;
            case R.id.tv_label_text_print:
                textPrint();
                break;
            case R.id.tv_label_barcode_print:
                intent.setClass(this, BarcodeActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_label_templet_print:
                intent.setClass(this, LabelTempletActivity1.class);
                startActivity(intent);
                break;
            case R.id.tv_label_picture_print:
                intent.setClass(this, ImagePrintActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_label_web_page_print:
                intent.setClass(this, WebPagePrintActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_lable_wifi_set:
                if (RTApplication.getConnState() != Contants.UNCONNECTED) {
                    intent.setClass(this, WifiSettingActivity.class);
                    //传入当前模式
                    intent.putExtra("currentMode",currentMode);
                    intent.putExtra("CON_TYPE",CON_TYPE);
                    startActivity(intent);
                } else {
                    ToastUtil.show(mContext, "unConnection");
                }
                break;
            case R.id.tv_lable_net_set:
                if (RTApplication.getConnState() != Contants.UNCONNECTED) {
                    intent.setClass(this, WifiSettingNetActivity.class);
                    //传入当前模式
                    intent.putExtra("currentMode", currentMode);
                    intent.putExtra("CON_TYPE", CON_TYPE);
                    startActivity(intent);
                } else {
                    ToastUtil.show(mContext, "unConnection");
                }
                break;
            case R.id.tv_sound:
                switch (RTApplication.getConnState()) {
                    case Contants.UNCONNECTED:
                        ToastUtil.show(mContext, R.string.tip_connect);
                        break;
                    case Contants.CONNECTED_BY_BLUETOOTH:
                        LabelBluetoothPrintDriver.getInstance().SetSOUND("2", "200");
                        break;
                    case Contants.CONNECTED_BY_USB:
                        LabelUsbPrintDriver.getInstance().SetSOUND("2", "200");
                        break;
                    case Contants.CONNECTED_BY_WIFI:
                        LabelWifiPrintDriver.getInstance().SetSOUND("2", "200");
                        break;
                }

            case R.id.tv_pcx:
                printPCX();
                break;

        }

    }

  private  void printPCX(){
      switch (RTApplication.getConnState()) {
          case Contants.UNCONNECTED:
              ToastUtil.show(mContext, R.string.tip_connect);
              break;
          case Contants.CONNECTED_BY_BLUETOOTH:
              LabelBluetoothPrintDriver labelBluetoothPrintDriver = LabelBluetoothPrintDriver.getInstance();
              labelBluetoothPrintDriver.Begin();
              labelBluetoothPrintDriver.SetCLS();
              labelBluetoothPrintDriver.SetDIRECTION("0");
              labelBluetoothPrintDriver.SetSize("80", "50");
              labelBluetoothPrintDriver.SetGAP("0", "0");
              labelBluetoothPrintDriver.Tsc_downloadfile("/sdcard/Download/SAMPLE.PCX","SAMPLE.PCX");
              labelBluetoothPrintDriver.Tsc_PutPcx("10","10","SAMPLE.PCX");
              labelBluetoothPrintDriver.SetPRINT("1", "1");
              labelBluetoothPrintDriver.Tsc_KILL("SAMPLE.PCX");
              break;
          case Contants.CONNECTED_BY_USB:
              LabelUsbPrintDriver labelUsbPrintDriver = LabelUsbPrintDriver.getInstance();
              labelUsbPrintDriver.Begin();
              labelUsbPrintDriver.SetCLS();
              labelUsbPrintDriver.SetDIRECTION("0");
              labelUsbPrintDriver.SetSize("80", "50");
              labelUsbPrintDriver.SetGAP("0", "0");
              labelUsbPrintDriver.Tsc_downloadfile("/sdcard/Download/SAMPLE.PCX","SAMPLE.PCX");
              labelUsbPrintDriver.Tsc_PutPcx("10","10","SAMPLE.PCX");
              labelUsbPrintDriver.SetPRINT("1", "1");
              labelUsbPrintDriver.Tsc_KILL("SAMPLE.PCX");;
              break;
          case Contants.CONNECTED_BY_WIFI:
              LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
              labelWifiPrintDriver.Begin();
              labelWifiPrintDriver.SetCLS();
              labelWifiPrintDriver.SetDIRECTION("0");
              labelWifiPrintDriver.SetSize("80", "50");
              labelWifiPrintDriver.SetGAP("0", "0");
              labelWifiPrintDriver.Tsc_downloadfile("/sdcard/Download/SAMPLE.PCX","SAMPLE.PCX");
              labelWifiPrintDriver.Tsc_PutPcx("10","10","SAMPLE.PCX");
              labelWifiPrintDriver.SetPRINT("1", "1");
              labelWifiPrintDriver.Tsc_KILL("SAMPLE.PCX");
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
                LabelBluetoothPrintDriver labelBluetoothPrintDriver = LabelBluetoothPrintDriver.getInstance();
                labelBluetoothPrintDriver.Begin();
                labelBluetoothPrintDriver.SetCLS();
                labelBluetoothPrintDriver.SelftestPrint();
                break;
            case Contants.CONNECTED_BY_USB:
                LabelUsbPrintDriver labelUsbPrintDriver = LabelUsbPrintDriver.getInstance();
                labelUsbPrintDriver.Begin();
                labelUsbPrintDriver.SetCLS();
                labelUsbPrintDriver.SelftestPrint();
                break;
            case Contants.CONNECTED_BY_WIFI:
                LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
                labelWifiPrintDriver.Begin();
                labelWifiPrintDriver.SetCLS();
                labelWifiPrintDriver.SelftestPrint();
                break;
        }
    }

    /**
     * 文本打印
     */
    private void textPrint() {
        String mLabelWidth = "30";
        String mLabelHeight = "15";
        StringBuilder sb;
        int blankCount = 0x00;
        int letterHeight = 12;//打印机文本字体高度
        int distanceY = (int) Math.ceil(letterHeight * 1.2);//打印机文本字体换行间隔
        String Font = "1";
        String content = "123456722222222222222222222222";
        String[] texts = content.split("\\n");
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                LabelBluetoothPrintDriver labelBluetoothPrintDriver = LabelBluetoothPrintDriver.getInstance();
                labelBluetoothPrintDriver.Begin();
                labelBluetoothPrintDriver.SetCLS();
                labelBluetoothPrintDriver.SetSize(mLabelWidth, mLabelHeight);
                for (int i = 0; i < texts.length; i++) {
                    String text = texts[i];
                    sb = new StringBuilder();
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(text);
                    labelBluetoothPrintDriver.PrintText("10", String.valueOf(10 + i * distanceY), Font, "0", "1", "1", sb.toString());
                }
                labelBluetoothPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelBluetoothPrintDriver.endPro();
                break;
            case Contants.CONNECTED_BY_USB:
                LabelUsbPrintDriver labelUsbPrintDriver = LabelUsbPrintDriver.getInstance();
                labelUsbPrintDriver.Begin();
                labelUsbPrintDriver.SetCLS();
                labelUsbPrintDriver.SetSize(mLabelWidth, mLabelHeight);
                for (int i = 0; i < texts.length; i++) {
                    String text = texts[i];
                    sb = new StringBuilder();
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(text);
//                    labelUsbPrintDriver
                    labelUsbPrintDriver.PrintText("10", String.valueOf(10 + i * distanceY), Font, "0", "1", "1", sb.toString());
                }
                labelUsbPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelUsbPrintDriver.endPro();
                break;
            case Contants.CONNECTED_BY_WIFI:
                LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
                labelWifiPrintDriver.Begin();
                labelWifiPrintDriver.SetCLS();
                labelWifiPrintDriver.SetSize(mLabelWidth, mLabelHeight);
                for (int i = 0; i < texts.length; i++) {
                    String text = texts[i];
                    LogUtils.d(TAG, "text = " + text);
                    sb = new StringBuilder();
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(text);
                    labelWifiPrintDriver.PrintText("10", String.valueOf(10 + i * distanceY), Font, "0", "1", "1", sb.toString());
                }
                labelWifiPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelWifiPrintDriver.endPro();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_label_bluetooth:
                currentMode = BLUETOOTH_MODE;
                notifyModeChanged();
                break;
            case R.id.rb_label_usb:
                currentMode = USB_MODE;
                notifyModeChanged();
                break;
            case R.id.rb_label_wifi:
                currentMode = WIFI_MODE;
                notifyModeChanged();
                break;
        }
    }

    @Override
    public void update(Observable observable, final Object data) {
        if (RTApplication.mode != RTApplication.MODE_LABEL) {
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
