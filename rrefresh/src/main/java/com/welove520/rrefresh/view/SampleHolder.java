package com.welove520.rrefresh.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.welove520.rrefresh.R;

import java.util.Map;

/**
 * Created by Administrator on 2017/7/17.
 */

public class SampleHolder extends RecyclerView.ViewHolder {
    public static final String KEY_ICON = "icon";
    public static final String KEY_COLOR = "color";

    private View mRootView;
    private ImageView mImageViewIcon;

    private Map<String, Integer> mData;

    public SampleHolder(View itemView) {
        super(itemView);

        mRootView = itemView;
        mImageViewIcon = (ImageView) itemView.findViewById(R.id.image_view_icon);
    }

    public void bindData(Map<String, Integer> data) {
        mData = data;

        mRootView.setBackgroundResource(mData.get(KEY_COLOR));
        mImageViewIcon.setImageResource(mData.get(KEY_ICON));
    }
}
