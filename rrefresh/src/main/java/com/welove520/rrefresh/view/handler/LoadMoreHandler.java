package com.welove520.rrefresh.view.handler;

import android.view.View;

import com.welove520.rrefresh.view.base.ILoadMoreViewFactory;
import com.welove520.rrefresh.view.listener.OnScrollBottomListener;

/**
 * Created by Administrator on 2017/7/16.
 */

public interface LoadMoreHandler {

    void addFooter();

    void removeFooter();

    boolean handleSetAdapter(View contentView, ILoadMoreViewFactory.ILoadMoreView loadMoreView, View.OnClickListener onClickLoadMoreListener);

    void setOnScrollBottomListener(View contentView, OnScrollBottomListener scrollBottomListener);
}
