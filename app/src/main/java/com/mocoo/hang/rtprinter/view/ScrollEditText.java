package com.mocoo.hang.rtprinter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;

import com.mocoo.hang.rtprinter.utils.LogUtils;

/**
 * Created by Administrator on 2015/7/15.
 */
public class ScrollEditText extends EditText {

    private final String TAG = getClass().getSimpleName();

    private GestureDetector detector;

    public ScrollEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            //当EditText滑动到顶部或者底部时，允许父类控件拦截触摸事件
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                super.onScroll(e1, e2, distanceX, distanceY);
                LogUtils.d(TAG, "onScroll");
                LogUtils.d(TAG, "distanceY = " + distanceY);
                LogUtils.d(TAG, "computeVerticalScrollExtent = " + computeVerticalScrollExtent());
                LogUtils.d(TAG, "computeVerticalScrollOffset = " + computeVerticalScrollOffset());
                LogUtils.d(TAG, "computeVerticalScrollRange = " + computeVerticalScrollRange());
                boolean clampedY = false;
                if (computeVerticalScrollOffset() == 0
                        && distanceY < 0) {
                    clampedY = true;
                }

                int deltaY = computeVerticalScrollRange() - computeVerticalScrollExtent();
                if ((computeVerticalScrollOffset() == deltaY || deltaY < 0)
                        && distanceY > 0) {
                    clampedY = true;
                }
                if (clampedY) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event) | detector.onTouchEvent(event);
    }

}
