package com.welove520.rrefresh.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import com.welove520.rrefresh.R;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

/**
 * Created by Raomengyang on 17-7-13.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class RRefreshView extends ViewGroup implements IRRefreshView {

    private static final String LOG_TAG = RRefreshView.class.getSimpleName();
    private static final float DECELERATE_INTERPOLATION_FACTOR = 1f;
    private static final float DRAG_RATE = .5f;
    private static final int DRAG_MAX_DISTANCE = 165;

    private static final int SCALE_DOWN_DURATION = 150;

    private static final int ALPHA_ANIMATION_DURATION = 300;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private static final int ANIMATE_TO_START_DURATION = 200;

    private IPtrViewFactory ptrViewFactory;
    private IPtrViewFactory.IPtrView ptrView;
    private View mTarget; // the target of the gesture
    private View mHeaderView;
    private View mFooterView;


    private DecelerateInterpolator mDecelerateInterpolator;
    private int mTouchSlop;
    private int mTotalDragDistance;
    private boolean refreshEnabled = false;
    private boolean loadMoreEnabled = false;
    private boolean mRefreshing;
    private boolean mIsBeingDragged;
    private boolean mReturningToStart;

    private float currentOffset;
    private float totalOffset;

    private float mInitialMotionY;
    private float mInitialDownY;
    private int mActivePointerId = INVALID_POINTER;
    protected int mOriginalOffsetTop;
    private int mCurrentTargetOffsetTop;
    private boolean mNotify;
    private boolean mScale;
    protected int mFrom;

    private Animation mScaleAnimation;

    private Animation mScaleDownAnimation;

    private Animation mAlphaStartAnimation;

    private Animation mAlphaMaxAnimation;

    private Animation mScaleDownToStartAnimation;


    public RRefreshView(Context context) {
        this(context, null);
    }

    public RRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RRefreshView);
        refreshEnabled = a.getBoolean(R.styleable.RRefreshView_rrv_refresh_enabled, true);
        loadMoreEnabled = a.getBoolean(R.styleable.RRefreshView_rrv_load_more_enabled, true);
        a.recycle();
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = Utils.convertDpToPixel(context, DRAG_MAX_DISTANCE);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        createHeaderView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        // the absolute offset has to take into account that the circle starts at an offset
        mSpinnerOffsetEnd = (int) (DRAG_MAX_DISTANCE * metrics.density);
        mTotalDragDistance = mSpinnerOffsetEnd;
        mOriginalOffsetTop = mCurrentTargetOffsetTop /*= -mCircleDiameter*/;
        moveToStart(1.0f);

    }

    private void createHeaderView() {
        mHeaderView = LayoutInflater.from(getContext()).inflate(R.layout.layout_header_view, null);
        mFooterView = LayoutInflater.from(getContext()).inflate(R.layout.layout_footer_view, null);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ensureLayout();
    }

    private void ensureLayout() {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = mHeaderView.getMeasuredWidth();
        int circleHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop,
                (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mHeaderView.measure(MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp()
                || mRefreshing /*|| mNestedScrollInProgress*/) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mHeaderView.getTop(), true);
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
//                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mHeaderView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mHeaderView.bringToFront();
        ViewCompat.offsetTopAndBottom(mHeaderView, offset);
        mCurrentTargetOffsetTop = mHeaderView.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
//            mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp()
                || mRefreshing/* || mNestedScrollInProgress*/) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0) {
//                        moveSpinner(overscrollTop);
                    } else {
                        return false;
                    }
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG,
                            "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishSpinner(overscrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }

    private void finishSpinner(float overscrollTop) {
        if (overscrollTop > mTotalDragDistance) {
            setRefreshing(true, true /* notify */);
        } else {
            // cancel refresh
            mRefreshing = false;
//            mProgress.setStartEndTrim(0f, 0f);
            Animation.AnimationListener listener = null;
            if (!mScale) {
                listener = new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (!mScale) {
                            startScaleDownAnimation(null);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                };
            }
            animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
//            mProgress.showArrow(false);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }

    private OnRefreshListener mListener;
    private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                // Make sure the progress view is fully visible
//                mProgress.setAlpha(MAX_ALPHA);
//                mProgress.start();
                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }
                mCurrentTargetOffsetTop = mHeaderView.getTop();
            } else {
                reset();
            }
        }
    };

    void reset() {
        mHeaderView.clearAnimation();
//        mProgress.stop();
        mHeaderView.setVisibility(View.GONE);
        // Return the circle to its start position
        if (mScale) {
//            setAnimationProgress(0 /* animation complete and view is hidden */);
        } else {
            setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop,
                    true /* requires update */);
        }
        mCurrentTargetOffsetTop = mHeaderView.getTop();
    }


    void startScaleDownAnimation(Animation.AnimationListener listener) {
        mScaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
//                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
//        mHeaderView.setAnimationListener(listener);
        mHeaderView.clearAnimation();
        mHeaderView.startAnimation(mScaleDownAnimation);
    }

    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
//            mHeaderView.setAnimationListener(listener);
        }
        mHeaderView.clearAnimation();
        mHeaderView.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        if (mScale) {
            // Scale the item back down
            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = from;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if (listener != null) {
//                mHeaderView.setAnimationListener(listener);
            }
            mHeaderView.clearAnimation();
            mHeaderView.startAnimation(mAnimateToStartPosition);
        }
    }

    private int mSpinnerOffsetEnd;
    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            int endTarget = 0;
            endTarget = mSpinnerOffsetEnd;
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mHeaderView.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
//            mProgress.setArrowScale(1 - interpolatedTime);
        }
    };

    void moveToStart(float interpolatedTime) {
        int targetTop = 0;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mHeaderView.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private void startScaleDownReturnToStartAnimation(int from,
                                                      Animation.AnimationListener listener) {
        mFrom = from;
//        if (isAlphaUsedForScale()) {
//            mStartingScale = mProgress.getAlpha();
//        } else {
//            mStartingScale = ViewCompat.getScaleX(mHeaderView);
//        }
        mScaleDownToStartAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
//                float targetScale = (mStartingScale + (-mStartingScale  * interpolatedTime));
//                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
//        if (listener != null) {
//            mHeaderView.setAnimationListener(listener);
//        }
        mHeaderView.clearAnimation();
        mHeaderView.startAnimation(mScaleDownToStartAnimation);
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     */
    private boolean isAlphaUsedForScale() {
        return android.os.Build.VERSION.SDK_INT < 11;
    }


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (!isEnabled() || mContent == null || mHeaderView == null) {
//            return dispatchTouchEventSupper(ev);
//        }
//        int action = ev.getAction();
//        switch (action) {
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                mPtrIndicator.onRelease();
//                if (mPtrIndicator.hasLeftStartPosition()) {
//                    if (DEBUG) {
//                        PtrCLog.d(LOG_TAG, "call onRelease when user release");
//                    }
//                    onRelease(false);
//                    if (mPtrIndicator.hasMovedAfterPressedDown()) {
//                        sendCancelEvent();
//                        return true;
//                    }
//                    return dispatchTouchEventSupper(ev);
//                } else {
//                    return dispatchTouchEventSupper(ev);
//                }
//
//            case MotionEvent.ACTION_DOWN:
//                mHasSendCancelEvent = false;
//                mPtrIndicator.onPressDown(ev.getX(), ev.getY());
//
//                mScrollChecker.abortIfWorking();
//
//                mPreventForHorizontal = false;
//                // The cancel event will be sent once the position is moved.
//                // So let the event pass to children.
//                // fix #93, #102
//                dispatchTouchEventSupper(ev);
//                return true;
//
//            case MotionEvent.ACTION_MOVE:
//                mLastMoveEvent = ev;
//                mPtrIndicator.onMove(ev.getX(), ev.getY());
//                float offsetX = mPtrIndicator.getOffsetX();
//                float offsetY = mPtrIndicator.getOffsetY();
//
//                if (mDisableWhenHorizontalMove && !mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop && Math.abs(offsetX) > Math.abs(offsetY))) {
//                    if (mPtrIndicator.isInStartPosition()) {
//                        mPreventForHorizontal = true;
//                    }
//                }
//                if (mPreventForHorizontal) {
//                    return dispatchTouchEventSupper(ev);
//                }
//
//                boolean moveDown = offsetY > 0;
//                boolean moveUp = !moveDown;
//                boolean canMoveUp = mPtrIndicator.hasLeftStartPosition();
//
//                if (DEBUG) {
//                    boolean canMoveDown = mPtrHandler != null && mPtrHandler.checkCanDoRefresh(this, mContent, mHeaderView);
//                    PtrCLog.v(LOG_TAG, "ACTION_MOVE: offsetY:%s, currentPos: %s, moveUp: %s, canMoveUp: %s, moveDown: %s: canMoveDown: %s", offsetY, mPtrIndicator.getCurrentPosY(), moveUp, canMoveUp, moveDown, canMoveDown);
//                }
//
//                // disable move when header not reach top
//                if (moveDown && mPtrHandler != null && !mPtrHandler.checkCanDoRefresh(this, mContent, mHeaderView)) {
//                    return dispatchTouchEventSupper(ev);
//                }
//
//                if ((moveUp && canMoveUp) || moveDown) {
//                    movePos(offsetY);
//                    return true;
//                }
//        }
//        return dispatchTouchEventSupper(ev);
//    }


    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    public void setHeaderView(IPtrViewFactory ptrViewFactory) {
        this.ptrViewFactory = ptrViewFactory;
        if (ptrViewFactory != null) {
            ptrView = ptrViewFactory.createPtrView();
        }
    }

    @Override
    public void setFooterView(ILoadMoreViewFactory loadMoreViewFactory) {

    }

    public void refresh() {

    }

    public void loadMore() {

    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        /**
         * Called when a swipe gesture triggers a refresh.
         */
        void onRefresh();
    }
}
