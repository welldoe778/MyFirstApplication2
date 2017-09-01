package com.mocoo.hang.rtprinter.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.mocoo.hang.rtprinter.R;

/**
 * Created by Administrator on 2015/6/10.
 */
public class CustomProcessDialog extends DialogFragment {

    private Context mContext;
    private Dialog mDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = new Dialog(mContext, R.style.CustomProcessDialog);
        return mDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_progressbar,null);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(192, 192);
    }

    public boolean isShowing() {
        if(mDialog == null){
            return false;
        }
        return mDialog.isShowing();
    }

}
