package com.mocoo.hang.rtprinter.print;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.dialog.BasicListDialog;
import com.mocoo.hang.rtprinter.main.RTApplication;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.DensityUtils;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.rtprinter.utils.SPUtils;
import com.mocoo.hang.rtprinter.utils.SaveMediaFileUtil;
import com.mocoo.hang.rtprinter.utils.ToastUtil;
import com.mocoo.hang.swipeback.SwipeBackLayout;
import com.mocoo.hang.swipeback.app.SwipeBackActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.rtdriver.driver.BitmapConvertUtil;
import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsBluetoothPrintDriver;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.HsWifiPrintDriver;
import com.rtdriver.driver.LabelBluetoothPrintDriver;
import com.rtdriver.driver.LabelUsbPrintDriver;
import com.rtdriver.driver.LabelWifiPrintDriver;

/**
 * Created by Administrator on 2015/6/3.
 */
public class ImagePrintActivity extends SwipeBackActivity implements Observer {

    public static final String BUNDLE_KEY_IMAGE_URI = "imageUri";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0xd0;
    private static final int ALBUM_IMAGE_ACTIVITY_REQUEST_CODE = 0xd1;
    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private SwipeBackLayout mSwipeBackLayout;
    private TextView tvConnectState;
    private LinearLayout back, llUploadImage;
    private FrameLayout flContent;
    private ImageView ivImage;

    private Uri imageUri;
    private Uri mCurrentImageUri;
    private Bitmap mBitmap;

    private final String X = "10", Y = "10";

    private final int handlerSign = 7173;
    private int state_a = -1;
    private RadioGroup rg_print_image;
    private int mPapertype=0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (data.getInt("flag")) {
                case handlerSign:
                    state_a =data.getInt("state") & 0xFF;
                    if (state_a == 0x80) {
                        Log.d("handleM--------------", "finish");
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_image);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        initData();
        initView();
        setListener();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
//        mSwipeBackLayout.setEdgeSize(ScreenUtil.getScreenWidth(mContext));
        mSwipeBackLayout.setEdgeSize(DensityUtils.dp2px(mContext, 70));
    }
    private void initData() {
        mPapertype = (int) SPUtils.get(mContext, "Papertype",  Contants.TYPE_80);
    }


    private void initView() {
        mSwipeBackLayout = getSwipeBackLayout();
        back = (LinearLayout) this.findViewById(R.id.back);
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
        llUploadImage = (LinearLayout) this.findViewById(R.id.ll_print_image_upload_image);
        flContent = (FrameLayout) this.findViewById(R.id.fl_print_image_content);
        ivImage = (ImageView) this.findViewById(R.id.iv_print_image_image);
        rg_print_image = (RadioGroup) this.findViewById(R.id.rg_print_image);
          switch (mPapertype) {
            case Contants.TYPE_80:
                rg_print_image.check(R.id.rg_print_image_80);
                break;
            case Contants.TYPE_58:
                rg_print_image.check(R.id.rg_print_image_58);
                break;
          }

    }

    private void setListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rg_print_image.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                 case R.id.rg_print_image_58:
                     mPapertype = Contants.TYPE_58;
                     break;
                 case R.id.rg_print_image_80:
                      mPapertype = Contants.TYPE_80;
                      break;
                 }
                SPUtils.put(mContext, "Papertype", mPapertype);
                if (imageUri != null)
                  showImage(imageUri);
            }
        });

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_print_image_upload_image:
                showPictureModeChooseDialog();
                break;
            case R.id.fl_print_image_content:
                showPictureModeChooseDialog();
                break;
            case R.id.print:
                if (RTApplication.getConnState() == Contants.UNCONNECTED) {
                    ToastUtil.show(mContext, R.string.tip_connect);
                    return;
                }
                if (flContent.getVisibility() == View.GONE) {
                    ToastUtil.show(mContext, R.string.tip_upload_image);
                    return;
                }
                print();
                break;
        }
    }

    private void showPictureModeChooseDialog() {
        final BasicListDialog pictureModeChooseDialog = new BasicListDialog();
        ArrayList<String> contentList = new ArrayList<>();
        contentList.add(getString(R.string.picture));
        contentList.add(getString(R.string.album));
        Bundle args = new Bundle();
        args.putString(BasicListDialog.BUNDLE_KEY_TITLE, getString(R.string.choose_picture_position));
        args.putStringArrayList(BasicListDialog.BUNDLE_KEY_CONTENT_LIST, contentList);
        pictureModeChooseDialog.setArguments(args);
        pictureModeChooseDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getAdapter().getItem(position).toString().equals(getString(R.string.picture))) {
                    takeAPicture();
                } else {
                    openAlbum();
                }
                pictureModeChooseDialog.dismiss();
            }
        });
        pictureModeChooseDialog.show(getFragmentManager(), null);
    }

    private void takeAPicture() {
        if (!SaveMediaFileUtil.isExternalStorageWritable()) {
            ToastUtil.show(mContext, R.string.insert_sdcard_tip);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = SaveMediaFileUtil.getOutputMediaFileUri(getApplicationContext(), SaveMediaFileUtil.DIR_CAPTURE, SaveMediaFileUtil.MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ALBUM_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    LogUtils.d(TAG, "imageUri = " + imageUri);
                    showImage(imageUri);
                }
                SimpleCursorTreeAdapter adapter;
                break;
            case ALBUM_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    LogUtils.d(TAG, "getData = " + data.getData());
                    imageUri = data.getData();
                    showImage(imageUri);
                }
                break;
        }
    }

    private void showImage(Uri uri) {
        llUploadImage.setVisibility(View.GONE);
        flContent.setVisibility(View.VISIBLE);
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            System.gc();
        }

         switch (mPapertype) {
             case Contants.TYPE_80:
                 mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(mContext, uri, 72 * 8, 4000);
                 break;
             case Contants.TYPE_58:
                 mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(mContext, uri, 48 * 8, 4000);
                 break;
         }
/*
        switch (RTApplication.mode) {
            case RTApplication.MODE_HS:
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(mContext, uri, 72 * 8, 4000);
                break;
            case RTApplication.MODE_LABEL:
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(mContext, uri, 48 * 8, 4000);
                break;
        }
        */


        LogUtils.d(TAG, "mBitmap getWidth = " + mBitmap.getWidth());
        LogUtils.d(TAG, "mBitmap getHeight = " + mBitmap.getHeight());
        ivImage.setImageBitmap(mBitmap);
        mCurrentImageUri = uri;
    }

    private void print() {

        switch (RTApplication.mode) {
            case RTApplication.MODE_HS:
                hsPrint();
                break;
            case RTApplication.MODE_LABEL:
                labelPrint();
                break;
        }

    }

    private void hsPrint() {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.SetAlignMode((byte) 0x01);//居中
                if (hsBluetoothPrintDriver.printImage(mBitmap, mPapertype)) {
                    hsBluetoothPrintDriver.LF();
                    hsBluetoothPrintDriver.CR();
                    hsBluetoothPrintDriver.LF();
                    hsBluetoothPrintDriver.CR();
                    hsBluetoothPrintDriver.LF();
                    hsBluetoothPrintDriver.CR();
                }
                hsBluetoothPrintDriver.StatusInquiryFinish(handlerSign, handler);

                hsBluetoothPrintDriver.BT_Write("Example: print image success");
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.BT_Write("Example: test");
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.BT_Write("Example: 0123456789");
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
                if (hsUsbPrintDriver.printImage(mBitmap, mPapertype)) {
                    hsUsbPrintDriver.LF();
                    hsUsbPrintDriver.CR();
                    hsUsbPrintDriver.LF();
                    hsUsbPrintDriver.CR();
                    hsUsbPrintDriver.LF();
                    hsUsbPrintDriver.CR();
                }
                hsUsbPrintDriver.USB_Write("Example: print image success");
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.USB_Write("Example: test");
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.USB_Write("Example: 0123456789");
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
                if (hsWifiPrintDriver.printImage(mBitmap, Contants.TYPE_58)) {
                    hsWifiPrintDriver.LF();
                    hsWifiPrintDriver.CR();
                    hsWifiPrintDriver.LF();
                    hsWifiPrintDriver.CR();
                    hsWifiPrintDriver.LF();
                    hsWifiPrintDriver.CR();
                }
                hsWifiPrintDriver.StatusInquiryFinish(handlerSign, handler);

                hsWifiPrintDriver.WIFI_Write("Example: print image success");
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.WIFI_Write("Example: test");
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.WIFI_Write("Example: 0123456789");
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                break;
        }
    }

    private void labelPrint() {
        Bitmap bm = BitmapConvertUtil.decodeSampledBitmapFromBitmap(mBitmap
                , Integer.parseInt(RTApplication.labelWidth) * 8, Integer.parseInt(RTApplication.labelHeight) * 8 - 40);
        int width = (bm.getWidth() + 7) / 8;
        int height = bm.getHeight();
        int X = (Integer.parseInt(RTApplication.labelWidth) * 8 - width * 8) / 2;
        LogUtils.d(TAG, "width = " + width);
        LogUtils.d(TAG, "height = " + height);
        LogUtils.d(TAG, "X = " + X);
        byte[] data = BitmapConvertUtil.convert2(bm);
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                LabelBluetoothPrintDriver labelBluetoothPrintDriver = LabelBluetoothPrintDriver.getInstance();
                labelBluetoothPrintDriver.Begin();
                labelBluetoothPrintDriver.SetCLS();
                labelBluetoothPrintDriver.SetSize(RTApplication.labelWidth, RTApplication.labelHeight);
                labelBluetoothPrintDriver.drawBitMap(String.valueOf(X), "20", String.valueOf(width), String.valueOf(height), "0", data);
                labelBluetoothPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelBluetoothPrintDriver.endPro();
                break;
            case Contants.CONNECTED_BY_USB:
                LabelUsbPrintDriver labelUsbPrintDriver = LabelUsbPrintDriver.getInstance();
                labelUsbPrintDriver.Begin();
                labelUsbPrintDriver.SetCLS();
                labelUsbPrintDriver.SetSize(RTApplication.labelWidth, RTApplication.labelHeight);
                labelUsbPrintDriver.drawBitMap(String.valueOf(X), "20", String.valueOf(width), String.valueOf(height), "0", data);
                labelUsbPrintDriver.SetPRINT("1", RTApplication.labelCopies);//RTApplication.labelCopies
                labelUsbPrintDriver.endPro();
                break;
            case Contants.CONNECTED_BY_WIFI:
                LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
                labelWifiPrintDriver.Begin();
                labelWifiPrintDriver.SetCLS();
                labelWifiPrintDriver.SetSize(RTApplication.labelWidth, RTApplication.labelHeight);
                labelWifiPrintDriver.drawBitMap(String.valueOf(X), "20", String.valueOf(width), String.valueOf(height), "0", data);
                labelWifiPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelWifiPrintDriver.endPro();
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
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            System.gc();
        }
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
