package com.mocoo.hang.rtprinter.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.adapter.WifiListAdapter;
import com.mocoo.hang.rtprinter.observable.ConnResultObservable;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.ToastUtil;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsBluetoothPrintDriver;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.HsWifiPrintDriver;
import com.rtdriver.driver.LabelBluetoothPrintDriver;
import com.rtdriver.driver.LabelUsbPrintDriver;
import com.rtdriver.driver.LabelWifiPrintDriver;

/**
 * Created by YL01 on 2016/5/9.
 */
public class WifiSettingActivity extends Activity implements Observer {

    private static final String[] WIFI_MMODE = {"STA", "AP"};//spinner的资源
    private int wifi_mode = 0;//wifi模式，0--STA,1--AP

    private int currentMode = -1;//蓝牙/usb/wifi
    private String CON_TYPE = "heatSensitive";//连接种类，热敏/标签

    private Context mContext;
    private LinearLayout back;//返回
    private TextView connect_state;
    private Spinner sp_wifi_set;
    private WifiManager wifiManager;
    private List<ScanResult> scanResults;// 拿到扫描周围wifi结果
    private ListView lv_set_wifi;
    private WifiListAdapter wifi_adapter;// listview的适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wifi);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        init();
        initData();
        initView();
    }

    private void init() {
        // TODO Auto-generated method stub
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
        scanResults = sortScanWifi();
    }

    private void initData() {
        Intent intent = getIntent();
        currentMode = intent.getIntExtra("currentMode", -1);
        CON_TYPE = intent.getStringExtra("CON_TYPE");
    }

    private void initView() {
        back = (LinearLayout) this.findViewById(R.id.back);
        connect_state = (TextView) this.findViewById(R.id.connect_state);
        sp_wifi_set = (Spinner) findViewById(R.id.sp_wifi_set);
        lv_set_wifi = (ListView) findViewById(R.id.lv_set_wifi);
        wifi_adapter = new WifiListAdapter(this, scanResults);
        lv_set_wifi.setAdapter(wifi_adapter);
        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, WIFI_MMODE);
        sp_wifi_set.setAdapter(spinner_adapter);
        sp_wifi_set.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wifi_mode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setListener();
    }

    private void setListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lv_set_wifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScanResult scanResult = scanResults.get(position);
                initSetWiFi(scanResult);
            }
        });
    }

    /**
     * .拿到扫描的到的指定wifi信息,进行分析
     *
     * @param scanResult
     */
    private void initSetWiFi(ScanResult scanResult) {
        int WIFIType = 0;//0无密码，1--WPA-PSK/WPA2-PSK，2--WEP
        int WPAType = 0;//wpa加密类型，0--WPA-PSK，1--WPA2-PSK
        int WPAEncryType = 0;//wpa加密方式，0--AES，1--TPIK
        int WEPType = 0;//wep加密类型,0--OPEN,1--SHARE
        String SSID = scanResult.SSID;//WIFI名称
        if (scanResult.capabilities.contains("WPA2-PSK")) {//加密模式WPA-PSK
            WIFIType = 1;
            WPAType = 1;
            if (scanResult.capabilities.contains("TKIP")) {//wpa加密方式TPIK
                WPAEncryType = 1;
            } else {//wpa加密方式AES
                WPAEncryType = 0;
            }
        } else if (scanResult.capabilities.contains("WPA-PSK")) {//加密模式WPA2-PSK
            WIFIType = 1;
            WPAType = 0;
            if (scanResult.capabilities.contains("TKIP")) {//wpa加密方式TPIK
                WPAEncryType = 1;
            } else {//wpa加密方式AES
                WPAEncryType = 0;
            }
        } else if (scanResult.capabilities.contains("WEP")) {//加密模式WEP
            WIFIType = 2;
            if (scanResult.capabilities.contains("SHARE")) {//wep加密类型SHARE
                WEPType = 1;
            } else {//wep加密类型OPEN
                WEPType = 0;
            }
        } else {//开放无密码
            WIFIType = 0;
        }
        showConfirmDialog(SSID, WIFIType, WPAType, WPAEncryType, WEPType);
    }

    /**
     * 弹出确认连接wifi的dialog
     *
     * @param SSID
     * @param WIFIType
     * @param WPAType
     * @param WPAEncryType
     * @param WEPType
     */
    private void showConfirmDialog(final String SSID, final int WIFIType, final int WPAType, final int WPAEncryType, final int WEPType) {

        Log.d("SSID", "SSID---" + SSID);
        Log.d("WIFIType", "WIFIType---" + WIFIType);
        Log.d("WPAType", "WPAType---" + WPAType);
        Log.d("WPAEncryType", "WPAEncryType---" + WPAEncryType);
        Log.d("WEPType", "WEPType---" + WEPType);
        Log.d("wifi_mode", "wifi_mode---" + wifi_mode);

        LayoutInflater inflater = getLayoutInflater();
        View inflate = inflater.inflate(R.layout.wifi_confirm_dialog, null);
        TextView tx_wifi_name = (TextView) inflate.findViewById(R.id.tx_wifi_name);
        final LinearLayout ll_wifi_pd = (LinearLayout) inflate.findViewById(R.id.ll_wifi_pd);
        final EditText et_wifi_pd = (EditText) inflate.findViewById(R.id.et_wifi_pd);
        tx_wifi_name.setText(SSID);
        if (WIFIType == 0) {
            ll_wifi_pd.setVisibility(View.GONE);
        }
        new AlertDialog.Builder(this).setTitle(R.string.wifi_confirm).setView(inflate).setCancelable(false).setPositiveButton(getResources().getText(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("currentMode-0-------", currentMode+"");
                Log.d("CON_TYPE-0-------", CON_TYPE+"");
                switch (currentMode) {//当前连接的模式
                    case 1://蓝牙
                        switch (CON_TYPE) {
                            case "heatSensitive":
                                HsBluetoothPrintDriver.getInstance().setWifiParam(SSID, et_wifi_pd.getText().toString(), (byte) WIFIType, (byte) WPAType, (byte) WPAEncryType, (byte) WEPType, (byte) wifi_mode);
                                break;
                            case "label":
                                LabelBluetoothPrintDriver.getInstance().setWifiParam(SSID, et_wifi_pd.getText().toString(), (byte) WIFIType, (byte) WPAType, (byte) WPAEncryType, (byte) WEPType, (byte) wifi_mode);
                                break;
                        }
                        break;
                    case 2://usb
                        switch (CON_TYPE) {
                            case "heatSensitive":
                                HsUsbPrintDriver.getInstance().setWifiParam(SSID, et_wifi_pd.getText().toString(), (byte) WIFIType, (byte) WPAType, (byte) WPAEncryType, (byte) WEPType, (byte) wifi_mode);
                                break;
                            case "label":
                                LabelUsbPrintDriver.getInstance().setWifiParam(SSID, et_wifi_pd.getText().toString(), (byte) WIFIType, (byte) WPAType, (byte) WPAEncryType, (byte) WEPType, (byte) wifi_mode);
                                break;
                        }
                        break;
                    case 3://wifi
                        switch (CON_TYPE) {
                            case "heatSensitive":
                                HsWifiPrintDriver.getInstance().setWifiParam(SSID, et_wifi_pd.getText().toString(), (byte) WIFIType, (byte) WPAType, (byte) WPAEncryType, (byte) WEPType, (byte) wifi_mode);
                                break;
                            case "label":
                                LabelWifiPrintDriver.getInstance().setWifiParam(SSID, et_wifi_pd.getText().toString(), (byte) WIFIType, (byte) WPAType, (byte) WPAEncryType, (byte) WEPType, (byte) wifi_mode);
                                break;
                        }
                        break;
                }
                Log.d("click", et_wifi_pd.getText().toString());
                ll_wifi_pd.setVisibility(View.VISIBLE);
            }
        }).setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ll_wifi_pd.setVisibility(View.VISIBLE);
            }
        }).show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        connect_state.setText(RTApplication.getConnStateString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnStateObservable.getInstance().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, final Object data) {
        if (observable == ConnStateObservable.getInstance()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connect_state.setText((CharSequence) data);
                }
            });
        } else if (observable == ConnResultObservable.getInstance()) {
            switch ((int) data) {
                case Contants.FLAG_FAIL_CONNECT:
                    ToastUtil.show(mContext, getString(R.string.fail_connect));
                    break;
                case Contants.FLAG_SUCCESS_CONNECT:
                    ToastUtil.show(mContext, getString(R.string.success_connect));
                    break;
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_wifi_set:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.show();
                scanResults.clear();
                wifiManager.startScan();
                scanResults.addAll(sortScanWifi());
                wifi_adapter.notifyDataSetChanged();
                progressDialog.cancel();
                break;
        }
    }

    /**
     * 整理扫描wifi获得的list
     */
    private List<ScanResult> sortScanWifi() {
        List<ScanResult> scanR = wifiManager.getScanResults();
        int size = scanR.size();
        for (int i = 0; i < size; i++) {
            if (scanR.get(i).SSID == "") {
                scanR.remove(i);
                size--;//因为集合删除元素长度减了
            }
        }
        return scanR;
    }
}
