package com.mocoo.hang.rtprinter.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.mocoo.hang.rtprinter.R;
import com.mocoo.hang.rtprinter.interfaces.CustomDialogInterface;

/**
 * Created by Administrator on 2015/6/10.
 */
public class SaveSettingDialog extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private CustomDialogInterface.NoticeDialogListener mNoticeDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_save_setting, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view).setCancelable(true).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNoticeDialogListener.onDialogPositiveClick(dialog);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNoticeDialogListener.onDialogNegativeClick(dialog);
            }
        });
        return builder.create();
    }


    public void setNoticeDialogListener(CustomDialogInterface.NoticeDialogListener listener) {
        mNoticeDialogListener = listener;
    }

}
