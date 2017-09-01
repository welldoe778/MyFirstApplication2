package com.mocoo.hang.rtprinter.templet;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.main.RTApplication;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.DensityUtils;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.rtprinter.utils.ToastUtil;
import com.mocoo.hang.swipeback.SwipeBackLayout;
import com.mocoo.hang.swipeback.app.SwipeBackActivity;

import java.util.Observable;
import java.util.Observer;

import com.rtdriver.driver.Contants;
import com.rtdriver.driver.LabelBluetoothPrintDriver;
import com.rtdriver.driver.LabelUsbPrintDriver;
import com.rtdriver.driver.LabelWifiPrintDriver;

/**
 * Created by Administrator on 2015/6/2.
 */
public class LabelTempletActivity1 extends SwipeBackActivity implements Observer {

    private final String TAG = getClass().getSimpleName();
    public static final String BUNDLE_KEY_TITLE = "title";
    public static final String BUNDLE_KEY_CONTENT1 = "content1";
    public static final String BUNDLE_KEY_CONTENT2 = "content2";

    private Context mContext;
    private SwipeBackLayout mSwipeBackLayout;

    private RelativeLayout title;
    private LinearLayout back;
    private TextView tvConnectState;
    private FrameLayout flContent;
    private EditText etTitle, etContent1, etContent2;

    private String[] fontArr;
    private int fontIndex = 0;//字体索引
    private String mLabelWidth = "60", mLabelHeight = "40";
    private int lineWidth;//表示每行文字的最大宽度
    private int multiple = 2;
    private int contentDistanceY, titleDistanceY;//打印机文本字体换行间隔
    private int contentLetterWidth, titleLetterWidth;//打印机文本字体宽度
    private int contentLetterCount, titleLetterCount;//单行字母最大个数
    private float contentTextSize, titleTextSize;//字体大小

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_templet1);
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
        flContent = (FrameLayout) this.findViewById(R.id.fl_label_templet1_content);
        etTitle = (EditText) this.findViewById(R.id.et_label_templet1_title);
        etContent1 = (EditText) this.findViewById(R.id.et_label_templet1_content1);
        etContent2 = (EditText) this.findViewById(R.id.et_label_templet1_content2);
        mSwipeBackLayout = getSwipeBackLayout();

        TextView tvSpecification = (TextView) this.findViewById(R.id.tv_label_templet1_specification);
        tvSpecification.setText(getString(R.string.specification) + "60mm * 40mm");
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/SimYou.TTF");//幼圆字体
        etTitle.setTypeface(typeface);
        etContent1.setTypeface(typeface);
        etContent2.setTypeface(typeface);
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
        etTitle.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                etTitle.getViewTreeObserver().removeOnPreDrawListener(this);
                lineWidth = etTitle.getWidth() - etTitle.getPaddingLeft() - etTitle.getPaddingRight();
                LogUtils.d(TAG, "lineWidth = " + lineWidth);
                initTextSetting();
                return true;
            }
        });
        etTitle.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                StringBuilder sb = new StringBuilder();
                sb.append(dest.subSequence(0, dstart));
                sb.append(source);
                sb.append(dest.subSequence(dend, dest.length()));
                LogUtils.d(TAG, "sb = " + sb.toString());
                String[] texts = sb.toString().split("\\n");
                for (String text : texts) {
                    if (etTitle.getPaint().measureText(text) > lineWidth) {
                        ToastUtil.show(mContext, R.string.tip_label_width_overstep);
                        return "";
                    }
                }
                return source;
            }
        }});
        etContent1.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                StringBuilder sb = new StringBuilder();
                sb.append(dest.subSequence(0, dstart));
                sb.append(source);
                sb.append(dest.subSequence(dend, dest.length()));
                LogUtils.d(TAG, "sb = " + sb.toString());
                String[] texts = sb.toString().split("\\n");
                for (String text : texts) {
                    if (etContent1.getPaint().measureText(text) > lineWidth) {
                        ToastUtil.show(mContext, R.string.tip_label_width_overstep);
                        return "";
                    }
                }
                return source;
            }
        }});
        etContent2.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                StringBuilder sb = new StringBuilder();
                sb.append(dest.subSequence(0, dstart));
                sb.append(source);
                sb.append(dest.subSequence(dend, dest.length()));
                LogUtils.d(TAG, "sb = " + sb.toString());
                String[] texts = sb.toString().split("\\n");
                for (String text : texts) {
                    if (etContent2.getPaint().measureText(text) > lineWidth) {
                        ToastUtil.show(mContext, R.string.tip_label_width_overstep);
                        return "";
                    }
                }
                return source;
            }
        }});
    }

    /**
     * 初始化文本设置
     */
    private void initTextSetting() {

        fontArr = getResources().getStringArray(R.array.label_text_print_font);
        String country = getResources().getConfiguration().locale.getCountry();
        LogUtils.d(TAG, "country = " + country);
        if (country.equals("KR")) {
            fontIndex = 10;//韩文
        } else if (country.equals("HK") || country.equals("TW")) {
            fontIndex = 8;//繁体中文
        } else {
            fontIndex = 9;//简体中文
        }

        int contentSpacingX = getResources().getIntArray(R.array.label_text_printer_spacing_x)[fontIndex];//打印机文本字体间隔
        int titleSpacingX = contentSpacingX * multiple;
        int contentLetterHeight = getResources().getIntArray(R.array.label_text_printer_letter_height)[fontIndex];//打印机文本字体高度
        int titleLetterHeight = contentLetterHeight * multiple;
        contentDistanceY = (int) Math.ceil(contentLetterHeight * 1.2);//打印机文本字体换行间隔
        titleDistanceY = (int) Math.ceil(titleLetterHeight * 1.2);
        contentLetterWidth = getResources().getIntArray(R.array.label_text_printer_letter_width)[fontIndex];//打印机文本字体宽度
        titleLetterWidth = contentLetterWidth * multiple;
        contentLetterCount = (int) Math.floor((Integer.parseInt(mLabelWidth) * 8 - 20 + contentSpacingX) / (contentLetterWidth + contentSpacingX));//单行字母最大个数
        titleLetterCount = (int) Math.floor((Integer.parseInt(mLabelWidth) * 8 - 20 + titleSpacingX) / (titleLetterWidth + titleSpacingX));
        contentTextSize = (float) Math.ceil(((double) lineWidth) / contentLetterCount);
        titleTextSize = (float) Math.ceil(((double) lineWidth) / titleLetterCount);
        LogUtils.d(TAG, "contentTextSize = " + contentTextSize);
        LogUtils.d(TAG, "titleTextSize = " + titleTextSize);
        contentLetterCount = (int) (lineWidth / contentTextSize);
        titleLetterCount = (int) (lineWidth / titleTextSize);
        LogUtils.d(TAG, "contentLetterCount = " + contentLetterCount);
        LogUtils.d(TAG, "titleLetterCount = " + titleLetterCount);
        etTitle.setTextSize(titleTextSize);
        etContent1.setTextSize(contentTextSize);
        etContent2.setTextSize(contentTextSize);
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
                print(title, content1, content2);
                break;
        }
    }

    private void print(String title, String content1, String content2) {
        switch (RTApplication.mode) {
            case RTApplication.MODE_LABEL:
                labelPrint(title, content1, content2);
                break;
        }
    }

    private void labelPrint(String title, String content1, String content2) {

        StringBuilder sb;
        int blankCount;
        int Y = 50;//文字Y方向起始点坐标
        String[] titleTexts = title.split("\\n");
        String[] contentTexts1 = content1.split("\\n");
        String[] contentTexts2 = content2.split("\\n");

        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                LabelBluetoothPrintDriver labelBluetoothPrintDriver = LabelBluetoothPrintDriver.getInstance();
                labelBluetoothPrintDriver.Begin();
                labelBluetoothPrintDriver.SetCLS();
                labelBluetoothPrintDriver.SetSize(mLabelWidth, mLabelHeight);
                for (int i = 0; i < titleTexts.length; i++) {
                    String titleText = titleTexts[i];
                    LogUtils.d(TAG, "titleText = " + titleText);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etTitle.getPaint().measureText(titleText)) / titleTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(titleText);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((titleLetterCount - (int) (etTitle.getPaint().measureText(titleText)) / titleTextSize) % 2 == 1) {
                        X = String.valueOf(10 + titleLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelBluetoothPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", String.valueOf(multiple), String.valueOf(multiple), sb.toString());
                    Y = Y + (i + 1) * titleDistanceY;
                }
                for (int i = 0; i < contentTexts1.length; i++) {
                    String contentText1 = contentTexts1[i];
                    LogUtils.d(TAG, "contentText1 = " + contentText1);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etContent1.getPaint().measureText(contentText1)) / contentTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(contentText1);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((contentLetterCount - (int) (etContent1.getPaint().measureText(contentText1)) / contentTextSize) % 2 == 1) {
                        X = String.valueOf(10 + contentLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelBluetoothPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", "1", "1", sb.toString());
                    Y = Y + (i + 1) * contentDistanceY;
                }
                for (int i = 0; i < contentTexts2.length; i++) {
                    String contentText2 = contentTexts2[i];
                    LogUtils.d(TAG, "contentText2 = " + contentText2);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etContent2.getPaint().measureText(contentText2)) / contentTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(contentText2);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((contentLetterCount - (int) (etContent2.getPaint().measureText(contentText2)) / contentTextSize) % 2 == 1) {
                        X = String.valueOf(10 + contentLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelBluetoothPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", "1", "1", sb.toString());
                    Y = Y + (i + 1) * contentDistanceY;
                }
                labelBluetoothPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelBluetoothPrintDriver.endPro();
                break;
            case Contants.CONNECTED_BY_USB:
                LabelUsbPrintDriver labelUsbPrintDriver = LabelUsbPrintDriver.getInstance();
                labelUsbPrintDriver.Begin();
                labelUsbPrintDriver.SetCLS();
                labelUsbPrintDriver.SetSize(mLabelWidth, mLabelHeight);
                for (int i = 0; i < titleTexts.length; i++) {
                    String titleText = titleTexts[i];
                    LogUtils.d(TAG, "titleText = " + titleText);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etTitle.getPaint().measureText(titleText)) / titleTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(titleText);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((titleLetterCount - (int) (etTitle.getPaint().measureText(titleText)) / titleTextSize) % 2 == 1) {
                        X = String.valueOf(10 + titleLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelUsbPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", String.valueOf(multiple), String.valueOf(multiple), sb.toString());
                    Y = Y + (i + 1) * titleDistanceY;
                }
                for (int i = 0; i < contentTexts1.length; i++) {
                    String contentText1 = contentTexts1[i];
                    LogUtils.d(TAG, "contentText1 = " + contentText1);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etContent1.getPaint().measureText(contentText1)) / contentTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(contentText1);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((contentLetterCount - (int) (etContent1.getPaint().measureText(contentText1)) / contentTextSize) % 2 == 1) {
                        X = String.valueOf(10 + contentLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelUsbPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", "1", "1", sb.toString());
                    Y = Y + (i + 1) * contentDistanceY;
                }
                for (int i = 0; i < contentTexts2.length; i++) {
                    String contentText2 = contentTexts2[i];
                    LogUtils.d(TAG, "contentText2 = " + contentText2);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etContent2.getPaint().measureText(contentText2)) / contentTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(contentText2);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((contentLetterCount - (int) (etContent2.getPaint().measureText(contentText2)) / contentTextSize) % 2 == 1) {
                        X = String.valueOf(10 + contentLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelUsbPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", "1", "1", sb.toString());
                    Y = Y + (i + 1) * contentDistanceY;
                }
                labelUsbPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelUsbPrintDriver.endPro();
                break;
            case Contants.CONNECTED_BY_WIFI:
                LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
                labelWifiPrintDriver.Begin();
                labelWifiPrintDriver.SetCLS();
                labelWifiPrintDriver.SetSize(mLabelWidth, mLabelHeight);
                for (int i = 0; i < titleTexts.length; i++) {
                    String titleText = titleTexts[i];
                    LogUtils.d(TAG, "titleText = " + titleText);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etTitle.getPaint().measureText(titleText)) / titleTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(titleText);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((titleLetterCount - (int) (etTitle.getPaint().measureText(titleText)) / titleTextSize) % 2 == 1) {
                        X = String.valueOf(10 + titleLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelWifiPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", String.valueOf(multiple), String.valueOf(multiple), sb.toString());
                    Y = Y + (i + 1) * titleDistanceY;
                }
                for (int i = 0; i < contentTexts1.length; i++) {
                    String contentText1 = contentTexts1[i];
                    LogUtils.d(TAG, "contentText1 = " + contentText1);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etContent1.getPaint().measureText(contentText1)) / contentTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(contentText1);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((contentLetterCount - (int) (etContent1.getPaint().measureText(contentText1)) / contentTextSize) % 2 == 1) {
                        X = String.valueOf(10 + contentLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelWifiPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", "1", "1", sb.toString());
                    Y = Y + (i + 1) * contentDistanceY;
                }
                for (int i = 0; i < contentTexts2.length; i++) {
                    String contentText2 = contentTexts2[i];
                    LogUtils.d(TAG, "contentText2 = " + contentText2);
                    sb = new StringBuilder();
                    blankCount = (int) ((lineWidth - etContent2.getPaint().measureText(contentText2)) / contentTextSize / 2.0);
                    LogUtils.d(TAG, "blankCount = " + blankCount);
                    for (int j = 0; j < blankCount; j++) {
                        sb.append("\b");
                    }
                    sb.append(contentText2);
                    LogUtils.d(TAG, "sb = " + sb.toString());
                    String X;//文字X方向起始点坐标
                    if ((contentLetterCount - (int) (etContent2.getPaint().measureText(contentText2)) / contentTextSize) % 2 == 1) {
                        X = String.valueOf(10 + contentLetterWidth / 2);
                    } else {
                        X = "10";
                    }
                    LogUtils.d(TAG, "X = " + X);
                    LogUtils.d(TAG, "Y = " + Y);
                    labelWifiPrintDriver.PrintText(X, String.valueOf(Y), fontArr[fontIndex], "0", "1", "1", sb.toString());
                    Y = Y + (i + 1) * contentDistanceY;
                }
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
