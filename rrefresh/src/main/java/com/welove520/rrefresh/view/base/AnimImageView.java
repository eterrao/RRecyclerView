package com.welove520.rrefresh.view.base;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AnimImageView extends ImageView implements IHeaderView {
    private AnimationDrawable mAnimSequence;
    private int mCurFrame = -1;
    private int mTotalFrame = 0;
    private float mPercent;

    public AnimImageView(Context context) {
        super(context);
    }

    public AnimImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public AnimImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAnimDrawable(AnimationDrawable animDraw) {
        mAnimSequence = animDraw;
        if (mAnimSequence != null) {
            mAnimSequence.stop();
            mTotalFrame = mAnimSequence.getNumberOfFrames();
            switchToFrame(0);
        }
    }

    public void switchToFrame(int index) {
        if (mAnimSequence != null) {
            if (index >= 0 && index < mTotalFrame) {
                super.setBackgroundDrawable(mAnimSequence.getFrame(index));
                mCurFrame = index;
            }
        }
    }

    public int getCurrentFrame() {
        return mCurFrame;
    }

    public int getTotalFrame() {
        return mTotalFrame;
    }

    // 播放下一帧
    public void nextFrame() {
        switchToFrame((mCurFrame + 1) % mTotalFrame);
    }

    // 播放前一帧
    public void prevFrame() {
        switchToFrame((mCurFrame + mTotalFrame - 1) % mTotalFrame);
    }

    @Override
    public void start() {
        super.setBackgroundDrawable(mAnimSequence);
        if (mAnimSequence != null) {
            mAnimSequence.stop();
            this.post(new Runnable() {

                @Override
                public void run() {
                    mAnimSequence.start();
                }
            });
        }
    }

    @Override
    public void stop() {
        if (mAnimSequence != null) {
            mAnimSequence.stop();
        }
        switchToFrame(0);
    }

    @Override
    public boolean isRunning() {
        return mAnimSequence.isRunning();
    }

    @Override
    public void setPercent(float mCurrentDragPercent, boolean invalidate) {
        this.mPercent = mCurrentDragPercent;
        if (invalidate) {
            invalidate();
        }
    }
}
