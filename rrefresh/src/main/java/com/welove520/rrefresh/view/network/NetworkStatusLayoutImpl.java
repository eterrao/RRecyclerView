package com.welove520.rrefresh.view.network;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.welove520.rrefresh.R;
import com.welove520.rrefresh.view.network.StatusLayout.NetworkStatusLayout;


/**
 * Created by Administrator on 2017/7/17.
 */

public class NetworkStatusLayoutImpl extends RelativeLayout implements NetworkStatusLayout {

    private View container;
    private View retryView;
    private View noNetView;
    private View errorView;

    public NetworkStatusLayoutImpl(Context context) {
        this(context, null);
    }

    public NetworkStatusLayoutImpl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkStatusLayoutImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        container = LayoutInflater.from(context).inflate(R.layout.layout_base_network_status, null);
        retryView = container.findViewById(R.id.layout_retry);
        noNetView = container.findViewById(R.id.layout_no_net);
        errorView = container.findViewById(R.id.layout_error);
        addView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView textView = (TextView) retryView.findViewById(R.id.reload_btn_text);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNetwork();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void showNetwork(int status) {
        setVisibility(VISIBLE);
        switch (status) {
            case STATUS_ERROR:
                if (retryView != null) {
                    retryView.setVisibility(GONE);
                }
                if (noNetView != null) {
                    noNetView.setVisibility(GONE);
                }
                if (errorView != null) {
                    errorView.setVisibility(VISIBLE);
                }
                break;
            case STATUS_RETRY:
                if (retryView != null) {
                    retryView.setVisibility(VISIBLE);
                }
                if (noNetView != null) {
                    noNetView.setVisibility(GONE);
                }
                if (errorView != null) {
                    errorView.setVisibility(GONE);
                }
                break;
            case STATUS_NO_NET:
                if (retryView != null) {
                    retryView.setVisibility(GONE);
                }
                if (noNetView != null) {
                    noNetView.setVisibility(VISIBLE);
                }
                if (errorView != null) {
                    errorView.setVisibility(GONE);
                }
                break;
        }
    }

    @Override
    public void hideNetwork() {
        setVisibility(INVISIBLE);
    }

    public View getContainer() {
        return container;
    }

    public View getRetryView() {
        return retryView;
    }

    public void setRetryView(View retryView) {
        this.retryView = retryView;
    }

    public View getNoNetView() {
        return noNetView;
    }

    public void setNoNetView(View noNetView) {
        this.noNetView = noNetView;
    }

    public View getErrorView() {
        return errorView;
    }

    public void setErrorView(View errorView) {
        this.errorView = errorView;
    }
}
