package com.mocoo.hang.rtprinter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Hashtable;

/**
 * Created by Administrator on 2015/6/12.
 */
public class ZXingUtil {

    public static final String TAG = "ZXingUtil";

    private ZXingUtil()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    /**
     * 生成条形码
     *
     * @param context
     * @param contents      需要生成的内容
     * @param desiredWidth  生成条形码的宽带
     * @param desiredHeight 生成条形码的高度
     * @param displayCode   是否在条形码下方显示内容
     * @return
     */
    public static Bitmap creatBarcodeBitmap(Context context, String contents, BarcodeFormat barcodeFormat,
                                            int desiredWidth, int desiredHeight, boolean displayCode) {
        Bitmap ruseltBitmap = null;

        if (displayCode) {

            //图片两端所保留的空白的宽度
            int codeHeight = desiredHeight / 3;
            LogUtils.d(TAG,"desiredHeight = " + desiredHeight);
            LogUtils.d(TAG,"codeHeight = " + codeHeight);
            Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat,
                    desiredWidth, desiredHeight - codeHeight);
            LogUtils.d(TAG,"barcodeBitmap getHeight = " + barcodeBitmap.getHeight());
            Bitmap codeBitmap = creatCodeBitmap(context, contents, desiredWidth, codeHeight);
            LogUtils.d(TAG,"codeBitmap getHeight = " + codeBitmap.getHeight());
            ruseltBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(
                    0, desiredHeight - codeHeight));
            LogUtils.d(TAG,"ruseltBitmap getHeight = " + ruseltBitmap.getHeight());
        } else {
            ruseltBitmap = encodeAsBitmap(contents, barcodeFormat,
                    desiredWidth, desiredHeight);
        }

        return ruseltBitmap;
    }

    /**
     * 生成条形码的Bitmap
     *
     * @param contents      需要生成的内容
     * @param format        编码格式
     * @param desiredWidth
     * @param desiredHeight
     * @return
     * @throws WriterException
     */
    protected static Bitmap encodeAsBitmap(String contents,
                                           BarcodeFormat format, int desiredWidth, int desiredHeight) {
        try {
            // 判断URL合法性
            if (contents == null || "".equals(contents) || contents.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents,
                    format, desiredWidth, desiredHeight, hints);
            int[] pixels = new int[desiredWidth * desiredHeight];
            // 下面这里按照条码的算法，逐个生成条码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < desiredHeight; y++) {
                for (int x = 0; x < desiredWidth; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * desiredWidth + x] = 0xff000000;
                    } else {
                        pixels[y * desiredWidth + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(desiredWidth, desiredHeight,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, desiredWidth, 0, 0, desiredWidth, desiredHeight);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, "return null");
        return null;
    }

    /**
     * 生成显示编码的Bitmap
     *
     * @param context
     * @param contents
     * @param width
     * @param height
     * @return
     */
    protected static Bitmap creatCodeBitmap(Context context,String contents, int width, int height) {

        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setWidth(width);
        LogUtils.d(TAG,"height = " + height);
        tv.setHeight(height);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        LogUtils.d(TAG,"tv getHeight = " + tv.getHeight());

        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        return bitmapCode;
    }

    /**
     * 将两个Bitmap合并成一个
     *
     * @param first
     * @param second
     * @param fromPoint 第二个Bitmap开始绘制的起始位置（相对于第一个Bitmap）
     * @return
     */
    protected static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
                                          PointF fromPoint) {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }
        Bitmap newBitmap = Bitmap.createBitmap(
                first.getWidth()>second.getWidth()?first.getWidth():second.getWidth(),
                first.getHeight() + second.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, 0, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();

        return newBitmap;
    }

}
