package com.welove520.rrefresh.view.network;

/**
 * Created by Administrator on 2017/7/17.
 */

public interface NetworkNoticeListener {

    int ERROR = 0;
    int NO_NET = 1;

    void show(int status);

    void hide();
}
