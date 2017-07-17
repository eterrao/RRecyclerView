package com.welove520.rrefresh.view.base;

import android.view.ViewGroup;
import android.view.animation.Animation;

import com.welove520.rrefresh.view.listener.OnLoadListener;

/**
 * Created by Administrator on 2017/7/16.
 */

public interface IHeaderView extends OnLoadListener {

    int STATE_RELEASE_TO_REFRESH = 1;
    int STATE_START = 2;
    int STATE_DONE = 3;
    int STATE_ERROR = 4;

    void setPercent(float mCurrentDragPercent, boolean invalidate);

    void performLayout(int l, int t, int r, int b);

    void requestLayout();

    int getMeasuredWidth();

    int getMeasuredHeight();

    void setState(int state);

    void setDragStatus(boolean isDragged);

    void setPadding(int left, int top, int right, int bottom);

    void measure(int widthMeasureSpec, int heightMeasureSpec);

    void clearAnimation();

    void startAnimation(Animation mAnimateToStartPosition);

    ViewGroup.LayoutParams getLayoutParams();

    void offsetTopAndBottom(int offset);
}
