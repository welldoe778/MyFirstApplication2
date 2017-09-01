package com.mocoo.hang.rtprinter.utils;

import android.util.Log;

/**
 * Created by Administrator on 2015/7/10.
 */
public class LogUtils {

    private static boolean mDebug = false;

    private LogUtils()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void setDebug(boolean debug){
        mDebug = debug;
    }

    public static void v(String tag, String msg){
        if(mDebug){
            Log.v(tag,msg);
        }
    }
    public static void d(String tag, String msg){
        if(mDebug){
            Log.d(tag,msg);
        }
    }
    public static void i(String tag, String msg){
        if(mDebug){
            Log.i(tag,msg);
        }
    }
    public static void w(String tag, String msg){
        if(mDebug){
            Log.w(tag,msg);
        }
    }
    public static void e(String tag, String msg){
        if(mDebug){
            Log.e(tag,msg);
        }
    }

}
