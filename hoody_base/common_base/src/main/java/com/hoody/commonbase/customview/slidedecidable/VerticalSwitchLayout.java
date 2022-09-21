package com.hoody.commonbase.customview.slidedecidable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hoody.commonbase.R;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;


/**
 * 直播间上下滑动切换控件
 * 使用时配合{@link Adapter},和{@link SlidableDecider}
 * {@link Adapter} 是相关viewitem 的获取类
 * {@link SlidableDecider} 是控件能否滑动的获取类
 * 这个控件采用懒加载，只有在滑动的时候，才去获取预览视图，
 * Created by cdm on 2019/11/11.
 */
@Deprecated
public class VerticalSwitchLayout extends SlideDecidableLayout {
    private static final boolean DEBUG = false;
    private static final String TAG = VerticalSwitchLayout.class.getSimpleName();

    public static final int STATE_IDLE = 0;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_SETTLING = 2;
    public static final int STATE_UP_IDLE = 3;//松手
    private ViewDragHelper mDragHelper;
    private Adapter mAdapter;

    private View TARGET_VIEW;

    private FrameLayout mDragHelpView;

    private FrameLayout mDrawerView;
    private View mContentView;
    private FrameLayout mPreContentView;
    private int mCurPos = 0;
    private int mCurPrePos = 0;
    private float mDownX;
    private float mDownY;
    /**
     * 侧边栏宽度
     */
    private int mDrawerWidth = 500;

    private SlideListener mSlideListener;


    private final int SLIDE_NULL = 0;
    //滑动方向
    private int mSlideOritation = SLIDE_NULL;
    //侧滑菜单完全打开时菜单左侧透明度百分数
    private int mDrawerMaxAlphaPersent = 50;
    //侧滑返回时整控件透明度百分数
    private int mBaseMaxAlphaPersent = 50;
    private PointF mCurrentEvPos1 = new PointF();
    private boolean mSlidable = true;
    /**
     * 侧边栏是否可用
     */
    private boolean mDrawerEnable = false;
    /**
     * 是否予许上下滑动，用于将视图锁死在指定页面
     */
    private boolean mVerticalSwitchEnable = true;
    /**
     * 是否不予许侧滑关闭
     */
    private boolean mSlideCloseEnable = true;
    private int mPointerId;
    //左侧阴影
    private Drawable mLeftShadowDrawable;
    private Rect mTmpContentViewRect = new Rect();
    //计算手势的速度
    private VelocityTracker mTracker;

    public VerticalSwitchLayout(Context context) {
        super(context);
        init();
    }

    public VerticalSwitchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalSwitchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLeftShadowDrawable = getContext().getResources().getDrawable(R.mipmap.shadow_left);
        mDragHelper = ViewDragHelper.create(this, callback);
        //预设侧边栏
        mDrawerView = new FrameLayout(getContext());
        LayoutParams layoutParamsDrawerView = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mDrawerView, layoutParamsDrawerView);
        //预设预览view
        mPreContentView = new FrameLayout(getContext());
        mPreContentView.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mPreContentView, layoutParams);
        mTracker = VelocityTracker.obtain();
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragHelpView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (TARGET_VIEW != null) {
                if (TARGET_VIEW == mContentView) {
                    if (DEBUG) Log.d(TAG, "onViewPositionChanged() called with: =1");
                    if (mSlideCloseEnable) {
                        moveContentViewHorizental(left);
                        movePreContentViewHorizental(left);
                    }
                } else if (TARGET_VIEW == mPreContentView) {
                    if (DEBUG) Log.d(TAG, "onViewPositionChanged() called with: =2");
                    if (mVerticalSwitchEnable) {
                        if (movePreContentViewVertical(top)) {
                            moveContentViewVertical(top);
                        } else {
                            moveContentViewVertical(0);
                        }
                    }
                } else if (TARGET_VIEW == mDrawerView) {
                    if (DEBUG) Log.d(TAG, "onViewPositionChanged() called with: =3");
                    moveDrawerView(left);
                }
            }
        }

        private void moveContentViewHorizental(int left) {
            if (DEBUG)
                Log.d(TAG, "moveContentViewHorizental() called with: left = [" + left + "]");
            if (mContentView != null) {
                mContentView.setX(left);
                //设置整个背景的透明度
                int alph = 0xff - 0xff * Math.abs(left) * 100 / getWidth() / 100;
                int argb = Color.argb(alph * mBaseMaxAlphaPersent / 100, 0x00, 0x00, 0x00);
                setBackgroundColor(argb);
                if (getWidth() == left && mSlideListener != null) {
                    mSlideListener.onSlideCloseSwitchLayout();
                }
                invalidate();
            }
        }

        private void movePreContentViewHorizental(int left) {
            if (mPreContentView != null) {
                mPreContentView.setX(left);
            }
        }

        private void moveDrawerView(int left) {
            if (DEBUG)
                Log.d(TAG, "moveDrawerView() called with: left = [" + left + "]" + mDrawerView);
            mDrawerView.setX(left + getWidth());
//            if (mDrawerView.getWidth() > 0) {
//                int alph = 0xff * Math.abs(left) * 100 / mDrawerView.getWidth() / 100;
//                int argb = Color.argb(alph * mDrawerMaxAlphaPersent / 100, 0x00, 0x00, 0x00);
//                mDragHelpView.setBackgroundColor(argb);
//            }
            if (mSlideListener != null) {
                if (DEBUG) Log.d(TAG, "call onSlideDrawer()");
                mSlideListener.onSlideDrawer(left);
                if (Math.abs(left) == mDrawerView.getWidth()) {
                    if (DEBUG) Log.d(TAG, "call onOpenDrawer()");
                    if (!mSlideListener.mDrawerOpen) {
                        mSlideListener.onOpenDrawer();
                        mSlideListener.mDrawerOpen = true;
                    }
                }
                if (left == 0) {
                    if (DEBUG) Log.d(TAG, "call onCloseDrawer()");
                    if (mSlideListener.mDrawerOpen) {
                        mSlideListener.onCloseDrawer();
                        mSlideListener.mDrawerOpen = false;
                    }
                }
            }
        }

        private void moveContentViewVertical(int top) {
            if (DEBUG) Log.d(TAG, "moveContentViewVertical() called with: top = [" + top + "]");
            if (mContentView != null) {
                mContentView.setY(top);
            }
        }

        /**
         * 调整接下来要展示的条目的预览
         * @param top
         * @return 是否有效的移动了mPreContentView true:移动了 false 没有移动
         */
        private boolean movePreContentViewVertical(int top) {
            if (DEBUG)
                Log.d(TAG, "movePreContentViewVertical() called with: top = [" + top + "]" + mCurPrePos + " mCurPos=" + mCurPos);
            if (top >= 0 && mCurPrePos != mCurPos - 1) {
                mCurPrePos = mCurPos - 1;
                View preContentView = null;
                if (mPreContentView.getChildCount() > 0) {
                    preContentView = mPreContentView.getChildAt(0);
                }
                preContentView = mAdapter.getPreContentView(preContentView, mCurPrePos);
                if (preContentView != null && preContentView.getParent() == null) {
                    ViewGroup.LayoutParams layoutParams = preContentView.getLayoutParams();
                    if (layoutParams == null) {
                        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    }
                    mPreContentView.addView(preContentView, 0, layoutParams);
                }
            } else if (top <= 0 && mCurPrePos != mCurPos + 1) {
                mCurPrePos = mCurPos + 1;
                View preContentView = null;
                if (mPreContentView.getChildCount() > 0) {
                    preContentView = mPreContentView.getChildAt(0);
                }
                preContentView = mAdapter.getPreContentView(preContentView, mCurPrePos);
                if (preContentView != null && preContentView.getParent() == null) {
                    ViewGroup.LayoutParams layoutParams = preContentView.getLayoutParams();
                    if (layoutParams == null) {
                        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    }
                    mPreContentView.addView(preContentView, 0, layoutParams);
                }
            }
            if (mPreContentView.getChildCount() == 0) {
                mPreContentView.setY(0);
                //没有内容不让滑动了
                return false;
            }
            if (mPreContentView != null) {
                if (top > 0) {
                    mPreContentView.setY(-getHeight() + top);
                } else {
                    mPreContentView.setY(getHeight() + top);
                }
            }
            return true;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (DEBUG)
                Log.d(TAG, "onViewDragStateChanged() called with: state = [" + state + "]");
            switch (state) {
                case ViewDragHelper.STATE_DRAGGING:
                    break;
                case ViewDragHelper.STATE_IDLE:
                    int finalLeft = 0;
                    int finalTop = 0;
                    mTracker.computeCurrentVelocity(1);
                    float xVelocity = mTracker.getXVelocity();
                    float yVelocity = mTracker.getYVelocity();
                    if (TARGET_VIEW == mContentView) {
                        finalTop = 0;
                        //预测以现在的滑动速度200毫秒后会落来X轴的坐标
                        float expectFinalX = mDragHelpView.getX() + xVelocity * 200;
                        if (expectFinalX < getWidth() / 2) {
                            finalLeft = 0;
                        } else {
                            finalLeft = getWidth();
                        }
                    } else if (TARGET_VIEW == mPreContentView) {
                        finalLeft = 0;
                        //预测以现在的滑动速度300毫秒后会落来Y轴的坐标
                        float expectFinalY = mDragHelpView.getY() + yVelocity * 300;
                        if (Math.abs(expectFinalY) < getHeight() / 4) {
                            finalTop = 0;
                        } else {
                            if (mPreContentView.getChildCount() == 0) {
                                finalTop = 0;
                            } else if (expectFinalY > 0) {
                                finalTop = getHeight();
                            } else {
                                finalTop = -getHeight();
                            }
                        }
                    } else if (TARGET_VIEW == mDrawerView) {
                        if (DEBUG)
                            Log.d(TAG, "onViewDragStateChanged() called with: mDrawerView.getLeft() = [" + mDrawerView.getX() + "]");
                        if (mDrawerEnable) {
                            //预测以现在的滑动速度200毫秒后会落来X轴的坐标
                            float expectFinalX = mDrawerView.getX() + xVelocity * 200;
                            if (expectFinalX < getWidth() - mDrawerView.getWidth() / 2) {
                                finalLeft = -mDrawerView.getWidth();
                            } else {
                                finalLeft = 0;
                            }
                        }
                        if (DEBUG)
                            Log.d(TAG, "onViewDragStateChanged() called with: finalLeft = [" + finalLeft + "]");
                    }
                    boolean continueSliding = mDragHelper.smoothSlideViewTo(mDragHelpView, finalLeft, finalTop);
                    if (DEBUG) Log.d(TAG, "onViewDragStateChanged() called--" + continueSliding);
                    if (continueSliding) {
                        ViewCompat.postInvalidateOnAnimation(VerticalSwitchLayout.this);
                    } else {
                        if (TARGET_VIEW == mPreContentView) {
                            if (mDragHelpView.getY() == 0) {
                                return;
                            } else if (mDragHelpView.getY() > 0) {
                                mCurPos--;
                            } else if (mDragHelpView.getY() < 0) {
                                mCurPos++;
                            }
                            if (DEBUG)
                                Log.d(TAG, "onViewDragStateChanged() called with: mCurPos = [" + mCurPos + "]");
                            removeView(mContentView);
                            if (mAdapter != null) {
                                mContentView = mAdapter.getContentView(mContentView, mCurPos);
                                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                mContentView.setX(0);
                                mContentView.setY(0);
                                addView(mContentView, layoutParams);
                                View drawerView = null;
                                if (mDrawerView.getChildCount() > 0) {
                                    drawerView = mDrawerView.getChildAt(0);
                                    mDrawerView.removeAllViews();
                                }
                                drawerView = mAdapter.getDrawerView(drawerView, mCurPos);
                                if (drawerView != null) {
                                    LayoutParams drawerViewlayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                    mDrawerView.addView(drawerView, drawerViewlayoutParams);
                                } else {
                                    setDrawerEnable(false);
                                }
                                LayoutParams drawerBaselayoutParams = new LayoutParams(mDrawerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                                mDrawerView.setLayoutParams(drawerBaselayoutParams);
                                mDrawerView.setX(getWidth());
                                bringChildToFront(mDrawerView);
                            }
                            if (TARGET_VIEW == mPreContentView) {
                                if (mScrollStateListener != null) {
                                    mScrollStateListener.onPageSelected(mCurPos);
                                }
                            }
                            mDragHelpView.setLeft(0);
                            mDragHelpView.setTop(0);
                            bringChildToFront(mDragHelpView);
                        }
                    }
                    break;
                case ViewDragHelper.STATE_SETTLING:
                    if (mTracker != null) {
                        mTracker.clear();
                    }
                    break;
            }
            if (TARGET_VIEW == mPreContentView) {
                if (mScrollStateListener != null) {
                    if (state == STATE_IDLE && isScrolling()) {
                        mScrollStateListener.onScrollStateChanged(STATE_UP_IDLE);
                    } else {
                        mScrollStateListener.onScrollStateChanged(state);
                    }
                }
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (!mVerticalSwitchEnable) {
                return 0;
            }
            if (TARGET_VIEW == mContentView) {
                return 0;
            } else if (TARGET_VIEW == mPreContentView) {
                return top;
            } else if (TARGET_VIEW == mDrawerView) {
                return 0;
            }
            return top;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (TARGET_VIEW == mContentView) {
                if (!mSlideCloseEnable) {
                    return 0;
                }
                return left < 0 ? 0 : left;
            } else if (TARGET_VIEW == mPreContentView) {
                return 0;
            } else if (TARGET_VIEW == mDrawerView) {
                if (DEBUG)
                    Log.d(TAG, "clampViewPositionHorizontal() called with: child = [" + child + "], left = [" + left + "], dx = [" + dx + "]");
                if (!mDrawerEnable) {
                    return 0;
                }
                if (left < -mDrawerView.getWidth()) {
                    return -mDrawerView.getWidth();
                }
                if (left > 0) {
                    return 0;
                }
                return left;
            }
            return left;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            if (DEBUG)
                Log.d(TAG, "getViewVerticalDragRange() called with: mSlideOritation = [" + mSlideOritation + "]");
            return getMeasuredHeight();
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            if (DEBUG)
                Log.d(TAG, "getViewHorizontalDragRange() called with: mSlideOritation = [" + mSlideOritation + "]");
            return getMeasuredWidth();
        }
    };


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (DEBUG) Log.d(TAG, "onInterceptTouchEvent() called with: event = [" + event + "]");
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (DEBUG) Log.d(TAG, "dispatchTouchEvent() called with: event = [" + event + "]");
        if (mDragHelper.continueSettling(true)) {
            return false;
        }
        boolean result = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointerId = event.getPointerId(0);
                TARGET_VIEW = null;
                mSlidable = true;
                if (mSlidableDecider != null) {
                    mSlidableDecider.mDecided = false;
                }
                mDownX = event.getX();
                mDownY = event.getY();
                mCurrentEvPos1.x = event.getRawX();
                mCurrentEvPos1.y = event.getRawY();
                result = super.dispatchTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                if (mDrawerView.getX() == getWidth() - mDrawerView.getWidth() && mDownX > getWidth() - mDrawerView.getWidth()) {
                    //侧边栏打开时，触摸到侧边栏的时候直接不做处理
//                    mDragHelper.cancel();
//                    return super.dispatchTouchEvent(event);
                }
                float adx = Math.abs(moveX - mDownX);
                float ady = Math.abs(moveY - mDownY);
                int slop = mDragHelper.getTouchSlop();
                if (DEBUG)
                    Log.d(TAG, "dispatchTouchEvent()mDownX==" + mDownX + " moveX==" + moveX + " with: ady = [" + ady + "]" + " adx =" + adx + " slop== " + slop);
                if (adx == 0 && ady == 0) {
                    return true;
                }
                if (TARGET_VIEW == null && mSlidable) {
                    //决定滑动哪个view
                    if (adx > ady) {
                        if (moveX - mDownX > 0) {
                            if (DEBUG)
                                Log.d(TAG, "dispatchTouchEvent() called with: mDrawerView.getLeft() = [" + mDrawerView.getX() + "]");
                            if (mDrawerView.getX() < getWidth()) {
                                TARGET_VIEW = mDrawerView;
                            } else {
                                TARGET_VIEW = mContentView;
                            }
                        } else {
                            TARGET_VIEW = mDrawerView;
                        }
                    } else {
                        if (mDrawerView.getX() < getWidth()) {
                            TARGET_VIEW = mDrawerView;
                        } else {
                            TARGET_VIEW = mPreContentView;
                        }
                    }
                }
                if (mSlidableDecider != null && !mSlidableDecider.mDecided) {
                    //由外部来决定是不是可以滑动
                    if (DEBUG)
                        Log.d(TAG, "dispatchTouchEvent() called with: TARGET_VIEWTARGET_VIEW = [" + TARGET_VIEW + "]");
                    boolean isHorizentalSlide = adx > ady;
                    boolean slideLeft = moveX < mDownX;
                    boolean slideDown = moveY > mDownY;
                    SlideOrientation slideOrientation;
                    if (isHorizentalSlide) {
                        slideOrientation = slideLeft ? SlideOrientation.SLIDE_LEFT : SlideOrientation.SLIDE_RIGHT;
                    } else {
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
                    if (TARGET_VIEW != null) {
                        mDragHelper.cancel();
                    }
                    TARGET_VIEW = null;
                    super.dispatchTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (DEBUG)
                    Log.d(TAG, "dispatchTouchEvent() called with: event TARGET_VIEW= [" + TARGET_VIEW + "]");
                if (TARGET_VIEW != null || (mDrawerView.getX() < getWidth() && mDrawerView.getX() > getWidth() - mDrawerView.getWidth())) {
                    super.dispatchTouchEvent(event);
                    result = onTouchEvent(event);
                } else {
                    if (mDrawerView.getWidth() > 0 && mDrawerView.getX() == getWidth() - mDrawerView.getWidth() && mDownX < getWidth() - mDrawerView.getWidth()) {
                        closeDrawer();
                        return onTouchEvent(event);
                    }
                    result = super.dispatchTouchEvent(event);
                }
                break;
        }
//        super.dispatchTouchEvent(event);
        if (DEBUG) Log.d(TAG, "dispatchTouchEvent() called with: eveee = [" + result + "]");
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerId(0) != mPointerId) {
            event.setAction(MotionEvent.ACTION_UP);
        }
        if (mTracker != null) {
            mTracker.addMovement(event);
        }
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mDragHelpView == null) {
            return;
        }
        float temTop = mDragHelpView.getY();
        float temLeft = mDragHelpView.getX();
        super.onLayout(changed, left, top, right, bottom);
        if (DEBUG)
            Log.d(TAG, "onLayout() called with: changed = [" + changed + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");
        if (mDrawerView.getWidth() > 0 && mDrawerView.getX() == getWidth() - mDrawerView.getWidth()) {
            mDragHelpView.setRight(getWidth() * 2 - mDrawerView.getWidth());
            mDragHelpView.setLeft(-mDrawerView.getWidth());
        } else {
            mDragHelpView.setRight((int) (getWidth() * 2 + temLeft));
            mDragHelpView.setLeft((int) temLeft);
            mDragHelpView.setTop((int) temTop);
            mDragHelpView.setBottom((int) (getHeight() + temTop));
            if (changed) {
                mDrawerView.setX(getWidth());
            }
        }
        if (DEBUG)
            Log.d(TAG, "onLayout() called with: getMeasuredWidth()== " + getMeasuredWidth());
        if (mDrawerWidth > getMeasuredWidth() * 4 / 5) {
            mDrawerWidth = getMeasuredWidth() * 4 / 5;
            mDrawerView.getLayoutParams().width = mDrawerWidth;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (DEBUG) Log.d(TAG, "onDetachedFromWindow() called");
        super.onDetachedFromWindow();
        removeAllViews();
//        mLeftShadowDrawable = null;
//        mAdapter = null;
//        mContentView = null;
//        mDrawerView = null;
//        mPreContentView = null;
//        mSlidableDecider = null;
//        TARGET_VIEW = null;
//        try {
//            if (mTracker != null) {
//                mTracker.recycle();
//            }
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }

    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child.equals(mContentView)) {
            child.getHitRect(mTmpContentViewRect);
            if (mTmpContentViewRect.left != 0) {
                mLeftShadowDrawable.setBounds(mTmpContentViewRect.left - mLeftShadowDrawable.getIntrinsicWidth(), mTmpContentViewRect.top,
                        mTmpContentViewRect.left, mTmpContentViewRect.bottom);
                mLeftShadowDrawable.draw(canvas);
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * 侧滑菜单完全打开时菜单左侧透明度
     *
     * @param persent 百分数
     */
    private void setDrawerMaxAlphaPersent(int persent) {
        this.mDrawerMaxAlphaPersent = persent;
    }

    /**
     * 侧滑返回时整控件透明度百分数
     *
     * @param persent 百分数
     */
    private void setBathMaxAlphaPersent(int persent) {
        this.mBaseMaxAlphaPersent = persent;
    }


    /**
     * 替换内容呈现view
     *
     * @param contentView
     */
    public void replaceContentView(View contentView) {
        if (mContentView != null) {
            removeView(mContentView);
        }
        mContentView = contentView;
        mContentView.setX(0);
        mContentView.setY(0);
        LayoutParams layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mContentView, 1, layoutParams1);
        bringChildToFront(mContentView);
        bringChildToFront(mDrawerView);
        bringChildToFront(mDragHelpView);
    }

    /**
     * 设置视图适配器
     *
     * @param adapter 视图适配器
     */
    public void setAdapter(Adapter adapter) {
        adapter.mHostView = this;
        mCurPos = 0;
        mCurPrePos = 0;
        if (mSlideListener != null) {
            mSlideListener.mDrawerOpen = false;
        }
        removeAllViews();
        this.mAdapter = adapter;
        if (mAdapter != null) {
            //拖拽辅助view
            {
                if (mDragHelpView == null) {
                    mDragHelpView = new FrameLayout(getContext());
//                mDragHelpView.setBackgroundColor(0x99121212);
                }
                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addView(mDragHelpView, layoutParams);
            }
            // 预览视图
            {
                View preContentView = null;
                if (mPreContentView.getChildCount() > 0) {
                    preContentView = mPreContentView.getChildAt(0);
                    mPreContentView.removeAllViews();
                }
                preContentView = mAdapter.getPreContentView(preContentView, 0);
                if (preContentView != null) {
                    LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mPreContentView.addView(preContentView, 0, layoutParams);
                }
                LayoutParams preViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addView(mPreContentView, preViewLayoutParams);
                //内容视图
                mContentView = mAdapter.getContentView(mContentView, 0);
                if (mContentView != null) {
                    LayoutParams layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    addView(mContentView, layoutParams1);
                }
            }
            //侧边栏视图
            {
                View drawerView = null;
                if (mDrawerView.getChildCount() > 0) {
                    drawerView = mDrawerView.getChildAt(0);
                    mDrawerView.removeAllViews();
                }
                drawerView = mAdapter.getDrawerView(drawerView, 0);
                if (drawerView != null) {
                    LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mDrawerView.addView(drawerView, 0, layoutParams);
                } else {
                    setDrawerEnable(false);
                }
                LayoutParams layoutParams = new LayoutParams(mDrawerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                mDrawerView.setX(10000);
                addView(mDrawerView, layoutParams);
                bringChildToFront(mDrawerView);
            }
            if (mDragHelpView != null) {
                bringChildToFront(mDragHelpView);
            }
        }
    }

    /**
     * @return 当前页面序号
     */
    public int getCurPos() {
        return mCurPos;
    }

    /**
     * 打开侧边栏
     */
    public void openDrawer() {
        if (DEBUG) Log.d(TAG, "openDrawer() called" + mDrawerView);
        if (mDrawerEnable) {
            if (DEBUG) Log.d(TAG, "openDrawer() called" + mDrawerView.getWidth());
            TARGET_VIEW = mDrawerView;
            if (mDragHelper.smoothSlideViewTo(mDragHelpView, -mDrawerView.getWidth(), 0)) {
                ViewCompat.postInvalidateOnAnimation(VerticalSwitchLayout.this);
            }
        }
    }

    /**
     * 关闭侧边栏
     */
    private void closeDrawer() {
        if (DEBUG) Log.d(TAG, "closeDrawer() called");
        TARGET_VIEW = mDrawerView;
        if (mDragHelper.smoothSlideViewTo(mDragHelpView, 0, 0)) {
            ViewCompat.postInvalidateOnAnimation(VerticalSwitchLayout.this);
        }
    }

    /**
     * 关闭侧边栏
     *
     * @param anim 是否使用动画
     */
    public void closeDrawer(boolean anim) {
        if (DEBUG) Log.d(TAG, "closeDrawer() called = " + anim);
        if (isDrawerOpened()) {
            if (anim) {
                closeDrawer();
            } else {
                mDragHelpView.setLeft(0);
                mDragHelpView.setTop(0);
                mDrawerView.setX(getWidth());
                if (mSlideListener != null && mSlideListener.mDrawerOpen) {
                    mSlideListener.onSlideDrawer(0);
                    mSlideListener.onCloseDrawer();
                    mSlideListener.mDrawerOpen = false;
                }
            }
        }

    }

    /**
     * 注意！！！在滑动的过程中去获取这个状态是不准确的
     * 滑动超过总距离的一半时返回true
     *
     * @return 侧边栏是否打开
     */
    public boolean isDrawerOpened() {
        if (DEBUG)
            Log.d(TAG, "isDrawerOpend() called  ===" + mDrawerView.getX() + ",,mDrawerView.getWidth()=" + mDrawerView.getWidth() + ",,getWidth()=" + getWidth());
        return mDrawerView != null && (getWidth() - mDrawerView.getX() >= mDrawerView.getWidth() / 2);
    }

    /**
     * 设置侧边栏是否可用
     *
     * @param enable 侧边栏是否可用
     */
    public void setDrawerEnable(boolean enable) {
        this.mDrawerEnable = enable;
    }

    /**
     * @return 侧边栏是否可用
     */
    public boolean isDrawerEnable() {
        return mDrawerEnable;
    }

    /**
     * 设置垂直方向滑动切换是否可用
     *
     * @param enable
     */
    public void setVerticalSwitchEnable(boolean enable) {
        mVerticalSwitchEnable = enable;
    }

    /**
     * @return 垂直方向滑动切换是否可用
     */
    public boolean isVerticalSwitchEnable() {
        return mVerticalSwitchEnable;
    }

    /**
     * 设置侧滑关闭是否可用
     *
     * @param enable
     */
    public void setSlideCloseEnable(boolean enable) {
        mSlideCloseEnable = enable;
    }

    /**
     * @return enable 侧滑关闭是否可用
     */
    public boolean isSlideCloseEnable() {
        return mSlideCloseEnable;
    }

    /**
     * 设置滑动监听
     */
    public void setSlideListener(SlideListener slideListener) {
        this.mSlideListener = slideListener;
    }

    /**
     * 设置侧边栏宽度
     * 这个值不能过大，目前限制最大为当前控件的4/5
     */
    public void setDrawerWidth(int drawerWidth) {
        if (getWidth() > 0 && drawerWidth > getWidth() * 4 / 5) {
            drawerWidth = getWidth() * 4 / 5;
        }
        this.mDrawerWidth = drawerWidth;
        ViewGroup.LayoutParams layoutParams = mDrawerView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = drawerWidth;
            mDrawerView.setLayoutParams(layoutParams);
        }
    }

    /**
     * @return 是否正处于滑动状态，包括手指的拖动和松手后的
     */
    public boolean isScrolling() {
        int viewDragState = mDragHelper.getViewDragState();
        return viewDragState == ViewDragHelper.STATE_DRAGGING || viewDragState == ViewDragHelper.STATE_SETTLING;
    }

    public static abstract class SlideListener {
        /**
         * 侧边栏是否打开
         */
        private boolean mDrawerOpen = false;

        /**
         * 拖拽侧边栏回调
         *
         * @param left 拖动的距离，左负 右正
         */
        protected abstract void onSlideDrawer(int left);

        /**
         * 侧边栏完全打开时的回调
         */
        protected abstract void onOpenDrawer();

        /**
         * 侧边栏完全关闭时的回调
         */
        protected abstract void onCloseDrawer();

        /**
         * 侧滑完全关闭整个view的回调
         */
        protected abstract void onSlideCloseSwitchLayout();
    }

    public abstract static class Adapter {
        public VerticalSwitchLayout mHostView;

        /**
         * 获取预览view
         *
         * @param convertView 复用的view
         * @param pos         列表指定序列
         * @return 预览view
         */
        protected abstract View getPreContentView(View convertView, int pos);

        /**
         * 获取内容承载view
         *
         * @param convertView 复用的view
         * @param pos         列表指定序列
         * @return 内容承载view
         */
        protected abstract View getContentView(View convertView, int pos);

        /**
         * 获取侧边栏承载view
         *
         * @param convertView
         * @param pos
         * @return
         */
        protected View getDrawerView(View convertView, int pos) {
            return null;
        }

    }

    public void setScrollStateListener(VerticalSwitchLayout.ScrollStateListener scrollStateListener) {
        this.mScrollStateListener = scrollStateListener;
    }

    private VerticalSwitchLayout.ScrollStateListener mScrollStateListener;

    public static abstract class ScrollStateListener {
        protected abstract void onPageSelected(int position);

        protected abstract void onScrollStateChanged(int state);
    }
}

