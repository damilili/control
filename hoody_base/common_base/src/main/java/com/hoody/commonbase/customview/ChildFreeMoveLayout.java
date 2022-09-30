package com.hoody.commonbase.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.hoody.commonbase.log.Logger;
import com.hoody.commonbase.util.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ChildFreeMoveLayout extends FrameLayout {
    private String TAG = ChildFreeMoveLayout.class.getName();
    private ViewDragHelper mDragHelper;
    private View mTargetView;
    private Map<View, ChildViewMoveInfo> mChildViewInfoMap;
    private ArrayList<OnViewDragStateChangeListener> mOnViewDragStateChangeListeners;
    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child.equals(mTargetView);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            Logger.d("ChildFreeMoveLayout", "onViewDragStateChanged() called with: state = [" + state + "]");
            switch (state) {
                case ViewDragHelper.STATE_DRAGGING:
                    if (mOnViewDragStateChangeListeners != null) {
                        for (OnViewDragStateChangeListener mOnViewDragStateChangeListener : mOnViewDragStateChangeListeners) {
                            mOnViewDragStateChangeListener.onStartDrag(mTargetView);
                        }
                    }
                    if (mDrawPoint == null) {
                        mDrawPoint = new Point();
                    }
                    break;
                case ViewDragHelper.STATE_IDLE:
                    int finalLeft = mTargetView.getLeft();
                    int finalTop = mTargetView.getTop();
                    if (mChildViewInfoMap != null) {
                        ChildViewMoveInfo childViewMoveInfo = mChildViewInfoMap.get(mTargetView);
                        if (childViewMoveInfo != null) {
                            if (childViewMoveInfo.adsorbType == 1) {
                                finalLeft = childViewMoveInfo.adsorbMargin;
                            } else if (childViewMoveInfo.adsorbType == 2) {
                                finalLeft = getWidth() - mTargetView.getWidth() - childViewMoveInfo.adsorbMargin;
                            } else if (childViewMoveInfo.adsorbType == 3) {
                                if (mTargetView.getX() + mTargetView.getWidth() / 2 < getWidth() / 2) {
                                    finalLeft = childViewMoveInfo.adsorbMargin;
                                } else {
                                    finalLeft = getWidth() - mTargetView.getWidth() - childViewMoveInfo.adsorbMargin;
                                }
                            } else if (childViewMoveInfo.adsorbType == 4) {
                                if (mDrawPoint != null) {
                                    finalLeft = mDrawPoint.x;
                                    finalTop = mDrawPoint.y;
                                    mDrawPoint = null;
                                }
                            }
                        }
                    }
                    boolean continueSliding = mDragHelper.smoothSlideViewTo(mTargetView, finalLeft, finalTop);
                    if (continueSliding) {
                        ViewCompat.postInvalidateOnAnimation(ChildFreeMoveLayout.this);
                    } else {
                        LayoutParams layoutParams = (LayoutParams) mTargetView.getLayoutParams();
                        layoutParams.leftMargin = mTargetView.getLeft();
                        layoutParams.topMargin = mTargetView.getTop();
                        requestLayout();
                        mTargetView = null;
                    }
                    break;

            }
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            if (mDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING && mOnViewDragStateChangeListeners != null) {
                for (OnViewDragStateChangeListener mOnViewDragStateChangeListener : mOnViewDragStateChangeListeners) {
                    mOnViewDragStateChangeListener.onDrag(mTargetView, left, top);
                }
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (mOnViewDragStateChangeListeners != null) {
                for (OnViewDragStateChangeListener mOnViewDragStateChangeListener : mOnViewDragStateChangeListeners) {
                    mOnViewDragStateChangeListener.onDragEnd(mTargetView);
                }
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left <= 0) {
                return 0;
            }
            int viewHorizontalDragRange = getViewHorizontalDragRange(child);
            if (left >= viewHorizontalDragRange) {
                return viewHorizontalDragRange;
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (top <= 0) {
                return 0;
            }
            int viewVerticalDragRange = getViewVerticalDragRange(child);
            if (top >= viewVerticalDragRange) {
                return viewVerticalDragRange;
            }
            return top;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return getWidth() - child.getWidth();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return getHeight() - child.getHeight();
        }
    };
    private Paint mPaint;


    public ChildFreeMoveLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public ChildFreeMoveLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChildFreeMoveLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private final int SplitCount = 24;
    private int mSplitLength;
    private Point mDrawPoint;

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.BLUE);
        mDragHelper = ViewDragHelper.create(this, callback);
        setWillNotDraw(false);
        mSplitLength = DeviceInfo.WIDTH / SplitCount;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    private float mDownX;
    private float mDownY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mDragHelper.continueSettling(true)) {
            return false;
        }
        boolean result = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                result = super.dispatchTouchEvent(ev);
                Logger.d(TAG, "dispatchTouchEvent() called with: result = " + result);
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if (child != null) {
                        boolean touchPointInView = isTouchPointInView(child, ev.getX(), ev.getY());
                        if (touchPointInView) {
                            if (mChildViewInfoMap != null) {
                                ChildViewMoveInfo childViewMoveInfo = mChildViewInfoMap.get(child);
                                if (childViewMoveInfo != null && childViewMoveInfo.canMove) {
                                    mTargetView = child;
                                }
                            } else {
                                mTargetView = child;
                            }
                            break;
                        }
                    }
                }
                Logger.d(TAG, "dispatchTouchEvent() called with: result = " + (mTargetView == null));
                if (mTargetView != null) {
                    return true;
                }
                return result;
            case MotionEvent.ACTION_MOVE:
                if (mDrawPoint != null) {
                    float x = mTargetView.getX();
                    float y = mTargetView.getY();
                    int x1 = (int) x / mSplitLength * mSplitLength;
                    int x2 = (int) (x + mTargetView.getWidth()) / mSplitLength * mSplitLength + mSplitLength;
                    int y1 = (int) y / mSplitLength * mSplitLength;
                    if (x - x1 < x2 - (x + mTargetView.getWidth())) {
                        mDrawPoint.set(x1, y1);
                    } else {
                        mDrawPoint.set(x2 - mTargetView.getWidth(), y1);
                    }
                    postInvalidate();
                }
                return super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mTargetView != null) {
                    if (ev.getX() == mDownX && ev.getY() == mDownY) {
                        Logger.d(TAG, "dispatchTouchEvent() called with: ev = 212");
                        mTargetView = null;
                        mDragHelper.cancel();
                        postInvalidate();
                        return super.dispatchTouchEvent(ev);
                    } else {
                        Logger.d(TAG, "dispatchTouchEvent() called with: ev = 200");
                        mDragHelper.processTouchEvent(ev);
                        return true;
                    }
                } else {
                    return super.dispatchTouchEvent(ev);
                }
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Logger.d(TAG, "onTouchEvent() called with: event = [" + event.getAction() + "]");
        if (mTargetView != null) {
            mDragHelper.processTouchEvent(event);
            return true;
        }
        boolean result = super.onTouchEvent(event);
        Logger.d(TAG, "onTouchEvent() called with: result = [" + result + "]");
        return result;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (mChildViewInfoMap != null) {
            mChildViewInfoMap.remove(child);
        }
        if (mOnViewDragStateChangeListeners != null) {
            mOnViewDragStateChangeListeners.remove(child);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mTargetView != null) {
            LayoutParams layoutParams = (LayoutParams) mTargetView.getLayoutParams();
            layoutParams.leftMargin = mTargetView.getLeft();
            layoutParams.topMargin = mTargetView.getTop();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTargetView != null) {
            mPaint.setColor(Color.parseColor("#333333"));
            mPaint.setStrokeWidth(2);
            for (int i = 1; i < SplitCount; i++) {
                canvas.drawLine(i * mSplitLength, 0, i * mSplitLength, DeviceInfo.HEIGHT, mPaint);
            }
            int i = 0;
            while (true) {
                canvas.drawLine(0, (i + 1) * mSplitLength, DeviceInfo.WIDTH, (i + 1) * mSplitLength, mPaint);
                i++;
                if ((i + 1) * mSplitLength > DeviceInfo.HEIGHT) {
                    break;
                }
            }
            if (mDrawPoint != null) {
                mPaint.setStrokeWidth(2);
                mPaint.setColor(Color.WHITE);
                canvas.drawPoint(mDrawPoint.x, mDrawPoint.y, mPaint);
                canvas.drawRect(mDrawPoint.x, mDrawPoint.y, mDrawPoint.x + mTargetView.getWidth(), mDrawPoint.y + mTargetView.getHeight(), mPaint);
            }
        }
    }

    protected boolean isTouchPointInView(View view, float x, float y) {
        if (view == null || !view.isShown()) {
            return false;
        }
        int left = (int) view.getLeft();
        int top = (int) view.getTop();
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (y >= top && y <= bottom && x >= left
                && x <= right) {
            return true;
        }
        return false;
    }

    /**
     * 为指定的子view绑定移动信息
     *
     * @param view
     * @param childViewMoveInfo
     */
    public void bindChildViewMoveInfo(View view, ChildViewMoveInfo childViewMoveInfo) {
        if (view == null) {
            return;
        }
        int index = 0;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == view) {
                index = i;
                removeView(view);
                break;
            }
        }
        addView(view, index, childViewMoveInfo);
    }

    /**
     * 添加子view 并且配置其移动相关信息
     *
     * @param view
     * @param childViewMoveInfo
     */
    public void addView(View view, int index, ChildViewMoveInfo childViewMoveInfo) {
        if (view == null) {
            return;
        }
        if (mChildViewInfoMap == null) {
            mChildViewInfoMap = new HashMap<>();
        }
        if (childViewMoveInfo != null) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(layoutParams);
            }
            if (childViewMoveInfo.adsorbType == 2) {
                layoutParams.gravity = Gravity.END;
            }
            if (childViewMoveInfo.adsorbType == 1) {
                layoutParams.gravity = Gravity.START;
            }
            if (childViewMoveInfo.adsorbType == 1 || childViewMoveInfo.adsorbType == 3) {
                layoutParams.leftMargin = childViewMoveInfo.adsorbMargin;
            }
            if (childViewMoveInfo.adsorbType == 2 || childViewMoveInfo.adsorbType == 3) {
                layoutParams.rightMargin = childViewMoveInfo.adsorbMargin;
            }
            mChildViewInfoMap.put(view, childViewMoveInfo);
        }
        addView(view, index);
    }

    /**
     * 添加子view 并且配置其移动相关信息
     *
     * @param view
     * @param childViewMoveInfo
     */
    public void addView(View view, ChildViewMoveInfo childViewMoveInfo) {
        addView(view, -1, childViewMoveInfo);
    }

    /**
     * 设置拖动监听
     *
     * @param onViewDragStateChangeListener
     */
    public void setOnViewDragStateChangeListener(OnViewDragStateChangeListener onViewDragStateChangeListener) {
        if (onViewDragStateChangeListener == null) {
            return;
        }
        if (mOnViewDragStateChangeListeners == null) {
            mOnViewDragStateChangeListeners = new ArrayList<>();
        }
        mOnViewDragStateChangeListeners.add(onViewDragStateChangeListener);
    }

    public ChildViewMoveInfo getMoveInfo(View view) {
        return mChildViewInfoMap.get(view);
    }

    public void startMoveView(View v) {
        ChildViewMoveInfo childViewMoveInfo = mChildViewInfoMap.get(v);
        if (childViewMoveInfo != null) {
            mTargetView = v;
            invalidate();
        }
    }

    public interface OnViewDragStateChangeListener {

        void onDragEnd(View targetView);

        void onStartDrag(View targetView);

        void onDrag(View targetView, int left, int top);

    }

    public static class ChildViewMoveInfo {
        //1:向左吸附，2:向右吸附,3:就近吸附（可左可右）4：就近格点吸附
        public int adsorbType;
        //吸附后的边距
        public int adsorbMargin;
        //是不是可以移动
        public boolean canMove = true;
    }
}
