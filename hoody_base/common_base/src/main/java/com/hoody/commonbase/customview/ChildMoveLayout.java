package com.hoody.commonbase.customview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hoody.commonbase.util.DeviceInfo;


public class ChildMoveLayout extends FrameLayout {
    private String TAG = ChildMoveLayout.class.getName();
    private Paint mPaint;
    private View mTargetView;

    private static final int SPLIT_COUNT = 24;
    public static final int SPLIT_LENGTH = DeviceInfo.WIDTH / SPLIT_COUNT;
    private Rect mRect;
    private ValueAnimator mChildAnimator;

    public ChildMoveLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public ChildMoveLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChildMoveLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.BLUE);
        setWillNotDraw(false);
        setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                View dragView = (View) event.getLocalState();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        dragView.setVisibility(View.INVISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        getRect(event, dragView);
                        invalidate();
                        break;
                    case DragEvent.ACTION_DROP:
                        getRect(event, dragView);
                        dragView.setX(mRect.left);
                        dragView.setY(mRect.top);
                        dragView.setVisibility(View.VISIBLE);
                        if (mOnViewPositionChangeedListener != null) {
                            mOnViewPositionChangeedListener.onViewPositionChangeed(dragView);
                        }
                        mTargetView = null;
                        break;
                    default:
                        break;
                }
                return true;
            }

            private void getRect(DragEvent event, View dragView) {
                int x = (int) (event.getX() - dragView.getWidth() / 2);
                int y = (int) (event.getY() - dragView.getHeight() / 2);
                int x1 = (int) x / SPLIT_LENGTH * SPLIT_LENGTH;
                int x2 = (int) (x + dragView.getWidth()) / SPLIT_LENGTH * SPLIT_LENGTH + SPLIT_LENGTH;
                int y1 = (int) y / SPLIT_LENGTH * SPLIT_LENGTH;
                if (x - x1 < x2 - (x + dragView.getWidth())) {
                    mRect.set(x1, y1, x1 + dragView.getWidth(), y1 + dragView.getHeight());
                } else {
                    mRect.set(x2 - dragView.getWidth(), y1, x2, y1 + dragView.getHeight());
                }
            }
        });
    }

    private OnLongClickListener mOnChildLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            DragShadowBuilder builder = new DragShadowBuilder(v);
            ClipData.Item clipDataItem = new ClipData.Item("111");
            ClipData clipData = new ClipData("111", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, clipDataItem);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(clipData, builder, v, 0);
            } else {
                v.startDrag(clipData, builder, v, 0);
            }
            mTargetView = v;
            return true;
        }
    };

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        child.setOnLongClickListener(mOnChildLongClickListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTargetView != null) {
            mPaint.setColor(Color.parseColor("#88333333"));
            mPaint.setStrokeWidth(2);
            for (int i = 1; i < SPLIT_COUNT; i++) {
                canvas.drawLine(i * SPLIT_LENGTH, 0, i * SPLIT_LENGTH, DeviceInfo.HEIGHT, mPaint);
            }
            int i = 0;
            while (true) {
                canvas.drawLine(0, (i + 1) * SPLIT_LENGTH, DeviceInfo.WIDTH, (i + 1) * SPLIT_LENGTH, mPaint);
                i++;
                if ((i + 1) * SPLIT_LENGTH > DeviceInfo.HEIGHT) {
                    break;
                }
            }
            if (mRect != null) {
                mPaint.setStrokeWidth(2);
                mPaint.setColor(0x88ffffff);
                Paint.Style style = mPaint.getStyle();
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(mRect, mPaint);
                mPaint.setStyle(style);
            }
        }
    }

    public void stopAnim() {
        if (mChildAnimator != null) {
            mChildAnimator.cancel();
        }
    }

    public void startAnim() {
        mChildAnimator = ValueAnimator.ofFloat(0, 3f, 0, -3f);
        mChildAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).setRotation((Float) animation.getAnimatedValue());
                }
            }
        });
        mChildAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mChildAnimator != null) {
                    mChildAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).setRotation(0);
                }
                mChildAnimator = null;
            }
        });
        mChildAnimator.setDuration(200);
        mChildAnimator.start();
    }

    private OnViewPositionChangeedListener mOnViewPositionChangeedListener;

    public void setOnViewPositionChangeedListener(OnViewPositionChangeedListener listener) {
        mOnViewPositionChangeedListener = listener;
    }

    public interface OnViewPositionChangeedListener {
        void onViewPositionChangeed(View positionChangedView);
    }
}
