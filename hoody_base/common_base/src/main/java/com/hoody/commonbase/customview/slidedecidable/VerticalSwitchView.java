package com.hoody.commonbase.customview.slidedecidable;

import android.content.Context;
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import java.util.Arrays;

public class VerticalSwitchView extends SlideDecidableLayout {
    private static final String TAG = "VerticalSwitchView";
    private static final boolean DEBUG = true;
    private View mDragHelpView;
    private ViewDragHelper mDragHelper;
    private int mScrolledX;
    private int mScrolledY;
    private View[] mContentViews;

    private int mDrawerWidth = 0;
    private View mDrawerView;
    //计算手势的速度
    private VelocityTracker mTracker;


    public static final int STATE_IDLE = 0;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_SETTLING = 2;
    public static final int STATE_UP_IDLE = 3;//松手
    private int mCurrentPosition = 0;

    /**
     * 0 :未决
     * 1 :横向
     * -1 :竖向
     */
    private Orientation mOrientation = null;


    private enum Orientation {
        Horizental,
        Vertical;
    }

    public VerticalSwitchView(@NonNull Context context) {
        super(context);
        init();
    }

    public VerticalSwitchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalSwitchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        populate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerId(0) != mPointerId) {
            event.setAction(MotionEvent.ACTION_UP);
        }
        mDragHelper.processTouchEvent(event);
        if (mTracker != null) {
            mTracker.addMovement(event);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private PointF mCurrentEvPos1 = new PointF();
    private int mPointerId;
    private float mDownX;
    private float mDownY;
    private boolean mSlidable = true;
    /**
     * 用于判断从手指落下后有没有移动过
     */
    private boolean mFingerMoved = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (DEBUG) Log.d(TAG, "dispatchTouchEventcalled with: event = [" + event + "]");
        if (mDragHelper.continueSettling(true)) {
            return false;
        }
        boolean result = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOrientation = null;
                mPointerId = event.getPointerId(0);
                mSlidable = true;
                if (mSlidableDecider != null) {
                    mSlidableDecider.mDecided = false;
                }
                mDownX = event.getX();
                mDownY = event.getY();
                mCurrentEvPos1.x = event.getRawX();
                mCurrentEvPos1.y = event.getRawY();
                mFingerMoved = false;
                result = super.dispatchTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float adx = Math.abs(moveX - mDownX);
                float ady = Math.abs(moveY - mDownY);
                int slop = mDragHelper.getTouchSlop();
                if (DEBUG)
                    Log.d(TAG, "dispatchTouchEvent()mDownX==" + mDownX + " moveX==" + moveX + " with: ady = [" + ady + "]" + " adx =" + adx + " slop== " + slop);
                if (adx == 0 && ady == 0) {
                    return true;
                }
                mFingerMoved = true;
                if (mOrientation == null) {
                    mOrientation = adx > ady ? Orientation.Horizental : Orientation.Vertical;
                    if (isDrawerOpen() && mDownX < (getWidth() - mDrawerWidth)) {
                        //侧边栏打开的情况下，左边的事件认为是横向滑动
                        mOrientation = Orientation.Horizental;
                    }
                }
                if (mSlidableDecider != null && !mSlidableDecider.mDecided) {
                    //由外部来决定是不是可以滑动
                    boolean slideLeft = moveX < mDownX;
                    boolean slideDown = moveY > mDownY;
                    SlideOrientation slideOrientation = null;
                    if (mOrientation == Orientation.Horizental) {
                        slideOrientation = slideLeft ? SlideOrientation.SLIDE_LEFT : SlideOrientation.SLIDE_RIGHT;
                    } else if (mOrientation == Orientation.Vertical) {
                        slideOrientation = slideDown ? SlideOrientation.SLIDE_DOWN : SlideOrientation.SLIDE_UP;
                    }
                    mSlidable = mSlidableDecider.slidable(mCurrentEvPos1, slideOrientation);
                    mSlidableDecider.mDecided = true;
                }
                if (DEBUG)
                    Log.d(TAG, "dispatchTouchEvent() called with: mSlidable = [" + mSlidable + "]");
                if (mSlidable) {
                    result = onTouchEvent(event);
                } else {
                    mDragHelper.cancel();
                    result = super.dispatchTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mDragHelpView.getY() % getHeight() != 0 || (mDrawerView != null && mDrawerView.getX() < getWidth() && mDrawerView.getX() > getWidth() - mDrawerView.getWidth())) {
                    onTouchEvent(event);
                } else {
                    onTouchEvent(event);
                    result = super.dispatchTouchEvent(event);
                }
                break;
        }
//        super.dispatchTouchEvent(event);
        if (DEBUG) Log.d(TAG, "dispatchTouchEventresult = [" + result + "]");
        return result;
    }

    private void init() {
        mContentViews = new View[3];
        mDragHelpView = new View(getContext());
//        mDragHelpView.setBackgroundColor(0x55ff0000);
        addView(mDragHelpView);
        mDragHelpView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mFingerMoved) {
                    closeDrawer();
                }
            }
        });
        mDragHelpView.setClickable(false);
        mDragHelper = ViewDragHelper.create(this, mCallback);
        mTracker = VelocityTracker.obtain();
    }

    private Adapter mAdapter;
    private DrawerAdapter mDrawerAdapter;

    private final int ITEM_ID_KEY = 0xff00ff00;

    private boolean mDrawerOpen = false;

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {


        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragHelpView;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            mScrolledX = left;
            mScrolledY = top;
            if (DEBUG) {
                Log.d(TAG, "onViewPositionChanged() called with: changedView = [" + mOrientation + "], left = [" + left + "], top = [" + top + "], dx = [" + dx + "], dy = [" + dy + "]");
            }
            if (mScrollStateListener != null) {
                mScrollStateListener.onVerticalPositionChanged(top);
                mScrollStateListener.onHorizontalPositionChanged(left);
            }
            requestLayout();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (DEBUG) {
                Log.d(TAG, "onViewDragStateChanged() called with: state = [" + state + "]");
            }
            switch (state) {
                case ViewDragHelper.STATE_DRAGGING:
                    if (mScrollStateListener != null) {
                        mScrollStateListener.onVerticalScrollStateChanged(STATE_DRAGGING);
                        if (mDrawerView != null) {
                            mScrollStateListener.onHorizontalScrollStateChanged(STATE_DRAGGING);
                        }
                    }
                    break;
                case ViewDragHelper.STATE_IDLE:
                    int finalLeft = 0;
                    int finalTop = 0;
                    if (mOrientation != null) {
                        mTracker.computeCurrentVelocity(1);
                        switch (mOrientation) {
                            case Vertical:
                                float yVelocity = mTracker.getYVelocity();
                                //预测以现在的滑动速度300毫秒后会落来Y轴的坐标
                                float expectFinalY = mDragHelpView.getY() + yVelocity * 300;
                                if (Math.abs(expectFinalY) < getHeight() / 4) {
                                    finalTop = 0;
                                } else {
                                    if (expectFinalY > 0) {
                                        if (mContentViews[0] != null) {
                                            finalTop = getHeight();
                                        }
                                    } else {
                                        if (mContentViews[2] != null) {
                                            finalTop = -getHeight();
                                        }
                                    }
                                }
                                break;
                            case Horizental:
                                float xVelocity = mTracker.getXVelocity();
                                float expectFinalX = mDragHelpView.getX() + xVelocity * 300;
                                if (mDrawerView == null) {
                                    finalLeft = 0;
                                } else {
                                    if (expectFinalX > -mDrawerWidth / 2f) {
                                        finalLeft = 0;
                                    } else {
                                        finalLeft = -mDrawerWidth;
                                    }
                                }
                                break;
                        }
                    }
                    boolean continueSliding = mDragHelper.smoothSlideViewTo(mDragHelpView, finalLeft, finalTop);
                    setDrawerOpen(finalLeft < 0);
                    if (DEBUG) Log.d(TAG, "onViewDragStateChanged() " + continueSliding);
                    if (continueSliding) {
                        ViewCompat.postInvalidateOnAnimation(VerticalSwitchView.this);
                        if (mScrollStateListener != null) {
                            if (mOrientation == Orientation.Vertical) {
                                mScrollStateListener.onVerticalScrollStateChanged(STATE_UP_IDLE);
                            }
                            if (mOrientation == Orientation.Horizental) {
                                if (mDrawerView != null) {
                                    mScrollStateListener.onHorizontalScrollStateChanged(STATE_UP_IDLE);
                                }
                            }
                        }
                    } else {
                        if (mOrientation == Orientation.Horizental) {
                            mScrolledX = finalLeft;
                            requestLayout();
                            if (mDrawerStateChangedListener != null) {
                                if (mDrawerView != null) {
                                    setDrawerOpen(mDragHelpView.getLeft() < 0);
                                    mDrawerStateChangedListener.onDrawerStateChanged(mDrawerOpen);
                                }
                            }
                            if (mScrollStateListener != null) {
                                if (mDrawerView != null) {
                                    mScrollStateListener.onHorizontalScrollStateChanged(STATE_IDLE);
                                }
                            }
                        } else {
                            mScrolledY = 0;
                            View tem;
                            if (finalTop > 0) {
                                mCurrentPosition--;
                                tem = mContentViews[2];
                                if (mAdapter != null) {
                                    mAdapter.destroyItem(1 + mCurrentPosition, mContentViews[1]);
                                    mAdapter.destroyItem(2 + mCurrentPosition, tem);
                                    View itemView = mAdapter.instantiateItem(-1 + mCurrentPosition, tem);
                                    if (itemView != null) {
                                        itemView.setTag(ITEM_ID_KEY, mAdapter.getItemId(mCurrentPosition - 1));
                                    }
                                    if ((itemView != null && itemView.equals(tem)) || (itemView == null && tem == null)) {

                                    } else {
                                        int childIndex = indexOfChild(tem);
                                        if (itemView != null) {
                                            addView(itemView, Math.max(childIndex, 0));
                                        }
                                        if (tem != null) {
                                            removeView(tem);
                                        }
                                        tem = itemView;
                                    }
                                }
                                mContentViews[2] = mContentViews[1];
                                mContentViews[1] = mContentViews[0];
                                mContentViews[0] = tem;
                            } else if (finalTop < 0) {
                                mCurrentPosition++;
                                tem = mContentViews[0];
                                if (mAdapter != null) {
                                    mAdapter.destroyItem(-1 + mCurrentPosition, mContentViews[1]);
                                    mAdapter.destroyItem(-2 + mCurrentPosition, tem);
                                    View itemView = mAdapter.instantiateItem(1 + mCurrentPosition, tem);
                                    if (itemView != null) {
                                        itemView.setTag(ITEM_ID_KEY, mAdapter.getItemId(mCurrentPosition + 1));
                                    }
                                    if ((itemView != null && itemView.equals(tem)) || (itemView == null && tem == null)) {

                                    } else {
                                        int childIndex = indexOfChild(tem);
                                        if (itemView != null) {
                                            addView(itemView, Math.max(childIndex, 0));
                                        }
                                        if (tem != null) {
                                            removeView(tem);
                                        }
                                        tem = itemView;
                                    }
                                }
                                mContentViews[0] = mContentViews[1];
                                mContentViews[1] = mContentViews[2];
                                mContentViews[2] = tem;
                            } else {
                                if (mAdapter != null) {
                                    mAdapter.destroyItem(-1 + mCurrentPosition, mContentViews[0]);
                                    mAdapter.destroyItem(1 + mCurrentPosition, mContentViews[2]);
                                }
                            }
                            requestLayout();
                            if (mScrollStateListener != null) {
                                if (mOrientation == Orientation.Vertical) {
                                    mScrollStateListener.onVerticalScrollStateChanged(STATE_IDLE);
                                }
                            }
                        }
                    }
                    break;
                case ViewDragHelper.STATE_SETTLING:
                    if (mTracker != null) {
                        mTracker.clear();
                    }
                    if (mScrollStateListener != null) {
                        if (mOrientation == Orientation.Vertical) {
                            mScrollStateListener.onVerticalScrollStateChanged(STATE_SETTLING);
                        }
                        if (mOrientation == Orientation.Horizental) {
                            mScrollStateListener.onHorizontalScrollStateChanged(STATE_SETTLING);
                        }
                    }
            }

        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (mOrientation != Orientation.Vertical) {
                return 0;
            }
            if (dy > 0) {
                if (mContentViews[0] == null) {
                    if (top > 0) {
                        return 0;
                    }
                }
            } else if (dy < 0) {
                if (mContentViews[2] == null) {
                    if (top < 0) {
                        return 0;
                    }
                }
            }
            return top;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (mOrientation != Orientation.Horizental) {
                return 0;
            }
            if (mDrawerView != null) {
                if (left > 0) {
                    return 0;
                }
                if (left + mDrawerWidth < 0) {
                    return -mDrawerWidth;
                }
                return left;
            }
            return 0;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            if (mDrawerView == null) {
                return 0;
            }
            return mDrawerWidth;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return getMeasuredHeight();
        }
    };

    public void setAdapter(Adapter adapter) {
        Arrays.fill(mContentViews, null);
        adapter.mHost = this;
        this.mAdapter = adapter;
        requestLayout();
    }

    public void setDrawerAdapter(DrawerAdapter adapter) {
        adapter.mHost = this;
        this.mDrawerAdapter = adapter;
        if (isAttachedToWindow()) {
            notifyDrawerChanged();
        }
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < mContentViews.length; i++) {
            View child = mContentViews[i];
            if (child == null) {
                continue;
            }
            child.layout(0, mScrolledY + getHeight() * (i - 1),
                    getWidth(),
                    mScrolledY + getHeight() * i);
        }
        if (mDrawerView != null) {
            mDrawerView.bringToFront();
            mDrawerView.getLayoutParams().width = mDrawerWidth;
            mDrawerView.layout(mScrolledX + getWidth(), 0, mScrolledX + mDrawerWidth + getWidth(), getHeight());
        }
        mDragHelpView.bringToFront();
        mDragHelpView.layout(mScrolledX, mScrolledY, mScrolledX + getWidth(), mScrolledY + getHeight());
    }


    private void populate() {
        for (View mContentView : mContentViews) {
            if (mContentView != null) {
                return;
            }
        }
        if (mAdapter != null) {
            int contentViewCount = 3;
            for (int i = -1; i < contentViewCount - 1; i++) {
                View itemView = mAdapter.instantiateItem(i + mCurrentPosition, null);
                if (itemView != null) {
                    itemView.setTag(ITEM_ID_KEY, mAdapter.getItemId(mCurrentPosition + i));
                }
                mContentViews[i + 1] = itemView;
                if (itemView != null) {
                    addView(itemView);
                }
            }
        }
        if (mDrawerAdapter != null && mDrawerView == null) {
            if (mDrawerWidth == 0) {
                mDrawerWidth = 200;
            }
            mDrawerView = mDrawerAdapter.instantiate(null);
            if (mDrawerView != null) {
                mDrawerView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    //设置个空实现，防止事件穿透
                    }
                });
                addView(mDrawerView);
            }
        }
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public boolean isDrawerOpen() {
        return mDrawerOpen;
    }

    private void setDrawerOpen(boolean drawerOpen) {
        this.mDrawerOpen = drawerOpen;
        mDragHelpView.setClickable(drawerOpen);
    }

    /**
     * 打开侧边栏
     */
    public void openDrawer() {
        if (DEBUG) Log.d(TAG, "openDrawer() called" + mDrawerView);
        if (mDrawerView == null) {
            return;
        }
        setDrawerOpen(true);
        mOrientation = Orientation.Horizental;
        if (mDragHelper.smoothSlideViewTo(mDragHelpView, -mDrawerWidth, 0)) {
            ViewCompat.postInvalidateOnAnimation(VerticalSwitchView.this);
        }
    }

    /**
     * 关闭侧边栏
     *
     * @param anim 是否使用动画
     */
    public void closeDrawer(boolean anim) {
        if (DEBUG) Log.d(TAG, "closeDrawer() called = " + anim);
        if (mDrawerView == null) {
            return;
        }
        if (isDrawerOpen()) {
            if (anim) {
                closeDrawer();
            } else {
                mDragHelpView.setLeft(0);
                mDragHelpView.setTop(0);
                mDrawerView.setX(getWidth());
                setDrawerOpen(false);
                if (mScrollStateListener != null) {
                    mScrollStateListener.onHorizontalPositionChanged(0);
                }
                if (mDrawerStateChangedListener != null) {
                    mDrawerStateChangedListener.onDrawerStateChanged(mDrawerOpen);
                }
            }
        }

    }

    /**
     * 关闭侧边栏
     */
    private void closeDrawer() {
        if (DEBUG) Log.d(TAG, "closeDrawer() called");
        mOrientation = Orientation.Horizental;
        setDrawerOpen(false);
        if (mDragHelper.smoothSlideViewTo(mDragHelpView, 0, 0)) {
            ViewCompat.postInvalidateOnAnimation(VerticalSwitchView.this);
        }
    }

    /**
     * 指定位置的数据改变
     */
    public void notifyItemChanged(int position) {
        if (mAdapter != null) {
            int relativePosition = position - mCurrentPosition;
            if (relativePosition != 0) {
                relativePosition = relativePosition / Math.abs(relativePosition);
            }
            View temView = mContentViews[relativePosition + 1];
            String itemId = mAdapter.getItemId(position);
            if (temView != null) {
                if (!TextUtils.equals((CharSequence) temView.getTag(ITEM_ID_KEY), itemId)) {
                    mAdapter.destroyItem(position, temView);
                }
            }
            View itemView = mAdapter.instantiateItem(position, temView);
            if (itemView != null) {
                itemView.setTag(ITEM_ID_KEY, itemId);
            }
            if ((itemView != null && itemView.equals(temView)) || (itemView == null && temView == null)) {

            } else {
                int childIndex = 0;
                if (temView != null) {
                    childIndex = indexOfChild(temView);
                }
                mContentViews[relativePosition + 1] = itemView;
                if (itemView != null) {
                    addView(itemView, Math.max(childIndex, 0));
                }
                if (temView != null) {
                    removeView(temView);
                }
            }
        }
    }

    public void notifyDrawerChanged() {
        if (mDrawerAdapter != null) {
            if (mDrawerView != null) {
                mDrawerAdapter.destroy(mDrawerView);
            }
            if (mDrawerWidth == 0) {
                mDrawerWidth = 200;
            }
            mDrawerView = mDrawerAdapter.instantiate(mDrawerView);
            if (mDrawerView != null) {
                mDrawerView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    //设置个空实现，防止事件穿透
                    }
                });
            }
        }
    }

    public static abstract class Adapter {
        public VerticalSwitchView mHost;

        public abstract View instantiateItem(int position, View convertView);

        public abstract void destroyItem(int position, View convertView);

        public abstract String getItemId(int position);
    }

    public static abstract class DrawerAdapter {
        public VerticalSwitchView mHost;

        public abstract View instantiate(View convertView);

        public abstract void destroy(View convertView);

    }

    public void setDrawerViewWidth(int drawerWidth) {
        if (drawerWidth < 0) {
            drawerWidth = 200;
        }
        this.mDrawerWidth = drawerWidth;
    }

    public void setScrollStateListener(ScrollStateListener scrollStateListener) {
        this.mScrollStateListener = scrollStateListener;
    }

    public void setDrawerStateChangedListener(DrawerStateChangedListener drawerStateChangedListener) {
        this.mDrawerStateChangedListener = drawerStateChangedListener;
    }

    private ScrollStateListener mScrollStateListener;
    private DrawerStateChangedListener mDrawerStateChangedListener;

    public static abstract class DrawerStateChangedListener {
        protected abstract void onDrawerStateChanged(boolean open);
    }

    public static abstract class ScrollStateListener {
        protected abstract void onVerticalScrollStateChanged(int state);

        protected abstract void onVerticalPositionChanged(int position);

        protected abstract void onHorizontalScrollStateChanged(int state);

        protected abstract void onHorizontalPositionChanged(int position);
    }
}
