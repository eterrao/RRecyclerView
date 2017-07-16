package com.welove520.rrefresh.view;

import android.view.View;

/**
 * Created by Raomengyang on 17-7-14.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class RFooterView {

    public class FooterViewCreator implements ILoadMoreViewFactory {

        @Override
        public ILoadMoreView createLoadMoreView() {
            return new LoadMoreCreator();
        }
    }

    private class LoadMoreCreator implements ILoadMoreViewFactory.ILoadMoreView {
        @Override
        public void init(ILoadMoreViewFactory.FootViewAdder footViewAdder, View.OnClickListener onClickListener) {

        }

        @Override
        public void showLoadMoreNormal() {

        }

        @Override
        public void showLoadMoreFailed() {

        }

        @Override
        public void showLoadMoreError() {

        }

        @Override
        public void showLoadMoreCompleted() {

        }

        @Override
        public void showLoading() {

        }

        @Override
        public void setFooterVisibility(boolean isVisible) {

        }
    }
}
