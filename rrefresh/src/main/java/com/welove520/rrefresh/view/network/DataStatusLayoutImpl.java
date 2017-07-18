package com.welove520.rrefresh.view.network;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Raomengyang on 17-7-18.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class DataStatusLayoutImpl extends RelativeLayout implements StatusLayout.DataStatusLayout {

    private View mContainer;

    public DataStatusLayoutImpl(Context context) {
        this(context, null);
    }

    public DataStatusLayoutImpl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataStatusLayoutImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {

    }

    @Override
    public void showData(int status) {
        if (mContainer != null) {
            removeView(mContainer);
            addView(mContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public void hideData() {
        if (mContainer != null) {
            removeView(mContainer);
        }
    }

    @Override
    public void setDataStatusContainer(View container) {
        if (this.mContainer != null) {
            removeView(this.mContainer);
        }
        this.mContainer = container;
    }

    public View getContainer() {
        return mContainer;
    }
}
