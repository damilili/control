package com.hoody.commonbase.customview.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class BannerViewPager extends ViewPager {
    private static final boolean DEBUG = false;
    private static final String TAG = "BannerViewPager";
    private Runnable mScrollAction;
    private int mDuration = 2000;

    public BannerViewPager(@NonNull Context context) {
        super(context);
    }

    public BannerViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = getCurrentItem();
                    int trueCount = getAdapter().getCount() / 3;
                    if ((trueCount > position) || (2 * trueCount <= position)) {
                        int i = position % trueCount;
                        Log.d("onPageSelectedTAG", "onPageSelected() called with: position = [" + i + "] " + position);
                        setCurrentItem(trueCount + i, false);
                    }
                }
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) {
            Log.d(TAG, "onAttachedToWindow() called");
        }
        startAnim();
    }

    private void startAnim() {
        if (mScrollAction == null) {
            mScrollAction = new Runnable() {
                @Override
                public void run() {
                    PagerAdapter adapter = getAdapter();
                    if (adapter != null) {
                        int position = getCurrentItem() + 1;
                        if (DEBUG) {
                            Log.d(TAG, "run() called position = " + position);
                        }
                        setCurrentItem(position);
                        postDelayed(this, mDuration);
                    }
                }
            };
        }
        postDelayed(mScrollAction, mDuration);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
            Log.d(TAG, "onDetachedFromWindow() called");
        }
        if (mScrollAction != null) {
            removeCallbacks(mScrollAction);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mScrollAction != null) {
                    removeCallbacks(mScrollAction);
                }
                break;
            case MotionEvent.ACTION_UP:
                startAnim();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        if (!(adapter instanceof BannerPagerAdapter)) {
            throw new IllegalArgumentException("必须使用 com.hoody.customview.view.viewpager.BannerViewPager.BannerPagerAdapter 的实例");
        }
        super.setAdapter(adapter);
    }

    public static abstract class BannerPagerAdapter extends PagerAdapter {
        @Override
        final public int getCount() {
            return getItemCount() * 3;
        }

        public abstract int getItemCount();

        @Override
        final public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            destroyPagerItem(container, position, object);
        }

        public abstract void destroyPagerItem(@NonNull ViewGroup container, int position, @NonNull Object object);

        @NonNull
        @Override
        final public Object instantiateItem(@NonNull ViewGroup container, int position) {
            return instantiatePagerItem(container, position % getItemCount());
        }

        public abstract Object instantiatePagerItem(@NonNull ViewGroup container, int position);

    }
}
