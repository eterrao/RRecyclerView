package com.welove520.rrefresh.view.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;

import com.welove520.rrefresh.R;
import com.welove520.rrefresh.view.Utils;
import com.welove520.rrefresh.view.handler.GridViewHandler;
import com.welove520.rrefresh.view.handler.ListViewHandler;
import com.welove520.rrefresh.view.handler.LoadMoreHandler;
import com.welove520.rrefresh.view.handler.RecyclerViewHandler;
import com.welove520.rrefresh.view.listener.OnLoadMoreListener;
import com.welove520.rrefresh.view.listener.OnRefreshListener;
import com.welove520.rrefresh.view.listener.OnScrollBottomListener;
import com.welove520.rrefresh.view.network.DataStatusLayoutImpl;
import com.welove520.rrefresh.view.network.NetworkStatusLayoutImpl;
import com.welove520.rrefresh.view.network.StatusLayout;

/**
 * Created by Raomengyang on 17-7-13.
 * Email    : ericrao@welove-inc.com
 * Desc     :
 * Version  : 1.0
 */

public class RRefreshView extends ViewGroup implements IRRefreshView, StatusLayout.DataStatusLayout, StatusLayout.NetworkStatusLayout {

    private static final String TAG = RRefreshView.class.getSimpleName();
    private static final int MAX_OFFSET_ANIMATION_DURATION = 700;
    private static final int DRAG_MAX_DISTANCE = 80;
    private static final int INVALID_POINTER = -1;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final float DRAG_RATE = 0.5f;
    private View mTarget;
    private IHeaderView mRefreshView;
    private NetworkStatusLayout networkStatusView;
    private DataStatusLayout dataStatusView;
    private Interpolator mDecelerateInterpolator;
    private int mTouchSlop;
    private int mTotalDragDistance;
    private int mCurrentOffsetTop;
    private int mActivePointerId;
    private int mFrom;
    private int mTargetPaddingTop;
    private int mTargetPaddingBottom;
    private int mTargetPaddingRight;
    private int mTargetPaddingLeft;
    private float mCurrentDragPercent;
    private float mInitialMotionY;
    private float mFromDragPercent;
    private boolean mRefreshing;
    private boolean mIsBeingDragged;
    private boolean mNotify;
    private boolean isLoadingMore = false;
    private boolean isAutoLoadMoreEnable = false;
    private boolean isLoadMoreEnable = false;
    private boolean hasInitLoadMoreView = false;
    private OnRefreshListener mListener;
    private OnLoadMoreListener mOnLoadMoreListener;
    private ILoadMoreViewFactory loadMoreViewFactory;
    private ILoadMoreViewFactory.ILoadMoreView mLoadMoreView;
    private LoadMoreHandler mLoadMoreHandler;
    private View mContentView;

    private Animation mAnimateToStartPosition = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };
    private Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            mCurrentOffsetTop = mTarget.getTop();
        }
    };
    private Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            moveToCorrect(interpolatedTime);
        }
    };
    private OnScrollBottomListener onScrollBottomListener = new OnScrollBottomListener() {
        @Override
        public void onScorllBootom() {
            if (isAutoLoadMoreEnable && isLoadMoreEnable && !isLoadingMore()) {
                // can check network here
                loadMore();
            }
        }
    };
    private OnClickListener onClickLoadMoreListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isLoadMoreEnable && !isLoadingMore()) {
                loadMore();
            }
        }
    };

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

    private void moveToCorrect(float interpolatedTime) {
        int targetTop;
        int endTarget = mTotalDragDistance;
        targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
        int offset = targetTop - mTarget.getTop();

        mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime;
        if (mRefreshView != null) {
            mRefreshView.setPercent(mCurrentDragPercent, false);
        }

        setTargetOffsetTop(offset, false /* requires update */);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RRefreshView);
        a.recycle();

        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = Utils.convertDpToPixel(context, DRAG_MAX_DISTANCE);

        initView(context);
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

    }

    public void initView(Context context) {
        setRefreshing(false);
        mRefreshView = new WeloveHeader().createPtrView(getContext());
        addView((View) mRefreshView);

        networkStatusView = new NetworkStatusLayoutImpl(getContext());
        dataStatusView = new DataStatusLayoutImpl(getContext());
        View container = LayoutInflater.from(context).inflate(R.layout.layout_data_status, null);
        setDataStatusContainer(container);
    }

    /**
     * This method sets padding for the refresh (progress) view.
     */
    public void setRefreshViewPadding(int left, int top, int right, int bottom) {
        mRefreshView.setPadding(left, top, right, bottom);
    }

    public int getTotalDragDistance() {
        return mTotalDragDistance;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, " onMeasure = " + " ,widthMeasureSpec = " + widthMeasureSpec + " ,heightMeasureSpec = " + heightMeasureSpec);

        ensureTarget();
        if (mTarget == null) {
            return;
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        mTarget.measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshView.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ensureTarget();
        if (mTarget == null) {
            return;
        }

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        float dragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));

        mTarget.layout(left, top + mCurrentOffsetTop, left + width - right, top + height - bottom + mCurrentOffsetTop);
        mRefreshView.performLayout(left, top, (int) ((left + width - right) * dragPercent), top + height - bottom);
        startLoadingAnim();
    }

    private void ensureTarget() {
        if (mTarget != null) {
            return;
        }
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != mRefreshView) {
                    mTarget = child;
                    mTargetPaddingBottom = mTarget.getPaddingBottom();
                    mTargetPaddingTop = mTarget.getPaddingTop();
                    mTargetPaddingLeft = mTarget.getPaddingLeft();
                    mTargetPaddingRight = mTarget.getPaddingRight();
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!isEnabled() || canChildScrollUp() || mRefreshing) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTop(0, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, " onInterceptTouchEvent ACTION_MOVE  ");

                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mRefreshing) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev);
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                requestLayout();
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = y - mInitialMotionY;
                final float scrollTop = yDiff * DRAG_RATE;
                mCurrentDragPercent = scrollTop / mTotalDragDistance;
                if (mCurrentDragPercent < 0) {
                    return false;
                }
                float boundedDragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));
                float extraOS = Math.abs(scrollTop) - mTotalDragDistance;
                float slingshotDist = mTotalDragDistance;
                float tensionSlingshotPercent = Math.max(0,
                        Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                        (tensionSlingshotPercent / 4), 2)) * 2f;
                float extraMove = (slingshotDist) * tensionPercent / 2;
                int targetY = (int) ((slingshotDist * boundedDragPercent) + extraMove);
                if (mRefreshView != null) {
                    mRefreshView.setPercent(mCurrentDragPercent, true);
                }
                setTargetOffsetTop(targetY - mCurrentOffsetTop, true);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                if (overScrollTop > mTotalDragDistance) {
                    setRefreshing(true, true);
                } else {
                    mRefreshing = false;
                    animateOffsetToStartPosition();
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    private void moveToStart(float interpolatedTime) {
        stopLoadingAnim();
        int targetTop = mFrom - (int) (mFrom * interpolatedTime);
        float targetPercent = mFromDragPercent * (1.0f - interpolatedTime);
        int offset = targetTop - mTarget.getTop();

        mCurrentDragPercent = targetPercent;
        if (mRefreshView != null) {
            mRefreshView.requestLayout();
        }
        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTargetPaddingBottom + targetTop);
        setTargetOffsetTop(offset, false);
    }

    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                if (mRefreshView != null) {
                    mRefreshView.setPercent(1f, true);
                }
                animateOffsetToCorrectPosition();
            } else {
                animateOffsetToStartPosition();
            }
        }
    }

    private void animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;
        long animationDuration = Math.abs((long) (MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent));

        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(animationDuration);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(mToStartListener);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToStartPosition);
    }

    private void animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;

        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToCorrectPosition);

        if (mRefreshing) {
            if (mNotify) {
                if (mListener != null) {
                    mListener.onRefresh();
                }
            }
        } else {
            animateOffsetToStartPosition();
        }
        mCurrentOffsetTop = mTarget.getTop();
        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTotalDragDistance);
    }

    private void stopLoadingAnim() {
        if (mRefreshView != null) {
            mRefreshView.stop(false);
        }
    }

    private void startLoadingAnim() {
        if (mRefreshView != null) {
            if (!mRefreshView.isRunning()) {
                mRefreshView.start();
            }
        }
    }

    private void setTargetOffsetTop(int offset, boolean requiresUpdate) {
        mTarget.offsetTopAndBottom(offset);
        mCurrentOffsetTop = mTarget.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
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
    public void setNetworkStatus(int status) {

    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mListener = onRefreshListener;
    }

    public void setAutoLoadMoreEnable(boolean isAutoLoadMoreEnable) {
        this.isAutoLoadMoreEnable = isAutoLoadMoreEnable;
    }

    @Override
    public void setHeaderView(IPtrViewFactory ptrViewFactory) {
        if (null == ptrViewFactory) return;
        if (mRefreshView != null) {
            removeView((View) mRefreshView);
            mRefreshView = null;
        }
        mRefreshView = ptrViewFactory.createPtrView(getContext());
        addView((View) mRefreshView);
    }

    @Override
    public void setFooterView(ILoadMoreViewFactory factory) {
        if (null == factory || (null != loadMoreViewFactory && loadMoreViewFactory == factory)) {
            return;
        }
        loadMoreViewFactory = factory;
        if (hasInitLoadMoreView) {
            mLoadMoreHandler.removeFooter();
            mLoadMoreView = loadMoreViewFactory.createLoadMoreView();
            hasInitLoadMoreView = mLoadMoreHandler.handleSetAdapter(mContentView, mLoadMoreView,
                    onClickLoadMoreListener);
            if (!isLoadMoreEnable) {
                mLoadMoreHandler.removeFooter();
            }
        }
    }

    private View getContentView() {
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof RecyclerView) {
                    return child;
                } else if (child instanceof ListView) {
                    return child;
                } else if (child instanceof GridView) {
                    return child;
                }
            }
        }
        return null;
    }

    public boolean isLoadMoreEnable() {
        return isLoadMoreEnable;
    }

    public void setLoadMoreEnable(boolean loadMoreEnable) {
        if (this.isLoadMoreEnable == loadMoreEnable) {
            return;
        }
        this.isLoadMoreEnable = loadMoreEnable;
        if (!hasInitLoadMoreView && isLoadMoreEnable) {
            mContentView = getContentView();
            if (null == loadMoreViewFactory) {
                loadMoreViewFactory = new DefaultLoadMoreViewFooter();
            }
            mLoadMoreView = loadMoreViewFactory.createLoadMoreView();

            if (null == mLoadMoreHandler) {
                if (mContentView instanceof GridView) {
                    mLoadMoreHandler = new GridViewHandler();
                } else if (mContentView instanceof AbsListView) {
                    mLoadMoreHandler = new ListViewHandler();
                } else if (mContentView instanceof RecyclerView) {
                    mLoadMoreHandler = new RecyclerViewHandler();
                }
            }

            if (null == mLoadMoreHandler) {
                throw new IllegalStateException("unSupported contentView !");
            }

            hasInitLoadMoreView = mLoadMoreHandler.handleSetAdapter(mContentView, mLoadMoreView,
                    onClickLoadMoreListener);
            mLoadMoreHandler.setOnScrollBottomListener(mContentView, onScrollBottomListener);
            return;
        }

        if (hasInitLoadMoreView) {
            if (isLoadMoreEnable) {
                mLoadMoreHandler.addFooter();
            } else {
                mLoadMoreHandler.removeFooter();
            }
        }
    }

    void loadMore() {
        isLoadingMore = true;
        mLoadMoreView.showLoading();
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore();
        }
    }

    public void loadMoreComplete(boolean hasMore) {
        isLoadingMore = false;
        isLoadMoreEnable = hasMore;
        if (hasMore) {
            mLoadMoreView.showLoadMoreNormal();
        } else {
            setNoMoreData();
        }
    }

    public void setNoMoreData() {
        mLoadMoreView.showLoadMoreNormal();
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.mOnLoadMoreListener = loadMoreListener;
    }

    @Override
    public void showNetwork(int status) {
        if (networkStatusView != null) {
            removeView((View) networkStatusView);
            addView((View) networkStatusView);
            networkStatusView.showNetwork(status);
        }
    }

    @Override
    public void hideNetwork() {
        if (networkStatusView != null) {
            networkStatusView.hideNetwork();
        }
    }

    @Override
    public void showData(int status) {
        if (dataStatusView != null) {
            removeView((View) dataStatusView);
            addView((View) dataStatusView, 0);
            dataStatusView.showData(status);
        }
    }

    @Override
    public void hideData() {
        if (dataStatusView != null) {
            dataStatusView.hideData();
        }
    }

    @Override
    public void setDataStatusContainer(View container) {
        dataStatusView.setDataStatusContainer(container);
    }
}
