package com.welove520.rrefresh.view.base;

/**
 * Created by Raomengyang on 17-7-14.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

interface IRRefreshView {

    void setHeaderView(IPtrViewFactory ptrViewFactory);

    void setFooterView(ILoadMoreViewFactory loadMoreViewFactory);

    void setNetworkStatus(int status);

}
