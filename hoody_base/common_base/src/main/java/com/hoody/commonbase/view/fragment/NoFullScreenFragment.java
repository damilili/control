package com.hoody.commonbase.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public abstract class NoFullScreenFragment extends CloseableFragment {
    @Nullable
    @Override
    final public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = createContentView(inflater, container, savedInstanceState);
        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return contentView;
    }

    protected abstract View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
}
