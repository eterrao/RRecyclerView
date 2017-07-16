package com.welove520.rrefresh.view;

import android.view.View;

/**
 * Created by Raomengyang on 17-7-14.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public interface ILoadMoreViewFactory {

    ILoadMoreView createLoadMoreView();

    interface ILoadMoreView {

        void init(FootViewAdder footViewAdder, View.OnClickListener onClickListener);

        void showLoadMoreNormal();

        void showLoadMoreFailed();

        void showLoadMoreError();

        void showLoadMoreCompleted();


        void showLoading();

        void setFooterVisibility(boolean isVisible);
    }

    interface FootViewAdder {

        View addFootView(View view);

        View addFootView(int layoutId);

    }

}
