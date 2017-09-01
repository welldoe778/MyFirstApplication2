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

/**
 * Created by Administrator on 2015/6/10.
 */
public class DeleteSavedContentDialog extends DialogFragment {

    private Context mContext;
    private DialogInterface.OnClickListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delete_saved_content, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view).setCancelable(true)
                .setPositiveButton(R.string.delete, mListener).setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    public void setOnPositiveClickListener(DialogInterface.OnClickListener listener) {
        mListener = listener;
    }

}
