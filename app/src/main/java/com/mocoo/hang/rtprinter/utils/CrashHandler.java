package com.mocoo.hang.rtprinter.utils;

import android.content.Context;
import android.os.Process;
import android.util.Log;

/**
 * Created by Administrator on 2015/7/11.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler ch = new CrashHandler();
    public final String TAG = getClass().getSimpleName();

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return ch;
    }

    public void init(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "error : ", ex);
        Process.killProcess(Process.myPid());
        System.exit(1);
    }
}
