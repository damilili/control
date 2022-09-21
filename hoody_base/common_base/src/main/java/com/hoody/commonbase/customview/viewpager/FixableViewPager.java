package com.hoody.commonbase.customview.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * 可以控制能不能滑动的viewpager
 * 通过回调来决定viewpager是不是可以滑动
 */
public class FixableViewPager extends ViewPager {
    private boolean mCanSwitch = true;

    public FixableViewPager(@NonNull Context context) {
        super(context);
    }

    public FixableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSwitchDecider != null) {
                mCanSwitch = mSwitchDecider.canSwitch();
            }
        }
        if (!mCanSwitch) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSwitchDecider != null) {
                mCanSwitch = mSwitchDecider.canSwitch();
            }
        }
        if (!mCanSwitch) {
            return false;
        }
        return super.onTouchEvent(ev);
    }

    private SwitchDecider mSwitchDecider;

    public void setSwitchDecider(SwitchDecider switchDecider) {
        this.mSwitchDecider = switchDecider;
    }

    public interface SwitchDecider {
        boolean canSwitch();
    }
}
