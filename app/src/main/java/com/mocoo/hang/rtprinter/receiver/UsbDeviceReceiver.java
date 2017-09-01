package com.mocoo.hang.rtprinter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.mocoo.hang.rtprinter.utils.LogUtils;


/**
 * Created by Administrator on 2015/7/8.
 */
public class UsbDeviceReceiver extends BroadcastReceiver {

    private CallBack mCallBack;

    private UsbDeviceReceiver() {

    }

    public UsbDeviceReceiver(CallBack callBack) {
        mCallBack = callBack;
    }

    private final String TAG = getClass().getSimpleName();

    public static final String ACTION_USB_PERMISSION =
            "com.hang.usb.action.USB_PERMISSION";
    private final int VENDOR_ID = 1659;//供应商Id
    private final int PRODUCT_ID = 8963;//产品Id

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "action = " + action);
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null || device.getVendorId() != VENDOR_ID || device.getProductId() != PRODUCT_ID) {
            return;
        }
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                LogUtils.d(TAG, "UsbManager EXTRA_PERMISSION_GRANTED booleanExtra = " + intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false));
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    mCallBack.onPermissionGranted(device);
                } else {
                    LogUtils.d(TAG, "permission denied for device " + device);
                }
            }
        } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            mCallBack.onDeviceAttached(device);
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            mCallBack.onDeviceDetached(device);
        }

    }

    public interface CallBack {
        public void onPermissionGranted(UsbDevice usbDevice);

        public void onDeviceAttached(UsbDevice usbDevice);

        public void onDeviceDetached(UsbDevice usbDevice);
    }
}
