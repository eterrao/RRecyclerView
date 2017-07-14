package com.welove520.rrefresh.view;

//import android.animation.ValueAnimator;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.welove520.rrefresh.R;


public class WeloveHeader extends LinearLayout implements BaseRefreshHeader {
    private RelativeLayout mContainer;
    private ImageView mArrowImageView;
    private TextView mStatusTextView;
    private int mState = STATE_NORMAL;
    private Context mContext;

    private boolean resetState = false;

    /**
     * 逐帧动画
     */
    private AnimationDrawable frameAnim = null;

    public int mMeasuredHeight;

    public WeloveHeader(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public WeloveHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        // 初始情况，设置下拉刷新view高度为0
        mContainer = (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.welove_recyclerview_header, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);

        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        setGravity(Gravity.BOTTOM);

        frameAnim = new AnimationDrawable();
        frameAnim.setOneShot(false);

        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_01), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_02), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_03), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_04), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_05), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_06), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_07), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_08), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_09), 100);
        frameAnim.addFrame(getResources().getDrawable(R.drawable.timeline_loading_10), 100);

        mArrowImageView = (ImageView) findViewById(R.id.listview_header_arrow);
        mStatusTextView = (TextView) findViewById(R.id.refresh_status_textview);

        mArrowImageView.setBackgroundDrawable(frameAnim);

        frameAnim.start();

        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();
    }

    public void setState(int state) {
        if (state == STATE_NORMAL) {
            if (resetState) {
                mArrowImageView.setVisibility(GONE);
            } else {
                mArrowImageView.setVisibility(VISIBLE);
            }
        }
        if (state == mState) return;

        mStatusTextView.setVisibility(GONE);

        switch (state) {
            case STATE_NORMAL:
                if (mState == STATE_RELEASE_TO_REFRESH) {
                }
                break;
            case STATE_RELEASE_TO_REFRESH:
                if (mState != STATE_RELEASE_TO_REFRESH) {
                }
                break;
            case STATE_REFRESHING:
                break;
            case STATE_DONE:
                mStatusTextView.setVisibility(VISIBLE);
                mArrowImageView.setVisibility(GONE);
                mStatusTextView.setText(R.string.life_refres_single_feed_success);
                break;
            case STATE_FAULT:
                mStatusTextView.setVisibility(VISIBLE);
                mArrowImageView.setVisibility(GONE);
                mStatusTextView.setText(R.string.life_refres_single_feed_failed);
            default:
        }

        mState = state;
    }

    public int getState() {
        return mState;
    }

    @Override
    public void refreshComplate() {
        setState(STATE_DONE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                reset();
            }
        }, 500);
    }

    @Override
    public void refreshFault() {
        setState(STATE_FAULT);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                reset();
            }
        }, 500);
    }



    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer
                .getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        int height = 0;
        LayoutParams lp = (LayoutParams) mContainer
                .getLayoutParams();
        height = lp.height;
        return height;
    }

    @Override
    public void onMove(float delta) {
        if (getVisiableHeight() > 0 || delta > 0) {
            setVisiableHeight((int) delta + getVisiableHeight());
            if (mState <= STATE_RELEASE_TO_REFRESH) { // 未处于刷新状态，更新箭头
                if (getVisiableHeight() > mMeasuredHeight) {
                    setState(STATE_RELEASE_TO_REFRESH);
                } else {
                    resetState = false;
                    setState(STATE_NORMAL);
                }
            }
        }
    }

    @Override
    public boolean releaseAction() {
        boolean isOnRefresh = false;
        int height = getVisiableHeight();
        if (height == 0) // not visible.
            isOnRefresh = false;

        if (getVisiableHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
            setState(STATE_REFRESHING);
            isOnRefresh = true;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
            //return;
        }
        int destHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mState == STATE_REFRESHING) {
            destHeight = mMeasuredHeight;
        }
        smoothScrollTo(destHeight);

        return isOnRefresh;
    }

    public void reset() {
        smoothScrollTo(0);
        resetState = true;
        setState(STATE_NORMAL);
    }

    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisiableHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisiableHeight((Integer) animation.getAnimatedValue());
            }
        });
        animator.start();
    }
}
