package com.hoody.commonbase.customview.slidedecidable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hoody.commonbase.R;

import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;



public class SwipeBackLayout extends SlideDecidableLayout {
    private static final String TAG = "SwipeBackLayout";
    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 10; // dips per second

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    private static final int FULL_ALPHA = 255;

    /**
     * A view is not currently being dragged or animating as a result of a
     * fling/snap.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * A view is currently being dragged. The position is currently changing as
     * a result of user input or simulated user input.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * A view is currently settling into place as a result of a fling or
     * predefined non-interactive motion.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    private static final int OVERSCROLL_DISTANCE = 10;

    private boolean mSwipeEnable = true;

    private View mContentView;

    private ViewDragHelper mDragHelper;

    private int mContentLeft;

    private int mContentTop;

    private SwipeListener mSwipeListener;

    private Drawable mShadowLeft;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private boolean mInLayout;

    public boolean isClosed() {
        return mClosed;
    }

    private boolean mClosed = false;

    private Rect mTmpRect = new Rect();

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.XCSwipeBackLayoutStyle);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mDragHelper = ViewDragHelper.create(this, new ViewDragCallback());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XCSwipeBackLayout, defStyle,
                R.style.XCSwipeBackLayout);

        int shadowLeft = a.getResourceId(R.styleable.XCSwipeBackLayout_shadow_left,
                R.mipmap.shadow_left);
        setShadow(getResources().getDrawable(shadowLeft));
        a.recycle();
        float density = getResources().getDisplayMetrics().density;
        float minVel = MIN_FLING_VELOCITY * density;
        mDragHelper.setMinVelocity(minVel);
    }

    /**
     * Set up contentView which will be moved by user gesture
     *
     * @param view
     */
    public void setContentView(View view) {
        mContentView = view;
    }

    public void setSwipeEnable(boolean enable) {
        mSwipeEnable = enable;
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a
     * drawer is open.
     *
     * @param color Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    public void setSwipeListener(SwipeListener listener) {
        mSwipeListener = listener;
    }


    public interface SwipeListener {
        void onScrollStateChange(int state);
    }

    /**
     * Set a drawable used for edge shadow.
     *
     * @param shadow Drawable to use
     */
    public void setShadow(Drawable shadow) {
        mShadowLeft = shadow;
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        mDragHelper.shouldInterceptTouchEvent(event);
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
                super.dispatchTouchEvent(event);
                return true;
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
                    if (adx > ady) {
                        SlideOrientation slideOrientation;
                        if (moveX - mDownX > 0) {
                            //--->right
                            slideOrientation = SlideOrientation.SLIDE_RIGHT;
                            mInterceptTouchEvent = mSlidableDecider.slidable(mCurrentEvPos, slideOrientation);
                        } else {
                            //left<---
                            slideOrientation = SlideOrientation.SLIDE_LEFT;
                        }
                    }
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
        if (!mSwipeEnable) {
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mInLayout = true;
        if (mContentView != null)
            mContentView.layout(mContentLeft, mContentTop,
                    mContentLeft + mContentView.getMeasuredWidth(),
                    mContentTop + mContentView.getMeasuredHeight());
        mInLayout = false;

    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean drawContent = child == mContentView;
        try {
            //4.0.x硬件加速空指针
            drawShadow(canvas, child);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean ret = false;
        try {
            ret = super.drawChild(canvas, child, drawingTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (drawContent && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawScrim(canvas, child);
        }
        return ret;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void drawScrim(Canvas canvas, View child) {
        int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        int alpha = (int) (baseAlpha * (getViewHorizontalDragRange() - child.getLeft()) / getViewHorizontalDragRange());
        int color = alpha << 24 | (mScrimColor & 0xffffff);
        canvas.clipRect(0, 0, child.getLeft(), getHeight());
        canvas.drawColor(color);
    }

    private void drawShadow(Canvas canvas, View child) {
        Rect childRect = mTmpRect;
        child.getHitRect(childRect);
        mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                childRect.left, childRect.bottom);
        mShadowLeft.setAlpha(FULL_ALPHA);
        mShadowLeft.draw(canvas);
    }

    public int getViewHorizontalDragRange() {
        return getWidth() + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE;
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return SwipeBackLayout.this.getViewHorizontalDragRange();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mContentLeft = left;
            mContentTop = top;
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //预测以现在的滑动速度300毫秒后会落来x轴的坐标
            float expectFinalY = releasedChild.getLeft() + xvel * 0.3f;
            int left = expectFinalY > releasedChild.getWidth() / 3 ? SwipeBackLayout.this.getViewHorizontalDragRange() : 0;
            mDragHelper.settleCapturedViewAt(left, 0);
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.max(left, 0);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE) {
                mClosed = mDragHelper.getCapturedView().getLeft() != 0;
            }
            if (mSwipeListener != null) {
                mSwipeListener.onScrollStateChange(state);
            }
        }
    }

    /**
     * Scroll out contentView
     */
    public void scrollToFinish() {
        final int childWidth = mContentView.getWidth();
        int left = childWidth + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE;
        mDragHelper.smoothSlideViewTo(mContentView, left, 0);
        invalidate();
        if (mSwipeListener != null) {
            mSwipeListener.onScrollStateChange(STATE_DRAGGING);
        }
    }
}
