package com.mocoo.hang.rtprinter.print;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.main.RTApplication;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.DensityUtils;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.rtprinter.utils.SaveMediaFileUtil;
import com.mocoo.hang.rtprinter.utils.ToastUtil;
import com.mocoo.hang.rtprinter.view.ScrollWebView;
import com.mocoo.hang.swipeback.SwipeBackLayout;
import com.mocoo.hang.swipeback.app.SwipeBackActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class WebPagePrintActivity extends SwipeBackActivity implements Observer {

    public static final String BUNDLE_KEY_WEB_URL = "webUrl";
    private static final String HTTP = "http://";

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private SwipeBackLayout mSwipeBackLayout;
    private RelativeLayout title;
    private LinearLayout back;
    private TextView tvConnectState, tvEmpty;
    private FrameLayout flContent;
    private ScrollWebView webView;
    private Bundle mBundle;
    private String mCurrentUrl;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_web_page);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        initView();
        setListener();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
//        mSwipeBackLayout.setEdgeSize(ScreenUtil.getScreenWidth(mContext));
        mSwipeBackLayout.setEdgeSize(DensityUtils.dp2px(mContext, 70));
        loadUrl();
    }



    private void initView() {
        mSwipeBackLayout = getSwipeBackLayout();
        title = (RelativeLayout) this.findViewById(R.id.title);
        back = (LinearLayout) this.findViewById(R.id.back);
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
        flContent = (FrameLayout) this.findViewById(R.id.fl_print_web_page_content);
        tvEmpty = (TextView) this.findViewById(R.id.tv_print_web_page_empty);
        webView = (ScrollWebView) this.findViewById(R.id.wv_print_web_page_web_page);
        progressBar = (ProgressBar) this.findViewById(R.id.pb_print_web_page_progress_bar);

        WebSettings webSettings = webView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mCurrentUrl = url;
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
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
        switch (view.getId()) {
            case R.id.print:
                if (RTApplication.getConnState() == Contants.UNCONNECTED) {
                    ToastUtil.show(mContext, R.string.tip_connect);
                    return;
                }
                if (flContent.getVisibility() == View.GONE) {
                    ToastUtil.show(mContext, R.string.tip_open_web_page);
                    return;
                }
                print();
                break;
        }
    }

    private void loadUrl() {
        mCurrentUrl = "www.baidu.com";
        if (!mCurrentUrl.contains(HTTP)) {
            mCurrentUrl = HTTP.concat(mCurrentUrl);
            LogUtils.v(TAG, "currentUrl = " + mCurrentUrl);
        }
        tvEmpty.setVisibility(View.GONE);
        flContent.setVisibility(View.VISIBLE);
        webView.loadUrl(mCurrentUrl);
    }

    private void print() {

        Bitmap bm;
        switch (RTApplication.mode) {
            case RTApplication.MODE_HS:
                Picture picture = webView.capturePicture();
                bm = BitmapConvertUtil.createBitmapFromPicture(picture);
                hsPrint(bm);
                break;
            case RTApplication.MODE_LABEL:
                flContent.setDrawingCacheEnabled(true);
                bm = flContent.getDrawingCache();
                labelPrint(bm);
                flContent.setDrawingCacheEnabled(false);
                flContent.destroyDrawingCache();
                bm.recycle();
                bm = null;
                System.gc();
                break;
        }

    }

    private void hsPrint(Bitmap bm) {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.SetAlignMode((byte) 0x01);//居中
                if(hsBluetoothPrintDriver.printImage(bm,0)){
                    hsBluetoothPrintDriver.LF();
                    hsBluetoothPrintDriver.CR();
                    hsBluetoothPrintDriver.LF();
                    hsBluetoothPrintDriver.CR();
                    hsBluetoothPrintDriver.LF();
                    hsBluetoothPrintDriver.CR();
                }
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();
                hsUsbPrintDriver.SetAlignMode((byte) 0x01);//居中
                if(hsUsbPrintDriver.printImage(bm,0)){
                    hsUsbPrintDriver.LF();
                    hsUsbPrintDriver.CR();
                    hsUsbPrintDriver.LF();
                    hsUsbPrintDriver.CR();
                    hsUsbPrintDriver.LF();
                    hsUsbPrintDriver.CR();
                }
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();
                hsWifiPrintDriver.SetAlignMode((byte) 0x01);//居中
                if(hsWifiPrintDriver.printImage(bm,0)){
                    hsWifiPrintDriver.LF();
                    hsWifiPrintDriver.CR();
                    hsWifiPrintDriver.LF();
                    hsWifiPrintDriver.CR();
                    hsWifiPrintDriver.LF();
                    hsWifiPrintDriver.CR();
                }
                break;
        }
    }

    private void labelPrint(Bitmap bm) {
        bm = BitmapConvertUtil.decodeSampledBitmapFromBitmap(bm
            , Integer.parseInt(RTApplication.labelWidth) * 8, Integer.parseInt(RTApplication.labelHeight) * 8 - 40);
        int width = (bm.getWidth() + 7) / 8;
        int height = bm.getHeight();
        int X = (Integer.parseInt(RTApplication.labelWidth)*8 - width * 8)/2;
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
                labelUsbPrintDriver.SetPRINT("1", RTApplication.labelCopies);
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
        }    }

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
    public void update(Observable observable,final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectState.setText((CharSequence) data);
            }
        });
    }

}
