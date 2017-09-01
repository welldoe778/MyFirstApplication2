package com.mocoo.hang.rtprinter.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.adapter.WifiDeviceAdapter;
import com.mocoo.hang.rtprinter.interfaces.CustomDialogInterface;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.rtprinter.utils.ToastUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/6/10.
 */
public class WifiDeviceChooseDialog extends DialogFragment {

    public static final String BUNDLE_KEY_ADDRESS = "address";
    private final String TAG = getClass().getSimpleName();
    private final String EXTRA_KEY_DEVICE_IP = "deviceIp";
    private final String EXTRA_KEY_DEVICE_PORT = "devicePort";
    private LinearLayout llRadioEdit, llRadioChoose;
    private EditText etIp, etPort;
    private TextView tvClickToChoose, tvFoundDeviceEmpty;
    private ProgressBar progressBar;
    private ListView lvFoundDevices;

    private Context mContext;
    private AlertDialog mDialog;
    private CustomDialogInterface.onPositiveClickListener mListener;
    private List<String> foundDeviceList;
    private WifiDeviceAdapter foundDeviceAdapter;
    private HandlerThread mSearchThread, mFoundThread;
    private Handler mSearchHandler;
    private FoundHandler mFoundHandler;
    private WifiDeviceReceiver mWifiReceiver;
    private IntentFilter mWifiIntentFilter;

    private String mAddress;
    private boolean mSearchInited = false;// 若为true表示一键搜索按钮已按下过，数据已初始化
    private boolean mSearching = false;// 若为true表示正在搜索

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddress = getArguments().getString(BUNDLE_KEY_ADDRESS, "");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_choose_wifi_device, null);
        initView(view);
        setListener();
        llRadioEdit.performClick();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view).setCancelable(true).setPositiveButton(R.string.confirm, null).
                setNegativeButton(R.string.cancel, null);
        mDialog = builder.create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setDialogButtonListener();
            }
        });
        return mDialog;
    }

    private void initView(View view) {
        llRadioEdit = (LinearLayout) view.findViewById(R.id.ll_dialog_choose_wifi_device_radio_edit);
        llRadioChoose = (LinearLayout) view.findViewById(R.id.ll_dialog_choose_wifi_device_radio_choose);
        etIp = (EditText) view.findViewById(R.id.et_dialog_choose_wifi_device_ip);
        etPort = (EditText) view.findViewById(R.id.et_dialog_choose_wifi_device_port);
        tvClickToChoose = (TextView) view.findViewById(R.id.tv_dialog_choose_wifi_device_click_to_choose);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_dialog_choose_wifi_device_progress_bar);
        lvFoundDevices = (ListView) view.findViewById(R.id.lv_dialog_choose_wifi_device_found_devices);
        tvFoundDeviceEmpty = (TextView) view.findViewById(R.id.tv_dialog_choose_wifi_device_found_devices_empty);

        lvFoundDevices.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        Log.d("log", mAddress);
        if (!TextUtils.isEmpty(mAddress)) {
            String[] temp = mAddress.split(":");
            etIp.setText(temp[0]);
            etPort.setText(temp[1]);
        }
    }

    private void setListener() {
        llRadioEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llRadioEdit.setSelected(true);
                llRadioChoose.setSelected(false);
                etIp.setEnabled(true);
                etPort.setEnabled(true);
                tvClickToChoose.setEnabled(false);
                lvFoundDevices.setEnabled(false);
                if (mSearching) {
                    mContext.unregisterReceiver(mWifiReceiver);
                    mSearching = false;
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        llRadioChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llRadioEdit.setSelected(false);
                llRadioChoose.setSelected(true);
                etIp.setEnabled(false);
                etPort.setEnabled(false);
                tvClickToChoose.setEnabled(true);
                lvFoundDevices.setEnabled(true);
            }
        });
        tvClickToChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchInited) {
                    foundDeviceList.clear();
                    foundDeviceAdapter.notifyDataSetChanged();
                } else {
                    initSearch();
                    mSearchInited = true;
                }
                mSearching = true;
                tvFoundDeviceEmpty.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                tvClickToChoose.setEnabled(false);
                mContext.registerReceiver(mWifiReceiver, mWifiIntentFilter);
                Message foundMessage = mFoundHandler.obtainMessage();
                mFoundHandler.sendMessage(foundMessage);
                Message searchMessage = mSearchHandler.obtainMessage();
                mSearchHandler.sendMessage(searchMessage);
            }
        });

    }

    private void setDialogButtonListener() {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address;
                if (llRadioEdit.isSelected()) {
                    String ip = etIp.getText().toString().trim();
                    if (TextUtils.isEmpty(ip)) {
                        ToastUtil.show(mContext, R.string.tip_ip_input);
                        return;
                    }
                    String port = etPort.getText().toString().trim();
                    if (TextUtils.isEmpty(port)) {
                        ToastUtil.show(mContext, R.string.tip_port_input);
                        return;
                    }
                    String regex = "(\\d{1,3}\\.){3}\\d{1,3}";
                    if (!ip.matches(regex)) {
                        ToastUtil.show(mContext, R.string.tip_input_correct_ip);
                        return;
                    }
                    address = ip + ":" + port;
                } else {
                    if (foundDeviceList == null || foundDeviceList.size() == 0) {
                        mDialog.dismiss();
                        return;
                    }
                    int checkedItemPosition = lvFoundDevices.getCheckedItemPosition();
                    if (checkedItemPosition == ListView.INVALID_POSITION) {
                        ToastUtil.show(mContext, R.string.tip_choose_address);
                        return;
                    } else {
                        address = lvFoundDevices.getAdapter().getItem(checkedItemPosition).toString();
                    }
                }
                mListener.onDialogPositiveClick(address);
                mDialog.dismiss();
            }
        });
    }

    private void initSearch() {
        foundDeviceList = new ArrayList<>();
        foundDeviceAdapter = new WifiDeviceAdapter(mContext, foundDeviceList);
        lvFoundDevices.setAdapter(foundDeviceAdapter);
        mWifiReceiver = new WifiDeviceReceiver();
        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiDeviceReceiver.ACTION_FOUND);
        mWifiIntentFilter.addAction(WifiDeviceReceiver.ACTION_FINISH);
        mFoundThread = new HandlerThread("foundThread", Process.THREAD_PRIORITY_BACKGROUND);
        mSearchThread = new HandlerThread("searchThread", Process.THREAD_PRIORITY_BACKGROUND);
        mFoundThread.start();
        mSearchThread.start();
        mFoundHandler = new FoundHandler(mFoundThread.getLooper());
        mSearchHandler = new SearchHandler(mSearchThread.getLooper());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mSearching) {
            mContext.unregisterReceiver(mWifiReceiver);
        }
        if (mFoundHandler != null) {
            mFoundHandler.close();
        }
    }

    public void setOnClickListener(CustomDialogInterface.onPositiveClickListener listener) {
        mListener = listener;
    }

    private class SearchHandler extends Handler {

        private DatagramSocket ds;

        public SearchHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            LogUtils.d(TAG, "SearchHandler thread name = " + Thread.currentThread().getName());
            try {
                ds = new DatagramSocket(6000);
                InetAddress inetAddress = InetAddress.getByName("255.255.255.255");
                byte[] buf = "RTSEARCH".getBytes();
                DatagramPacket dp = new DatagramPacket(buf, 8, inetAddress, 3000);
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                }
            }
        }
    }

    private class FoundHandler extends Handler {

        private DatagramSocket ds;

        public FoundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            LogUtils.v(TAG, "FoundHandler thread name = " + Thread.currentThread().getName());
            byte[] buf = new byte[1024];
            try {
                while (mSearching) {
                    synchronized (this) {
                        DatagramPacket dp = new DatagramPacket(buf, buf.length);
                        if(ds == null){
                            ds = new DatagramSocket(6001);
                            ds.setSoTimeout(3 * 1000);
                        }
                        ds.receive(dp);
                        byte[] data = dp.getData();

                        for (int i = 0; i < data.length; i++) {
                            byte b = data[i];
                            LogUtils.d(TAG, "b = " + b);
                        }
                        LogUtils.d(TAG, "data = " + new String(data));
                        if (data.length > 7 && new String(data, 0, 7).equals("RTFOUND")) {
                            StringBuilder sb = new StringBuilder();
                            //读取ip和端口号
                            for (int i = 0; i < 4; i++) {
                                sb.append(data[13 + i]);
                                sb.append(".");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            String ip = sb.toString();
                            int port = data[25] << 8 + data[26];
                            Intent intent = new Intent();
                            intent.setAction(WifiDeviceReceiver.ACTION_FOUND);
                            intent.putExtra(EXTRA_KEY_DEVICE_IP, ip);
                            intent.putExtra(EXTRA_KEY_DEVICE_PORT, port);
                            mContext.sendBroadcast(intent);
                        }
                    }
                }
            } catch (IOException e) {
                LogUtils.d(TAG, "IOException e getMessage = " + e.getMessage());
                Intent intent = new Intent();
                intent.setAction(WifiDeviceReceiver.ACTION_FINISH);
                mContext.sendBroadcast(intent);
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                    ds = null;
                }
            }
        }

        public void close() {
            if (ds != null && !ds.isClosed()) {
                ds.close();
            }
        }
    }

    private class WifiDeviceReceiver extends BroadcastReceiver {

        private static final String ACTION_FOUND = "com.hang.wifi.action.found";
        private static final String ACTION_FINISH = "com.hang.wifi.action.finish";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.v(TAG, "action = " + action);
            if (ACTION_FOUND.equals(action)) {
                String address = intent.getStringExtra(EXTRA_KEY_DEVICE_IP) + ":" + intent.getIntExtra(EXTRA_KEY_DEVICE_PORT, -1);
                foundDeviceList.add(address);
                foundDeviceAdapter.notifyDataSetChanged();
            } else if (ACTION_FINISH.equals(action)) {
                mContext.unregisterReceiver(mWifiReceiver);
                mSearching = false;
                progressBar.setVisibility(View.GONE);
                tvClickToChoose.setEnabled(true);
                LogUtils.v(TAG, "foundDeviceList size = " + foundDeviceList.size());
                if (foundDeviceList.size() == 0) {
                    tvFoundDeviceEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
