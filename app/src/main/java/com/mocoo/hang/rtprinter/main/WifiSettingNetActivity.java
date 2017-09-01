package com.mocoo.hang.rtprinter.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.main.RTApplication;
import com.mocoo.hang.rtprinter.observable.ConnResultObservable;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.ToastUtil;

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
 * Created by YL01 on 2016/5/10.
 */
public class WifiSettingNetActivity extends Activity implements Observer, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private Context mContext;
    private byte NET_MODE = 0x00;//0是ip，1是dhcp
    private byte DHCP_STATE = 0x00;//0是disable,1是enable

    private int currentMode = -1;
    private String CON_TYPE = "heatSensitive";

    private LinearLayout back;//返回
    private TextView tvConnectState;
    private RadioGroup rd_set_net_mode, rg_set_net_type;
    private LinearLayout ll_wifi_set_net_ip_ip, ll_wifi_set_net_ip_dhcp;
    private EditText et_set_net_ip, et_set_net_mask, et_set_net_gateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wifi_net);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        currentMode = intent.getIntExtra("currentMode", -1);
        CON_TYPE = intent.getStringExtra("CON_TYPE");
    }

    private void initView() {
        back = (LinearLayout) this.findViewById(R.id.back);
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
        rd_set_net_mode = (RadioGroup) findViewById(R.id.rd_set_net_mode);
        rg_set_net_type = (RadioGroup) findViewById(R.id.rg_set_net_type);
        ll_wifi_set_net_ip_ip = (LinearLayout) findViewById(R.id.ll_wifi_set_net_ip_ip);
        ll_wifi_set_net_ip_dhcp = (LinearLayout) findViewById(R.id.ll_wifi_set_net_ip_dhcp);
        et_set_net_ip = (EditText) findViewById(R.id.et_set_net_ip);
        et_set_net_mask = (EditText) findViewById(R.id.et_set_net_mask);
        et_set_net_gateway = (EditText) findViewById(R.id.et_set_net_gateway);
        setListener();
    }

    private void setListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rd_set_net_mode.setOnCheckedChangeListener(this);
        rg_set_net_type.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvConnectState.setText(RTApplication.getConnStateString());
        et_set_net_mask.setText("255.255.255.0");
        et_set_net_gateway.setText("192.168.1.1");
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
                    tvConnectState.setText((CharSequence) data);
                }
            });
        } else if (observable == ConnResultObservable.getInstance()) {
            switch ((int) data) {
                case Contants.FLAG_FAIL_CONNECT:
                    ToastUtil.show(mContext,getString(R.string.fail_connect));
                    break;
                case Contants.FLAG_SUCCESS_CONNECT:
                    ToastUtil.show(mContext, getString(R.string.success_connect));
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_set_net_mode_ip:
                NET_MODE = 0x00;
                ll_wifi_set_net_ip_ip.setVisibility(View.VISIBLE);
                ll_wifi_set_net_ip_dhcp.setVisibility(View.GONE);
                break;
            case R.id.rb_set_net_mode_dhcp:
                NET_MODE = 0x01;
                ll_wifi_set_net_ip_ip.setVisibility(View.GONE);
                ll_wifi_set_net_ip_dhcp.setVisibility(View.VISIBLE);
                break;

            case R.id.rb_set_net_dhcp_dis:
                DHCP_STATE = 0x00;
                break;
            case R.id.rb_set_net_dhcp_en:
                DHCP_STATE = 0x01;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tx_set_net_save_change:
                String S_net_ip = et_set_net_ip.getText().toString();
                String S_net_mask = et_set_net_mask.getText().toString();
                String S_net_gateway = et_set_net_gateway.getText().toString();
                String[] ip_split = S_net_ip.split("\\.");
                String[] mask_split = S_net_mask.split("\\.");
                String[] gateway_split = S_net_gateway.split("\\.");
                int ip_length = ip_split.length;
                int mask_length = mask_split.length;
                int gateway_length = gateway_split.length;
                switch (CON_TYPE) {
                    case "heatSensitive":
                        if ((ip_length == 4) && (mask_length == 4) && (gateway_length == 4)) {
                            switch (currentMode) {
                                //BLUETOOTH_MODE
                                case 1:
                                    HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                                    switch (NET_MODE) {
                                        case 0x00:
                                            hsBluetoothPrintDriver.setStaticIp(S_net_ip, S_net_mask, S_net_gateway);
                                            break;
                                        case 0x01:
                                            if (DHCP_STATE == 0x00) {
                                                hsBluetoothPrintDriver.setDhcp(false);
                                            } else {
                                                hsBluetoothPrintDriver.setDhcp(true);
                                            }
                                            break;
                                    }
                                    break;
                                //USB_MODE
                                case 2:
                                    HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                                    switch (NET_MODE) {
                                        case 0x00:
                                            hsUsbPrintDriver.setStaticIp(S_net_ip, S_net_mask, S_net_gateway);
                                            break;
                                        case 0x01:
                                            if (DHCP_STATE == 0x00) {
                                                hsUsbPrintDriver.setDhcp(false);
                                            } else {
                                                hsUsbPrintDriver.setDhcp(true);
                                            }
                                            break;
                                    }
                                    break;
                                //WIFI_MODE
                                case 3:
                                    HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                                    switch (NET_MODE) {
                                        case 0x00:
                                            hsWifiPrintDriver.setStaticIp(S_net_ip, S_net_mask, S_net_gateway);
                                            break;
                                        case 0x01:
                                            if (DHCP_STATE == 0x00) {
                                                hsWifiPrintDriver.setDhcp(false);
                                            } else {
                                                hsWifiPrintDriver.setDhcp(true);
                                            }
                                            break;
                                    }
                                    break;
                                default:
                                    ToastUtil.show(mContext, "Error");
                                    break;
                            }
                        } else {
                            ToastUtil.show(mContext, "inputError");
                        }
                        break;
                    case "lable":
                        if ((ip_length == 4) && (mask_length == 4) && (gateway_length == 4)) {
                            switch (currentMode) {
                                //BLUETOOTH_MODE
                                case 1:
                                    LabelBluetoothPrintDriver labelBluetoothPrintDriver= LabelBluetoothPrintDriver.getInstance();
                                    switch (NET_MODE) {
                                        case 0x00:
                                            labelBluetoothPrintDriver.setStaticIp(S_net_ip, S_net_mask, S_net_gateway);
                                            break;
                                        case 0x01:
                                            if (DHCP_STATE == 0x00) {
                                                labelBluetoothPrintDriver.setDhcp(false);
                                            } else {
                                                labelBluetoothPrintDriver.setDhcp(true);
                                            }
                                            break;
                                    }
                                    break;
                                //USB_MODE
                                case 2:
                                    LabelUsbPrintDriver labelUsbPrintDriver=LabelUsbPrintDriver.getInstance();
                                    switch (NET_MODE) {
                                        case 0x00:
                                            labelUsbPrintDriver.setStaticIp(S_net_ip, S_net_mask, S_net_gateway);
                                            break;
                                        case 0x01:
                                            if (DHCP_STATE == 0x00) {
                                                labelUsbPrintDriver.setDhcp(false);
                                            } else {
                                                labelUsbPrintDriver.setDhcp(true);
                                            }
                                            break;
                                    }
                                    break;
                                //WIFI_MODE
                                case 3:
                                    LabelWifiPrintDriver labelWifiPrintDriver= LabelWifiPrintDriver.getInstance();
                                    switch (NET_MODE) {
                                        case 0x00:
                                            labelWifiPrintDriver.setStaticIp(S_net_ip, S_net_mask, S_net_gateway);
                                            break;
                                        case 0x01:
                                            if (DHCP_STATE == 0x00) {
                                                labelWifiPrintDriver.setDhcp(false);
                                            } else {
                                                labelWifiPrintDriver.setDhcp(true);
                                            }
                                            break;
                                    }
                                    break;
                                default:
                                    ToastUtil.show(mContext, "Error");
                                    break;
                            }
                        } else {
                            ToastUtil.show(mContext, "inputError");
                        }
                        break;
                }
                finish();
                break;
        }
    }
}
