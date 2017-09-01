package com.mocoo.hang.rtprinter.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.adapter.UsbDeviceAdapter;
import com.mocoo.hang.rtprinter.receiver.UsbDeviceReceiver;
import com.mocoo.hang.rtprinter.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2015/6/10.
 */
public class
        UsbDeviceChooseDialog extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private ListView lvContent;
    private TextView tvEmpty;
    private AdapterView.OnItemClickListener mListener;
    private List<UsbDevice> mList;
    private UsbDeviceAdapter mAdapter;
    private UsbManager mUsbManager;
    private UsbDeviceReceiver mUsbReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_choose_usb_device, null);
        initView(view);
        setListener();
        initData();
        setAdapter();
        registerUsbReceiver();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view).setCancelable(true).setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    private void registerUsbReceiver() {
        mUsbReceiver = new UsbDeviceReceiver(new UsbDeviceReceiver.CallBack() {
            @Override
            public void onPermissionGranted(UsbDevice usbDevice) {

            }

            @Override
            public void onDeviceAttached(UsbDevice usbDevice) {
                mList.add(usbDevice);
                mAdapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.GONE);
            }

            @Override
            public void onDeviceDetached(UsbDevice usbDevice) {
                mList.remove(usbDevice);
                mAdapter.notifyDataSetChanged();
                if(mList.size() == 0){
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReceiver, intentFilter);
    }

    private void initView(View view) {
        lvContent = (ListView) view.findViewById(R.id.lv_dialog_choose_usb_device);
        tvEmpty = (TextView) view.findViewById(R.id.tv_dialog_choose_usb_device_empty);
    }

    private void setListener() {
        lvContent.setOnItemClickListener(mListener);
    }

    private void initData() {
        mList = new ArrayList<>();
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        LogUtils.d(TAG, "deviceList size = " + deviceList.size());
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
/*        if(deviceIterator.hasNext()){
            deviceIterator.next();
        }*/
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            LogUtils.d(TAG,"device getDeviceName" + device.getDeviceName());
            LogUtils.d(TAG,"device getVendorId" + device.getVendorId());
            LogUtils.d(TAG,"device getProductId" + device.getProductId());
            mList.add(device);
        }
        if(mList.size()==0){
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

/*    private void initData() {
        mList = new ArrayList<>();
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        LogUtils.v(TAG, "accessories length = " + accessories.length);
        for (int i = 0; i < accessories.length; i++) {
            UsbAccessory accessory = accessories[i];
            LogUtils.v(TAG, "accessories toString = " + accessory.toString());
        }
    }*/

    private void setAdapter() {
        mAdapter = new UsbDeviceAdapter(mContext, mList);
        lvContent.setAdapter(mAdapter);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        LogUtils.v(TAG,"onDismiss");
        mContext.unregisterReceiver(mUsbReceiver);
    }


}
