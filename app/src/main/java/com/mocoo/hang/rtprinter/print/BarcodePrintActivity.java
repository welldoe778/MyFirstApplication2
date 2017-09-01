package com.mocoo.hang.rtprinter.print;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.main.RTApplication;
import com.mocoo.hang.rtprinter.observable.ConnStateObservable;
import com.mocoo.hang.rtprinter.utils.DensityUtils;
import com.mocoo.hang.rtprinter.utils.LogUtils;
import com.mocoo.hang.rtprinter.utils.SPUtils;
import com.mocoo.hang.rtprinter.utils.SaveMediaFileUtil;
import com.mocoo.hang.rtprinter.utils.ToastUtil;
import com.mocoo.hang.rtprinter.utils.ZXingUtil;
import com.mocoo.hang.rtprinter.view.ScrollEditText;
import com.mocoo.hang.swipeback.SwipeBackLayout;
import com.mocoo.hang.swipeback.app.SwipeBackActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com.rtdriver.driver.BarcodeType;
import com.rtdriver.driver.Contants;
import com.rtdriver.driver.HsBluetoothPrintDriver;
import com.rtdriver.driver.HsUsbPrintDriver;
import com.rtdriver.driver.HsWifiPrintDriver;
import com.rtdriver.driver.LabelBluetoothPrintDriver;
import com.rtdriver.driver.LabelUsbPrintDriver;
import com.rtdriver.driver.LabelWifiPrintDriver;

/**
 * Created by Administrator on 2015/6/1.
 */
public class BarcodePrintActivity extends SwipeBackActivity implements Observer {

    private final String TAG = getClass().getSimpleName();

    public static final String BUNDLE_KEY_BARCODE_TYPE = "barcodeType";
    public static final String BUNDLE_KEY_BARCODE_CODE_FOR_CREATED = "barcodeCodeForCreated";

    private Context mContext;

    private SwipeBackLayout mSwipeBackLayout;
    private RelativeLayout title;
    private LinearLayout back;
    private TextView tvConnectState;
    private FrameLayout flContent;
    private ImageView ivBarcode;
    private RadioGroup rg_print_barcode_orientation;

    private Bundle mBundle;
    private BarcodeType barcodeType;//表示当前的条码类型
    private String labelCodeType;//表示当前的条码类型（仅用于标签打印）
    private String mCurrentCodeForCreated = "";//表示当前生成的条形码的值
    private boolean flagRestored = false;//表示保存内容的数据已经读取过
    private Bitmap barcodeBm;

    private int showRotate = 0;//条码显示的方向，90左旋，-90右旋，0正常
    private int rotate = 0;//条码显示的方向，1左旋，2右旋，0正常


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_barcode);
        mContext = this;
        ConnStateObservable.getInstance().addObserver(this);
        initData();
        initView();
        setCustomSelectionActionModeCallback();
        showDefaultBarcode(barcodeType);

        LogUtils.d(TAG, "String.valueOf(barcodeType) = " + String.valueOf(barcodeType));
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        mSwipeBackLayout.setEdgeSize(DensityUtils.dp2px(mContext, 70));
    }

    private void initData() {
        LogUtils.v(TAG, "initData");
        mBundle = getIntent().getExtras();
        barcodeType = Enum.valueOf(BarcodeType.class, mBundle.getString(BUNDLE_KEY_BARCODE_TYPE));
        if (RTApplication.mode == RTApplication.MODE_LABEL) {
            switch (barcodeType) {
                case UPC_A:
                    labelCodeType = "UPCA";
                    break;
                case EAN13:
                    labelCodeType = "EAN13";
                    break;
                case EAN8:
                    labelCodeType = "EAN8";
                    break;
                case CODE39:
                    labelCodeType = "39";
                    break;
                case CODABAR:
                    labelCodeType = "CODA";
                    break;
                case CODE128:
                    labelCodeType = "128M";
                    break;
            }
        }
        /**
         * 加载条码方向
         */
        showRotate = (int) SPUtils.get(mContext, "showRotate", 0);
    }

    private void initView() {
        mSwipeBackLayout = getSwipeBackLayout();
        title = (RelativeLayout) this.findViewById(R.id.title);
        back = (LinearLayout) this.findViewById(R.id.back);
        tvConnectState = (TextView) this.findViewById(R.id.connect_state);
        flContent = (FrameLayout) this.findViewById(R.id.fl_print_barcode_content);
        ivBarcode = (ImageView) this.findViewById(R.id.iv_print_barcode_barcode);
        rg_print_barcode_orientation = (RadioGroup) this.findViewById(R.id.rg_print_barcode_orientation);
        TextView tvPreview = (TextView) this.findViewById(R.id.tv_print_barcode_preview);
        tvPreview.setText(getString(R.string.tip_preview) + " " + barcodeType);
        /**
         *初始化radiobutton
         */
        switch (showRotate) {
            case 90:
                rg_print_barcode_orientation.check(R.id.rb_print_barcode_orientation_left);
                break;
            case 0:
                rg_print_barcode_orientation.check(R.id.rb_print_barcode_orientation_normal);
                break;
            case -90:
                rg_print_barcode_orientation.check(R.id.rb_print_barcode_orientation_right);
                break;
        }
        setListener();
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
    }

    private void setListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rg_print_barcode_orientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("group", "group");
                switch (checkedId) {
                    case R.id.rb_print_barcode_orientation_left:
                        Log.d("rotate", "左");
                        showRotate = 90;
                        break;
                    case R.id.rb_print_barcode_orientation_normal:
                        showRotate = 0;
                        Log.d("rotate", "中");
                        break;
                    case R.id.rb_print_barcode_orientation_right:
                        showRotate = -90;
                        Log.d("rotate", "右");
                        break;
                }
                Matrix matrix = new Matrix();
                matrix.setRotate(showRotate);
                Bitmap b = Bitmap.createBitmap(barcodeBm, 0, 0, barcodeBm.getWidth(), barcodeBm.getHeight(), matrix, true);
                ivBarcode.setImageBitmap(b);
                SPUtils.put(mContext, "showRotate", showRotate);
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.print:
                LogUtils.v(TAG, "print");
                if (RTApplication.getConnState() == Contants.UNCONNECTED) {
                    ToastUtil.show(mContext, R.string.tip_connect);
                    return;
                }
                /**
                 * 转成命令格式
                 */
                switch (showRotate) {
                    case 0:
                        rotate = 0;
                        break;
                    case -90:
                        rotate = 2;
                        break;
                    case 90:
                        rotate = 1;
                        break;
                }
                print();
                break;
        }
    }

    /**
     * 检查输入值的合法性
     *
     * @param inputStr
     * @return 返回true表示输入值合法
     */
    private boolean checkInput(String inputStr) {
        String regex = null;
        switch (barcodeType) {
            case UPC_A:
                regex = "\\d{11}";
                return inputStr.matches(regex);
            case UPC_E:
/*                regex = "\\d{6}";
                return inputStr.matches(regex);*/
                return false;
            case EAN13:
                regex = "\\d{12}";
                return inputStr.matches(regex);
            case EAN8:
                regex = "\\d{7}";
                return inputStr.matches(regex);
            case CODE39:
                regex = "[a-zA-Z\\p{Digit} \\$%\\+\\-\\./]{1,30}";
                return inputStr.matches(regex);
            case ITF:
                regex = "(\\d{2}){1,15}";
                return inputStr.matches(regex);
            case CODABAR:
                regex = "[A-D][0-9\\$\\+\\-\\./:]{0,28}[A-D]";
                return inputStr.matches(regex);
            case CODE93:
                return false;
            case CODE128:
                regex = "[\\p{ASCII}]{1,42}";
                return inputStr.matches(regex);
            case QR_CODE:
                regex = "[\\p{ASCII}]+";
                return inputStr.matches(regex);
            default:
                return false;
        }
    }

    private void showDefaultBarcode(BarcodeType barcodeType) {
        switch (barcodeType) {
            case UPC_A:
                showBarcode("12345678901");
                break;
            case UPC_E:
                break;
            case EAN13:
                showBarcode("6901234567892");
                break;
            case EAN8:
                showBarcode("12345678");
                break;
            case CODE39:
                showBarcode("123ABC $%");
                break;
            case ITF:
                showBarcode("1234567890");
                break;
            case CODABAR:
                showBarcode("A123$+-B");
                break;
            case CODE93:
                break;
            case CODE128:
                showBarcode("123ABC$%^");
                break;
            case QR_CODE:
                showBarcode("http://www.rongtatech.com");
                break;
        }
    }

    private void showBarcode(String inputStr) {
        switch (barcodeType) {
            case UPC_A:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.UPC_A, DensityUtils.dp2px(mContext, 200), DensityUtils.dp2px(mContext, 70), true);
                break;
            case UPC_E:
                break;
            case EAN13:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.EAN_13, DensityUtils.dp2px(mContext, 200), DensityUtils.dp2px(mContext, 70), true);
                break;
            case EAN8:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.EAN_8, DensityUtils.dp2px(mContext, 200), DensityUtils.dp2px(mContext, 70), true);
                break;
            case CODE39:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.CODE_39, DensityUtils.dp2px(mContext, 200), DensityUtils.dp2px(mContext, 70), true);
                break;
            case ITF:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.ITF, DensityUtils.dp2px(mContext, 200), DensityUtils.dp2px(mContext, 70), true);
                break;
            case CODABAR:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.CODABAR, DensityUtils.dp2px(mContext, 200), DensityUtils.dp2px(mContext, 70), true);
                break;
            case CODE93:
                break;
            case CODE128:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.CODE_128, DensityUtils.dp2px(mContext, 200), DensityUtils.dp2px(mContext, 70), true);
                break;
            case QR_CODE:
                barcodeBm = ZXingUtil.creatBarcodeBitmap(mContext, inputStr, BarcodeFormat.QR_CODE, DensityUtils.dp2px(mContext, 150), DensityUtils.dp2px(mContext, 150), false);
                break;
        }
        mCurrentCodeForCreated = inputStr;
        LogUtils.d(TAG, "mCurrentCodeForCreated = " + mCurrentCodeForCreated);

        //初始化条码旋转的方向。。
        Matrix matrix = new Matrix();
        matrix.setRotate(showRotate);
        Bitmap b = Bitmap.createBitmap(barcodeBm, 0, 0, barcodeBm.getWidth(), barcodeBm.getHeight(), matrix, true);
        ivBarcode.setImageBitmap(b);
    }

    /**
     * 对数字字符串进行加工，在其最后一位添加校验码
     *
     * @param originalStr
     * @return
     */
    private String getVerifiedStr(String originalStr, BarcodeType barcodeType) {
        int sum = 0;
        switch (barcodeType) {
            case EAN8:
                for (int i = 0; i < originalStr.length(); i++) {
                    sum += (originalStr.charAt(i) - '0') * (i % 2 == 0 ? 3 : 1);
                }
                break;
            case EAN13:
                for (int i = 0; i < originalStr.length(); i++) {
                    sum += (originalStr.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
                }
                break;
            case UPC_E:
            case UPC_A:
                for (int i = 0; i < originalStr.length(); i++) {
                    sum += (originalStr.charAt(i) - '0') * (i % 2 == 1 ? 1 : 3);
                }
                break;
        }
        int checkCode = (10 - sum % 10) == 10 ? 0 : 10 - sum % 10;
        return originalStr + String.valueOf(checkCode);
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
        Log.d("rotatee-----", rotate + "");
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                HsBluetoothPrintDriver hsBluetoothPrintDriver = HsBluetoothPrintDriver.getInstance();
                hsBluetoothPrintDriver.Begin();
                hsBluetoothPrintDriver.SetDefaultSetting();
                hsBluetoothPrintDriver.SetPrintRotate((byte) rotate);
                hsBluetoothPrintDriver.SetAlignMode((byte) 0x01);
                hsBluetoothPrintDriver.SetHRIPosition((byte) 0x02);
                hsBluetoothPrintDriver.AddCodePrint(barcodeType, mCurrentCodeForCreated);
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.LF();
                hsBluetoothPrintDriver.CR();
                hsBluetoothPrintDriver.SetPrintRotate((byte) 0);
                break;
            case Contants.CONNECTED_BY_USB:
                HsUsbPrintDriver hsUsbPrintDriver = HsUsbPrintDriver.getInstance();
                hsUsbPrintDriver.Begin();
                hsUsbPrintDriver.SetDefaultSetting();
                hsUsbPrintDriver.SetPrintRotate((byte) rotate);
                hsUsbPrintDriver.SetAlignMode((byte) 0x01);
                hsUsbPrintDriver.SetHRIPosition((byte) 0x02);
                hsUsbPrintDriver.AddCodePrint(barcodeType, mCurrentCodeForCreated);
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.LF();
                hsUsbPrintDriver.CR();
                hsUsbPrintDriver.SetPrintRotate((byte) 0);
                break;
            case Contants.CONNECTED_BY_WIFI:
                HsWifiPrintDriver hsWifiPrintDriver = HsWifiPrintDriver.getInstance();
                hsWifiPrintDriver.Begin();
                hsWifiPrintDriver.SetDefaultSetting();
                hsWifiPrintDriver.SetPrintRotate((byte) rotate);
                hsWifiPrintDriver.SetAlignMode((byte) 0x01);
                hsWifiPrintDriver.SetHRIPosition((byte) 0x02);
                hsWifiPrintDriver.AddCodePrint(barcodeType, mCurrentCodeForCreated);
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.LF();
                hsWifiPrintDriver.CR();
                hsWifiPrintDriver.SetPrintRotate((byte) 0);
                break;
        }
    }

    private void labelPrint() {
        switch (RTApplication.getConnState()) {
            case Contants.CONNECTED_BY_BLUETOOTH:
                LabelBluetoothPrintDriver labelBluetoothPrintDriver = LabelBluetoothPrintDriver.getInstance();
                labelBluetoothPrintDriver.Begin();
                labelBluetoothPrintDriver.SetCLS();
                labelBluetoothPrintDriver.SetPrintRotate((byte) rotate);
                labelBluetoothPrintDriver.SetSize(RTApplication.labelWidth, RTApplication.labelHeight);
                labelBluetoothPrintDriver.CodePrint("64", "30", labelCodeType, "72", "1", "0", "2", "2", mCurrentCodeForCreated);
                labelBluetoothPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelBluetoothPrintDriver.endPro();
                labelBluetoothPrintDriver.SetPrintRotate((byte) 0);

                break;
            case Contants.CONNECTED_BY_USB:
                LabelUsbPrintDriver labelUsbPrintDriver = LabelUsbPrintDriver.getInstance();
                labelUsbPrintDriver.Begin();
                labelUsbPrintDriver.SetCLS();
                labelUsbPrintDriver.SetPrintRotate((byte) rotate);
                labelUsbPrintDriver.SetSize(RTApplication.labelWidth, RTApplication.labelHeight);
                labelUsbPrintDriver.CodePrint("64", "30", labelCodeType, "72", "1", "0", "2", "2", mCurrentCodeForCreated);
                labelUsbPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelUsbPrintDriver.endPro();
                labelUsbPrintDriver.SetPrintRotate((byte) 0);

                break;
            case Contants.CONNECTED_BY_WIFI:
                LabelWifiPrintDriver labelWifiPrintDriver = LabelWifiPrintDriver.getInstance();
                labelWifiPrintDriver.Begin();
                labelWifiPrintDriver.SetCLS();
                labelWifiPrintDriver.SetPrintRotate((byte) rotate);
                labelWifiPrintDriver.SetSize(RTApplication.labelWidth, RTApplication.labelHeight);
                labelWifiPrintDriver.CodePrint("64", "30", labelCodeType, "72", "1", "0", "2", "2", mCurrentCodeForCreated);
                labelWifiPrintDriver.SetPRINT("1", RTApplication.labelCopies);
                labelWifiPrintDriver.endPro();
                labelWifiPrintDriver.SetPrintRotate((byte) 0);
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
        barcodeBm.recycle();
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
