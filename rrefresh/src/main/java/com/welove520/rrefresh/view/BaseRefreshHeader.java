package com.welove520.rrefresh.view;

/**
 * Created by jianghejie on 15/11/22.
 */
public interface BaseRefreshHeader {
    public void onMove(float delta);

    public boolean releaseAction();

    public void refreshComplate();

    public void refreshFault();

    public final static int STATE_NORMAL = 0;
    public final static int STATE_RELEASE_TO_REFRESH = 1;
    public final static int STATE_REFRESHING = 2;
    public final static int STATE_DONE = 3;
    public final static int STATE_FAULT = 4;
}
