package com.welove520.rrefresh.view.listener;

/**
 * Created by Raomengyang on 17-7-17.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public interface OnLoadListener {

    void start();

    void stop(boolean needRefresh);

    boolean isRunning();
}
