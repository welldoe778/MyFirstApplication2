package com.mocoo.hang.rtprinter.templet;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.main.RTApplication;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.DensityUtils;
import com.mocoo.hang.rtprinter.utils.ToastUtil;
import com.mocoo.hang.swipeback.SwipeBackLayout;
import com.mocoo.hang.swipeback.app.SwipeBackActivity;

import java.util.Observable;
import java.util.Observer;

import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsBluetoothPrintDriver;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.HsWifiPrintDriver;

/**
 * Created by Administrator on 2015/6/2.
 */
public class HsTempletActivity1 extends SwipeBackActivity implements Observer{

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private SwipeBackLayout mSwipeBackLayout;

    private RelativeLayout title;
    private LinearLayout back;
    private TextView tvConnectState;
    private EditText etTitle,etContent1,etContent2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hs_templet1);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        initView();
        setCustomSelectionActionModeCallback();
        setListener();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
//        mSwipeBackLayout.setEdgeSize(ScreenUtil.getScreenWidth(mContext));
        mSwipeBackLayout.setEdgeSize(DensityUtils.dp2px(mContext, 70));
    }

    private void initView() {
        title = (RelativeLayout) this.findViewById(R.id.title);
        back = (LinearLayout) this.findViewById(R.id.back);
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
        etTitle = (EditText) this.findViewById(R.id.et_hs_templet1_title);
        etContent1 = (EditText) this.findViewById(R.id.et_hs_templet1_content1);
        etContent2 = (EditText) this.findViewById(R.id.et_hs_templet1_content2);
        mSwipeBackLayout = getSwipeBackLayout();
    }

    private void setCustomSelectionActionModeCallback() {
        ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                title.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                title.setVisibility(View.VISIBLE);
            }
        };
        etTitle.setCustomSelectionActionModeCallback(actionModeCallback);
        etContent1.setCustomSelectionActionModeCallback(actionModeCallback);
        etContent2.setCustomSelectionActionModeCallback(actionModeCallback);
    }

    private void setListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    public void onClick(View view) {

        String title = etTitle.getText().toString();
        String content1 = etContent1.getText().toString();
        String content2 = etContent2.getText().toString();
        switch (view.getId()) {
            case R.id.print:
                if (RTApplication.getConnState() == Contants.UNCONNECTED) {
                    ToastUtil.show(mContext, R.string.tip_connect);
                    return;
                }
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content1) || TextUtils.isEmpty(content2)) {
                    ToastUtil.show(mContext, R.string.tip_input_text);
                    return;
                }
                print(title,content1,content2);
                break;
        }
    }


    private void print(String title,String content1,String content2) {
        switch (RTApplication.mode){
            case RTApplication.MODE_HS:
                hsPrint(title,content1,content2);
                break;
            case RTApplication.MODE_LABEL:
                labelPrint(title,content1,content2);
                break;
        }
    }

    private void hsPrint(String title,String content1,String content2) {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.SetAlignMode((byte) 0x01);//居中
                hsBluetoothPrintDriver.SetCharacterPrintMode((byte) (0x08 | 0x10 | 0x20));//粗体、倍高、倍宽
                hsBluetoothPrintDriver.SelChineseCodepage();
                hsBluetoothPrintDriver.SetChineseCharacterMode((byte) (0x04|0x08));//倍宽、倍高
                hsBluetoothPrintDriver.BT_Write(title);
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.SetCharacterPrintMode((byte) 0x00);//解除粗体、倍高、倍宽
                hsBluetoothPrintDriver.SetChineseCharacterMode((byte) 0x00);//解除倍宽、倍高
                hsBluetoothPrintDriver.BT_Write(content1);
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.BT_Write(content2);
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
                hsUsbPrintDriver.SetAlignMode((byte) 0x01);//居中
                hsUsbPrintDriver.SetCharacterPrintMode((byte) (0x08 | 0x10 | 0x20));//粗体、倍高、倍宽
                hsUsbPrintDriver.SelChineseCodepage();
                hsUsbPrintDriver.SetChineseCharacterMode((byte) (0x04 | 0x08));//倍宽、倍高
                hsUsbPrintDriver.USB_Write(title);
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.SetCharacterPrintMode((byte) 0x00);//解除粗体、倍高、倍宽
                hsUsbPrintDriver.SetChineseCharacterMode((byte) 0x00);//解除倍宽、倍高
                hsUsbPrintDriver.USB_Write(content1);
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.USB_Write(content2);
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
                hsWifiPrintDriver.SetAlignMode((byte) 0x01);//居中
                hsWifiPrintDriver.SetCharacterPrintMode((byte) (0x08 | 0x10 | 0x20));//粗体、倍高、倍宽
                hsWifiPrintDriver.SelChineseCodepage();
                hsWifiPrintDriver.SetChineseCharacterMode((byte) (0x04 | 0x08));//倍宽、倍高
                hsWifiPrintDriver.WIFI_Write(title);
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.SetCharacterPrintMode((byte) 0x00);//解除粗体、倍高、倍宽
                hsWifiPrintDriver.SetChineseCharacterMode((byte) 0x00);//解除倍宽、倍高
                hsWifiPrintDriver.WIFI_Write(content1);
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.WIFI_Write(content2);
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

    private void labelPrint(String title,String content1,String content2) {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:

                break;
            case Contants.CONNECTED_BY_USB:

                break;
            case Contants.CONNECTED_BY_WIFI:

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
        ConnStateObservable.getInstance().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectState.setText((CharSequence) data);
            }
        });
    }

}
