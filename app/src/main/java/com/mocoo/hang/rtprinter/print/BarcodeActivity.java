package com.mocoo.hang.rtprinter.print;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.adapter.BarcodeAdapter;
import com.mocoo.hang.rtprinter.main.RTApplication;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.DensityUtils;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.swipeback.SwipeBackLayout;
import com.mocoo.hang.swipeback.Utils;
import com.mocoo.hang.swipeback.app.SwipeBackActivityBase;
import com.mocoo.hang.swipeback.app.SwipeBackActivityHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.rtdriver.driver.BarcodeType;

/**
 * Created by Administrator on 2015/6/2.
 */
public class BarcodeActivity extends ListActivity implements Observer, SwipeBackActivityBase {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private SwipeBackActivityHelper mHelper;

    private SwipeBackLayout mSwipeBackLayout;
    private LinearLayout back;
    private TextView tvConnectState;
    private List<String> tagList;
    private List<String> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        initView();
        setAdapter();
        setListener();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
//        mSwipeBackLayout.setEdgeSize(ScreenUtil.getScreenWidth(mContext));
        mSwipeBackLayout.setEdgeSize(DensityUtils.dp2px(mContext, 70));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    private void initView() {
        mSwipeBackLayout = getSwipeBackLayout();
        back = (LinearLayout) this.findViewById(R.id.back);
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
    }

    public void setAdapter() {
        tagList = Arrays.asList(mContext.getResources().getStringArray(R.array.barcode_tag));
        switch (RTApplication.mode){
            case RTApplication.MODE_HS:
                itemList = Arrays.asList(mContext.getResources().getStringArray(R.array.hs_barcode_item));
                break;
            case RTApplication.MODE_LABEL:
                itemList = Arrays.asList(mContext.getResources().getStringArray(R.array.label_barcode_item));
                break;
        }
        LogUtils.v(TAG, "tagList = " + tagList);
        LogUtils.v(TAG, "itemList = " + itemList);
        BarcodeAdapter adapter = new BarcodeAdapter(mContext, itemList, tagList);
        setListAdapter(adapter);
    }

    private void setListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String barcodeType = l.getAdapter().getItem(position).toString();
        if (getString(R.string.UPC_A).equals(barcodeType)) {
            barcodeType = BarcodeType.UPC_A.name();
        } else if (getString(R.string.UPC_E).equals(barcodeType)) {
            barcodeType = BarcodeType.UPC_E.name();
        } else if (getString(R.string.EAN13).equals(barcodeType)) {
            barcodeType = BarcodeType.EAN13.name();
        } else if (getString(R.string.EAN8).equals(barcodeType)) {
            barcodeType = BarcodeType.EAN8.name();
        } else if (getString(R.string.CODE39).equals(barcodeType)) {
            barcodeType = BarcodeType.CODE39.name();
        } else if (getString(R.string.ITF).equals(barcodeType)) {
            barcodeType = BarcodeType.ITF.name();
        } else if (getString(R.string.CODABAR).equals(barcodeType)) {
            barcodeType = BarcodeType.CODABAR.name();
        } else if (getString(R.string.CODE93).equals(barcodeType)) {
            barcodeType = BarcodeType.CODE93.name();
        } else if (getString(R.string.CODE128).equals(barcodeType)) {
            barcodeType = BarcodeType.CODE128.name();
        } else if (getString(R.string.QR_CODE).equals(barcodeType)) {
            barcodeType = BarcodeType.QR_CODE.name();
        }
        Intent intent = new Intent(mContext, BarcodePrintActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BarcodePrintActivity.BUNDLE_KEY_BARCODE_TYPE, barcodeType);
        intent.putExtras(bundle);
        startActivity(intent);
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
    public void update(Observable observable,final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectState.setText((CharSequence) data);
            }
        });
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {

    }

}
