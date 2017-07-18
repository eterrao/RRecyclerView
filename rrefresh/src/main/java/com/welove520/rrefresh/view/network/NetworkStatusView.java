package com.welove520.rrefresh.view.network;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.welove520.rrefresh.R;

/**
 * Created by Administrator on 2017/7/17.
 */

public class NetworkStatusView extends RelativeLayout {

    OnNetworkStatusViewListener onNetworkStatusViewListener;
    private View mContainer;

    public NetworkStatusView(Context context) {
        this(context, null);
    }

    public NetworkStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContainer = LayoutInflater.from(context).inflate(R.layout.common_loading_reload_layout, null);
        TextView retryBtn = (TextView) mContainer.findViewById(R.id.reload_btn_text);
        retryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNetworkStatusViewListener != null) {
                    onNetworkStatusViewListener.onRetry();
                }
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);
        addView(mContainer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setOnNetworkStatusViewListener(OnNetworkStatusViewListener onNetworkStatusViewListener) {
        this.onNetworkStatusViewListener = onNetworkStatusViewListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public interface NetworkNoticeListener {

        int STATE_ERROR = 0;
        int STATE_NO_NET = 1;

        void show(int status);

        void hide();
    }

    public interface OnNetworkStatusViewListener {

        void onRetry();

    }
}
