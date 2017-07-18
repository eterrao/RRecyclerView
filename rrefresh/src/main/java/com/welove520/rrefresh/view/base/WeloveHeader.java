package com.welove520.rrefresh.view.base;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.welove520.rrefresh.R;


public class WeloveHeader implements IPtrViewFactory {

    @Override
    public IPtrView createPtrView(Context context) {
        return new HeaderImpl(context);
    }

    public class HeaderImpl extends LinearLayout implements IPtrView {
        public int mMeasuredHeight;
        private RelativeLayout mContainer;
        private ImageView mArrowImageView;
        private TextView mStatusTextView;
        private AnimationDrawable heartAnimDrawable;
        private Context mContext;
        private boolean mStarted;
        private float mDragPercent;
        private boolean isDragged = false;

        public HeaderImpl(Context context) {
            this(context, null);
        }

        public HeaderImpl(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public HeaderImpl(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initView(context);
        }

        private void initView(Context context) {
            mContext = context;
            // 初始情况，设置下拉刷新view高度为0
            mContainer = (RelativeLayout) LayoutInflater.from(context)
                    .inflate(R.layout.welove_recyclerview_header, null);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);
            this.setLayoutParams(lp);
            this.setPadding(0, 0, 0, 0);

            addView(mContainer);
            setGravity(Gravity.TOP);

            mArrowImageView = (ImageView) findViewById(R.id.listview_header_arrow);
            mStatusTextView = (TextView) findViewById(R.id.refresh_status_textview);

            measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mMeasuredHeight = getMeasuredHeight();
            heartAnimDrawable = (AnimationDrawable) mArrowImageView.getDrawable();
        }

        @Override
        public void setPercent(float mCurrentDragPercent, boolean invalidate) {
            this.mDragPercent = mCurrentDragPercent;
            if (mDragPercent >= 1.0 && isDragged) {
                setState(STATE_RELEASE_TO_REFRESH);
            } else {
                setState(STATE_START);
            }
            if (invalidate) {
                invalidate();
            }
        }

        @Override
        public void performLayout(int l, int t, int r, int b) {
            layout(l, t, r, b);
        }

        @Override
        public void start() {
            mStarted = true;
            if (heartAnimDrawable != null && !heartAnimDrawable.isRunning()) {
                heartAnimDrawable.start();
            }
            setState(STATE_START);
        }

        @Override
        public void stop(boolean needRefresh) {
            mStarted = false;
            if (heartAnimDrawable != null) {
                heartAnimDrawable.stop();
            }
            if (needRefresh) {
                setState(STATE_DONE);
            }
        }

        @Override
        public boolean isRunning() {
            return mStarted;
        }

        @Override
        public void setState(int state) {
            switch (state) {
                case STATE_RELEASE_TO_REFRESH:
                    mStatusTextView.setVisibility(VISIBLE);
                    mArrowImageView.setVisibility(INVISIBLE);
                    mStatusTextView.setText(R.string.life_refres_single_feed_release_to_refresh);
                    break;
                case STATE_START:
                    mArrowImageView.setVisibility(VISIBLE);
                    mStatusTextView.setVisibility(INVISIBLE);
                    break;
                case STATE_DONE:
                    mStatusTextView.setVisibility(VISIBLE);
                    mArrowImageView.setVisibility(INVISIBLE);
                    mStatusTextView.setText(R.string.life_refres_single_feed_success);
                    break;
                case STATE_ERROR:
                    mStatusTextView.setVisibility(VISIBLE);
                    mArrowImageView.setVisibility(INVISIBLE);
                    mStatusTextView.setText(R.string.life_refres_single_feed_failed);
                default:
                    break;
            }
            invalidate();
        }

        @Override
        public void setDragStatus(boolean isDragged) {
            this.isDragged = isDragged;
        }

        @Override
        public void showPTRNormal() {

        }

        @Override
        public void showPTRFailed() {

        }

        @Override
        public void showPTRError() {

        }

        @Override
        public void showPTRCompleted() {

        }
    }
}
