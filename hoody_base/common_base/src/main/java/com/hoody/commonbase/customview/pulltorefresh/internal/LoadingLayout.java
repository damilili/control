package com.hoody.commonbase.customview.pulltorefresh.internal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hoody.commonbase.R;
import com.hoody.commonbase.customview.pulltorefresh.PullToRefreshBase;


public class LoadingLayout extends AbstractLoadingLayout {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

	private final ImageView headerImage;
	private final ProgressBar headerProgress;
	private final TextView headerText;

	private String pullLabel;
	private String refreshingLabel;
	private String releaseLabel;

	public LoadingLayout(Context context, final int mode, String releaseLabel, String pullLabel, String refreshingLabel) {
		super(context);
		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(getLayoutId(), this);
		headerText = (TextView) header.findViewById(R.id.pull_refresh_text);
		headerText.setText(pullLabel);
		headerImage = (ImageView) header.findViewById(R.id.pull_refresh_image);
		headerProgress = (ProgressBar) header.findViewById(R.id.pull_refresh_progress);

//		mHeaderImageMatrix = new Matrix();
//		headerImage.setImageMatrix(mHeaderImageMatrix);

		final Interpolator interpolator = new LinearInterpolator();
		rotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
		        0.5f);
		rotateAnimation.setInterpolator(interpolator);
		rotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		rotateAnimation.setFillAfter(true);

		resetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
		        Animation.RELATIVE_TO_SELF, 0.5f);
		resetRotateAnimation.setInterpolator(interpolator);
		resetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		resetRotateAnimation.setFillAfter(true);

		/* -------------------- */

		/* -------------------- */

		this.releaseLabel = releaseLabel;
		this.pullLabel = pullLabel;
		this.refreshingLabel = refreshingLabel;

		switch (mode) {
			case PullToRefreshBase.MODE_PULL_UP_TO_REFRESH:
				headerImage.setImageResource(R.drawable.kwjx_pull_refresh_up_arrow);
				break;
			case PullToRefreshBase.MODE_PULL_DOWN_TO_REFRESH:
			default:
				headerImage.setImageResource(R.drawable.kwjx_pull_refresh_down_arrow);
				break;
		}
//		Drawable imageDrawable = context.getResources().getDrawable(R.drawable.kwjx_pull_refresh_up_arrow);
//		mRotationPivotX = Math.round(imageDrawable.getIntrinsicWidth() / 2f);
//		mRotationPivotY = Math.round(imageDrawable.getIntrinsicHeight() / 2f);
	}

	private final Animation rotateAnimation, resetRotateAnimation;

	protected int getLayoutId(){
		return R.layout.kwjx_pull_refresh_loading;
	}

	@Override
	public void reset() {
		headerText.setText(pullLabel);
		headerImage.setVisibility(View.VISIBLE);
		headerImage.clearAnimation();
		headerProgress.setVisibility(View.INVISIBLE);
	}

	@Override
	public void releaseToRefresh() {
		headerText.setText(releaseLabel);
		headerImage.clearAnimation();
		headerImage.startAnimation(rotateAnimation);
	}

	@Override
	public void setPullLabel(String pullLabel) {
		this.pullLabel = pullLabel;
	}

	@Override
	public void refreshing() {
		headerText.setText(refreshingLabel);
		headerImage.clearAnimation();
		headerImage.setVisibility(View.INVISIBLE);
		headerProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onMove(float dragY,boolean byUser) {
		return false;
	}

	@Override
	public void setRefreshingLabel(String refreshingLabel) {
		this.refreshingLabel = refreshingLabel;
	}

	@Override
	public void setReleaseLabel(String releaseLabel) {
		this.releaseLabel = releaseLabel;
	}

	@Override
	public void pullToRefresh() {
		headerText.setText(pullLabel);
		headerImage.clearAnimation();
		headerImage.startAnimation(resetRotateAnimation);
	}

	//使用XML默认颜色
	@Override
	public void setTextColor(int color) {
		if (headerText != null) {
			headerText.setTextColor(color);
		}
	}

	@Override
	public void setTextVisibility(int visibility) {
		if(headerText != null){
			headerText.setVisibility(visibility);
		}
	}

	@Override
	public void startPull() {

	}
}
