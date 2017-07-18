package com.welove520.rrefresh.view.network;

import android.view.View;

/**
 * Created by Administrator on 2017/7/17.
 */

public interface StatusLayout {

    interface NetworkStatusLayout extends StatusLayout {
        int STATUS_ERROR = 0;
        int STATUS_RETRY = 1;
        int STATUS_NO_NET = 2;
        int STATUS_NO_WIFI = 3;
        int STATUS_NO_DATA = 4;

        void showNetwork(int status);

        void hideNetwork();

    }

    interface DataStatusLayout extends StatusLayout {
        int STATUS_ERROR = 0;
        int STATUS_RETRY = 1;
        int STATUS_PRE_LOAD = 2;
        int STATUS_EMPTY = 3;

        void showData(int status);

        void hideData();

        void setDataStatusContainer(View container);
    }
}
