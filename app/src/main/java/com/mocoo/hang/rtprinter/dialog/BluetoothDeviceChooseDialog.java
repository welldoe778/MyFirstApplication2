package com.mocoo.hang.rtprinter.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.adapter.BluetoothDeviceAdapter;
import com.mocoo.hang.rtprinter.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/6/30.
 */
public class BluetoothDeviceChooseDialog extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private onDeviceItemClickListener mListener;
    private ListView lvPairedDevices, lvFoundDevices;
    private TextView tvPairedDeviceEmpty, tvFoundDeviceEmpty, tvSearchDevice;
    private ProgressBar progressBar;
    private BroadcastReceiver mBluetoothReceiver;
    private IntentFilter mBluetoothIntentFilter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDeviceAdapter pairedDeviceAdapter, foundDeviceAdapter;
    private List<BluetoothDevice> pairedDeviceList, foundDeviceList;
    private boolean mSearchInited = false;// 若为true表示搜索设备按钮已按下过，数据已初始化
    private boolean mRegistered = false;// 若为true表示接收器已注册

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_choose_bluetooth_device, null);
        initView(view);
        setListener();
        initData();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view).setCancelable(true).setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    private void initView(View view) {
        lvPairedDevices = (ListView) view.findViewById(R.id.lv_dialog_choose_bluetooth_device_paired_devices);
        lvFoundDevices = (ListView) view.findViewById(R.id.lv_dialog_choose_bluetooth_device_found_devices);
        tvPairedDeviceEmpty = (TextView) view.findViewById(R.id.tv_dialog_choose_bluetooth_device_paired_devices_empty);
        tvFoundDeviceEmpty = (TextView) view.findViewById(R.id.tv_dialog_choose_bluetooth_device_found_devices_empty);
        tvSearchDevice = (TextView) view.findViewById(R.id.tv_dialog_choose_bluetooth_device_search_device);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_dialog_choose_bluetooth_device_progress_bar);
    }

    private void setListener() {
        tvSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSearchDevice.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                tvFoundDeviceEmpty.setVisibility(View.GONE);
                if (mSearchInited) {
                    foundDeviceList.clear();
                    foundDeviceAdapter.notifyDataSetChanged();
                } else {
                    foundDeviceList = new ArrayList<BluetoothDevice>();
                    foundDeviceAdapter = new BluetoothDeviceAdapter(mContext, foundDeviceList);
                    lvFoundDevices.setAdapter(foundDeviceAdapter);
                    mBluetoothReceiver = new BluetoothDeviceReceiver();
                    mBluetoothIntentFilter = new IntentFilter();
                    mBluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                    mBluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    mSearchInited = true;
                }
                mContext.registerReceiver(mBluetoothReceiver, mBluetoothIntentFilter);
                mRegistered = true;
                mBluetoothAdapter.startDiscovery();
            }
        });
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                if (mRegistered) {
                    mContext.unregisterReceiver(mBluetoothReceiver);
                    mRegistered = false;
                }
                mListener.onDeviceItemClick((BluetoothDevice) parent.getAdapter().getItem(position));
                getDialog().dismiss();
            }
        });
        lvFoundDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                if (mRegistered) {
                    mContext.unregisterReceiver(mBluetoothReceiver);
                    mRegistered = false;
                }
                mListener.onDeviceItemClick((BluetoothDevice) parent.getAdapter().getItem(position));
                getDialog().dismiss();
            }
        });
    }

    private void initData() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDeviceList = new ArrayList<>(mBluetoothAdapter.getBondedDevices());
        if (pairedDeviceList.size() == 0) {
            tvPairedDeviceEmpty.setVisibility(View.VISIBLE);
        }
        pairedDeviceAdapter = new BluetoothDeviceAdapter(mContext, pairedDeviceList);
        lvPairedDevices.setAdapter(pairedDeviceAdapter);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mBluetoothAdapter.cancelDiscovery();
        if (mRegistered) {
            mContext.unregisterReceiver(mBluetoothReceiver);
        }
    }

    public void setOnDeviceItemClickListener(onDeviceItemClickListener listener) {
        mListener = listener;
    }

    public interface onDeviceItemClickListener {
        public void onDeviceItemClick(BluetoothDevice device);
    }

    private class BluetoothDeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "action = " + action);
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC)
                    return;
                LogUtils.d(TAG, "!foundDeviceList.contains(device) = " + !foundDeviceList.contains(device));
                if (!foundDeviceList.contains(device)) {
                    foundDeviceList.add(device);
                    foundDeviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mBluetoothAdapter.cancelDiscovery();
                mContext.unregisterReceiver(mBluetoothReceiver);
                mRegistered = false;
                tvSearchDevice.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (foundDeviceList.size() == 0) {
                    tvFoundDeviceEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
