package com.welove520.rrefresh.view;

import android.view.View;

/**
 * Created by Administrator on 2017/7/16.
 */

public interface LoadMoreHandler {

    void addFooter();

    void removeFooter();

    boolean handleSetAdapter(View contentView, ILoadMoreViewFactory.ILoadMoreView loadMoreView, View.OnClickListener onClickLoadMoreListener);

    void setOnScrollBottomListener(View contentView, OnScrollBottomListener scrollBottomListener);
}
