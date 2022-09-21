package com.hoody.commonbase.customview.slidedecidable;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;


public class ClearScreenLayout extends SlideDecidableLayout {
    private static final String TAG = "GestureSwitchLayout";
    private ViewDragHelper mDragHelper;

    private boolean mEnable = true;
    private boolean mCleared = false;
    private int mHelpViewLeft;
    private View mDragHelpView;
    private View mContentView;

    public ClearScreenLayout(Context context) {
        super(context);
        init();
    }

    public ClearScreenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearScreenLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHelpViewLeft = -getWidth();
        ViewGroup.LayoutParams layoutParams = mDragHelpView.getLayoutParams();
        layoutParams.width = getWidth() * 2;
        layoutParams.height = getHeight();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, mHelpCallback);
        mDragHelpView = new View(getContext());
        addView(mDragHelpView);
    }

    /**
     * 是否可滑动
     */
    public void setClearEnable(boolean enable) {
        mEnable = enable;
    }

    /**
     * 是否可滑动
     */
    public boolean isClearEnable() {
        return mEnable;
    }

    /**
     * @return 是否已经清屏
     */
    public boolean isCleared() {
        return mCleared;
    }

    /**
     * 恢复到未清屏的状态
     */
    public void reset() {
        mHelpViewLeft = -getWidth();
        mCleared = false;
        requestLayout();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) != mDragHelpView) {
                mContentView = getChildAt(i);
            }
        }
        mDragHelpView.bringToFront();
    }

    private ViewDragHelper.Callback mHelpCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragHelpView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mHelpViewLeft = left;
            if (mHelpViewLeft <= -getWidth()) {
                mHelpViewLeft = -getWidth();
            } else if (mHelpViewLeft >= 0) {
                mHelpViewLeft = 0;
            }
            Log.d(TAG, "mHelpViewLeft==" + left);
            requestLayout();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //预测以现在的滑动速度50毫秒后会落来Y轴的坐标
            float expectFinalX = releasedChild.getX() + xvel * 0.05f;
            if (expectFinalX > -getWidth() / 2) {
                smoothToRight();
            } else {
                smoothToLeft();
            }
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return getWidth();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 0;
        }
    };

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mDragHelpView.layout(mHelpViewLeft, 0, mHelpViewLeft + getWidth() * 2, getHeight());
        mContentView.layout(mHelpViewLeft + getWidth(), 0, mHelpViewLeft + getWidth() * 2, getHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mDragHelper.shouldInterceptTouchEvent(ev);
        return false;
    }

    private boolean mInterceptTouchEvent = false;
    private float mDownX;
    private float mDownY;
    private PointF mCurrentEvPos = new PointF();

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDragHelper.continueSettling(true)) {
            return false;
        }
        boolean result = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInterceptTouchEvent = false;
                mSlidableDecider.mDecided = false;
                mDownX = event.getX();
                mDownY = event.getY();
                mCurrentEvPos.x = event.getRawX();
                mCurrentEvPos.y = event.getRawY();
                result = super.dispatchTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float adx = Math.abs(moveX - mDownX);
                float ady = Math.abs(moveY - mDownY);
                int touchSlop = mDragHelper.getTouchSlop();
                if ((adx * adx + ady * ady) < touchSlop * touchSlop) {
                    return true;
                }
                if (!mSlidableDecider.mDecided) {
                    SlideOrientation slideOrientation;
                    if (adx <= ady) {
                        if (moveY - mDownY > 0) {
                            //--->down
                            slideOrientation = SlideOrientation.SLIDE_DOWN;
                        } else {
                            //up<---
                            slideOrientation = SlideOrientation.SLIDE_UP;
                        }
                    } else {
                        if (moveX - mDownX > 0) {
                            //--->right
                            slideOrientation = SlideOrientation.SLIDE_RIGHT;
                        } else {
                            //left<---
                            slideOrientation = SlideOrientation.SLIDE_LEFT;
                        }
                    }
                    Log.d(TAG, "dispatchTouchEvent() called with: event = [" + slideOrientation + "]");
                    mInterceptTouchEvent = mSlidableDecider.slidable(mCurrentEvPos, slideOrientation);
                    mSlidableDecider.mDecided = true;
                }
                if (mInterceptTouchEvent) {
                    result = onTouchEvent(event);
                } else {
                    mDragHelper.abort();
                    result = super.dispatchTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mInterceptTouchEvent) {
                    result = onTouchEvent(event);
                } else {
                    result = super.dispatchTouchEvent(event);
                }
                break;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        if (mSlidableDecider.mDecided) {
            if (mInterceptTouchEvent) {
                mDragHelper.processTouchEvent(event);
                return true;
            }
        } else {
            mDragHelper.processTouchEvent(event);
            return true;
        }
        return false;
    }

    private void smoothToRight() {
        Log.d(TAG, "smoothToRight");
        if (!mCleared) {
            mCleared = true;
            if (mOnClearChangedListener != null) {
                mOnClearChangedListener.onClearChanged(mCleared);
            }
        }
        mDragHelper.settleCapturedViewAt(0, 0);
    }

    private void smoothToLeft() {
        Log.d(TAG, "smoothToLeft");
        if (mCleared) {
            mCleared = false;
            if (mOnClearChangedListener != null) {
                mOnClearChangedListener.onClearChanged(mCleared);
            }
        }
        mDragHelper.settleCapturedViewAt(-getWidth(), 0);
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private OnClearChangedListener mOnClearChangedListener;

    public void setOnClearChangedListener(OnClearChangedListener mOnClearChangedListener) {
        this.mOnClearChangedListener = mOnClearChangedListener;
    }

    public interface OnClearChangedListener {
        public void onClearChanged(boolean cleared);
    }

}

