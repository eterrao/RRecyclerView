package com.welove520.rrefresh.view.base;

import android.content.Context;

/**
 * Created by Raomengyang on 17-7-14.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public interface IPtrViewFactory {

    IPtrView createPtrView(Context context);

    interface IPtrView extends IHeaderView {
        void showPTRNormal();

        void showPTRFailed();

        void showPTRError();

        void showPTRCompleted();
    }

}
