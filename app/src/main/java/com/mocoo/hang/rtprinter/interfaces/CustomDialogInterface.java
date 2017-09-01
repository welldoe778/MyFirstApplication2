package com.mocoo.hang.rtprinter.interfaces;

import android.content.DialogInterface;

/**
 * Created by Administrator on 2015/6/30.
 */
public interface CustomDialogInterface {

    public interface onPositiveClickListener {
        public void onDialogPositiveClick(String text);
    }

    public interface onItemClickListener {
        public void onItemClick(String text);
    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogInterface dialog);
        public void onDialogNegativeClick(DialogInterface dialog);
    }


}
