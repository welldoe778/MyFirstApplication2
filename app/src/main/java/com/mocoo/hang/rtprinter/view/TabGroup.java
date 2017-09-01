package com.mocoo.hang.rtprinter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2015/4/17.
 */
public class TabGroup extends LinearLayout {

    private final String TAG = getClass().getSimpleName();

    public TabGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setClickable(false);
    }

    private int mCheckedId = -1;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    /**
     * <p>Interface definition for a callback to be invoked when the checkbake
     * view changed in this group.</p>
     */
    public interface OnCheckedChangeListener {
        /**
         * <p>Called when the checkable view has changed. When the
         * selection is cleared, checkedId is -1.</p>
         *
         * @param group     the group in which the checkable view has changed
         * @param checkedId the unique identifier of the newly checked checkable view
         */
        public void onCheckedChanged(TabGroup group, int checkedId);
    }

    public TabGroup(Context context) {
        super(context);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setItemListener();
    }

    private void setItemListener() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof Checkable) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        check(v.getId());
                    }
                });
                if (((Checkable) view).isChecked()) {
                    mCheckedId = view.getId();
                }
            }
        }
    }

    public void check(int id) {
        if (id != -1 && id == mCheckedId) {
            return;
        }

        if (mCheckedId != -1) {
            setCheckedStateForView(mCheckedId, false);
        }

        if (id != -1) {
            setCheckedStateForView(id, true);
        }

        setCheckedId(id);
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView != null && checkedView instanceof Checkable) {
            ((Checkable) checkedView).setChecked(checked);
        }
    }

    private void setCheckedId(int id) {
        mCheckedId = id;
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
        }
    }

}
