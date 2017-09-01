package com.mocoo.hang.rtprinter.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/4/16.
 */
public class ToastUtil {

    private ToastUtil()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     *
     * @param context
     * @param msg 要显示的信息字符串
     */
    public static void show(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param context
     * @param msgResId 要显示的信息字符串在R文件中的id
     */
    public static void show(Context context,int msgResId){
        Toast.makeText(context,msgResId,Toast.LENGTH_SHORT).show();
    }

}
