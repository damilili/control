package com.hoody.commonbase.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;


public class NoScrollGridView extends GridView {

	private OnTouchBlankPositionListener mTouchBlankPosListener;
	private float mTouchX;
	private float mTouchY;

	public NoScrollGridView(Context context) {
		super(context);
	}
	public NoScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec); 
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_MOVE){
            return true;//禁止Gridview进行滑动
        }
        return super.dispatchTouchEvent(ev);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mTouchBlankPosListener != null) {
			if (!isEnabled()) {
				return isClickable() || isLongClickable();
			}
			int action = event.getActionMasked();
			final int motionPosition = pointToPosition((int) event.getX(), (int) event.getY());
			if (motionPosition == INVALID_POSITION) {
				switch (action) {
					case MotionEvent.ACTION_UP:
						mTouchBlankPosListener.onTouchBlank(this);
						break;
				}
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 设置GridView的空白区域的触摸事件
	 *
	 * @param listener
	 */
	public void setOnTouchBlankPositionListener(
			OnTouchBlankPositionListener listener) {
		mTouchBlankPosListener = listener;
	}

	public interface OnTouchBlankPositionListener {
		void onTouchBlank(View view);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}
}