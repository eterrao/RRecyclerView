package com.welove520.rrefresh.view.base;

import android.graphics.drawable.Animatable;

/**
 * Created by Administrator on 2017/7/16.
 */

public interface IHeaderView extends Animatable {

    void setPercent(float mCurrentDragPercent, boolean invalidate);

    void layout(int l, int t, int r, int b);

    void requestLayout();
}
