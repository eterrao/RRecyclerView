package com.welove520.rrefresh.view.base;

/**
 * Created by Raomengyang on 17-7-14.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public interface IPtrViewFactory {

    IPtrView createPtrView();

    interface IPtrView {
        void showPTRNormal();

        void showPTRFailed();

        void showPTRError();

        void showPTRCompleted();
    }

}
