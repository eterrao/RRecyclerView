package com.welove520.rrefresh.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Raomengyang on 17-7-13.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class RRecyclerFooterView extends View {
    public RRecyclerFooterView(Context context) {
        super(context);
    }

    public RRecyclerFooterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RRecyclerFooterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RRecyclerFooterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
