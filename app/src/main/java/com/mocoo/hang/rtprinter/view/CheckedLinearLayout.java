package com.mocoo.hang.rtprinter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2015/6/1.
 */
public class CheckedLinearLayout extends LinearLayout implements Checkable{

    private boolean mChecked;

    public CheckedLinearLayout(Context context) {
        this(context, null);
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked) {
        if(mChecked!=checked){
            mChecked = checked;
            this.setSelected(mChecked);
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
