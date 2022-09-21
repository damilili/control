package com.hoody.commonbase.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hoody.commonbase.R;
import com.hoody.commonbase.customview.slidedecidable.SwipeBackLayout;
import com.hoody.commonbase.view.activity.IFragmentControl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;


public abstract class SwipeBackFragment extends CloseableFragment implements ISwipeBack {
    private static final String TAG = "SwipeBackFragment";
    private SwipeBackLayout mSwipeBackLayout;

    @Nullable
    @Override
    final public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = createContentView(inflater, container, savedInstanceState);
        contentView.setBackgroundColor(getResources().getColor(R.color.common_bg));
        mSwipeBackLayout = new SwipeBackLayout(getContext());
        mSwipeBackLayout.setBackgroundColor(Color.TRANSPARENT);
        mSwipeBackLayout.setSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state) {
                FragmentActivity activity = getActivity();
                switch (state) {
                    case SwipeBackLayout.STATE_DRAGGING:
                        if (activity instanceof IFragmentControl) {
                            ((IFragmentControl) activity).showPreFragmentView(getTag());
                        }
                        break;
                    case SwipeBackLayout.STATE_IDLE:
                        if (mSwipeBackLayout.isClosed()) {
                            close();
                        } else {
                            if (activity instanceof IFragmentControl) {
                                ((IFragmentControl) activity).hidePreFragmentView(getTag());
                            }
                        }

                        break;
                }
            }
        });
        mSwipeBackLayout.addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mSwipeBackLayout.setContentView(contentView);
        return mSwipeBackLayout;
    }

    protected abstract View createContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    final public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    @Override
    final public void setSwipeBackEnable(boolean enable) {
        if (mSwipeBackLayout != null) {
            mSwipeBackLayout.setSwipeEnable(enable);
        }
    }

    @Override
    final public void scrollToClose() {
        if (mSwipeBackLayout != null) {
            mSwipeBackLayout.scrollToFinish();
        }
    }
}
