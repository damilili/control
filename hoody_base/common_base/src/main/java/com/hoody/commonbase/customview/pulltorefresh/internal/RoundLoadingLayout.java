package com.hoody.commonbase.customview.pulltorefresh.internal;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hoody.commonbase.R;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;



/**
 * Created by cdm on 2019/4/4.
 */

public class RoundLoadingLayout extends AbstractLoadingLayout {

    private final ImageView pull_refresh_image;
    private final CircularProgressDrawable mProgress;
    private boolean startPull = false;

    public RoundLoadingLayout(Context context) {
        super(context);
        ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.kwjx_pull_refresh_round_loading, this);
        pull_refresh_image = header.findViewById(R.id.pull_refresh_image);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.setIntrinsicWidth(100);
        shapeDrawable.setIntrinsicHeight(100);
        shapeDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        pull_refresh_image.setBackgroundDrawable(shapeDrawable);
        mProgress = new CircularProgressDrawable(getContext());
        mProgress.setArrowEnabled(true);
        mProgress.setStyle(CircularProgressDrawable.DEFAULT);
        mProgress.setStrokeWidth(dip2px(2.5f));
        mProgress.setStrokeCap(Paint.Cap.SQUARE);
        mProgress.setStartEndTrim(0, 0);
        mProgress.setCenterRadius(dip2px(8));

        pull_refresh_image.setImageDrawable(mProgress);
    }

    public int dip2px( float dpValue) {
        return (int) (dpValue * getResources().getDisplayMetrics().density + 0.5f);
    }
    @Override
    public void pullToRefresh() {
        Log.d("RoundLoadingLayout", "pullToRefresh() called");
    }

    @Override
    public void releaseToRefresh() {
        Log.d("RoundLoadingLayout", "releaseToRefresh() called");
    }

    @Override
    public void refreshing() {
        Log.d("RoundLoadingLayout", "refreshing() called");
        mProgress.start();
    }

    @Override
    public void reset() {
        Log.d("RoundLoadingLayout", "reset() called");
        startPull = false;
    }

    @Override
    public void setPullLabel(String pullLabel) {
        Log.d("RoundLoadingLayout", "setPullLabel() called with: pullLabel = [" + pullLabel + "]");
    }


    @Override
    public void setRefreshingLabel(String refreshingLabel) {

    }

    @Override
    public void setReleaseLabel(String releaseLabel) {

    }


    @Override
    public void setTextColor(int color) {

    }

    @Override
    public void setTextVisibility(int visibility) {

    }

    @Override
    public void startPull() {
        Log.d("RoundLoadingLayout", this + "startPull() called");
        startPull = true;
        pull_refresh_image.setScaleX(1);
        pull_refresh_image.setScaleY(1);
        mProgress.setStartEndTrim(0, 0);
        setTranslationY(0);
    }


    @Override
    public boolean onMove(float dragY, boolean byUser) {
        if (byUser) {
            if (dragY < 0) {
                dragY = -dragY;
            }
            dragY = dragY - getHeight() * 2 / 3;
            if (dragY < 0) {
                dragY = 0;
            }
            mProgress.setProgressRotation(dragY / getHeight());
            mProgress.setStartEndTrim(0, dragY / getHeight() > 0.8f ? 0.8f : dragY / getHeight());
            mProgress.setArrowEnabled(true);
            mProgress.setArrowScale(dragY / getHeight() > 1 ? 1 : dragY / getHeight());
            pull_refresh_image.setScaleX(1);
            pull_refresh_image.setScaleY(1);
            return false;
        }
        if (!startPull) {
            if (Math.abs(dragY / getHeight()) > 1) {
                return false;
            }
            pull_refresh_image.setScaleX(Math.abs(dragY / getHeight()) > 1 ? 1 : Math.abs(dragY / getHeight()));
            pull_refresh_image.setScaleY(Math.abs(dragY / getHeight()) > 1 ? 1 : Math.abs(dragY / getHeight()));
            mProgress.setStartEndTrim(mProgress.getStartTrim(), mProgress.getEndTrim());
            if (dragY==0) {
                mProgress.stop();
            }
            return true;
        }
        return false;
    }
}
