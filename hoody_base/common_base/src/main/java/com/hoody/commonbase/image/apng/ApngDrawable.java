package com.hoody.commonbase.image.apng;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;

public class ApngDrawable extends Drawable implements Animatable, Runnable {
    static String TAG = "ApngDrawable";
    //循环次数
    private final int mMaxLoopCount;
    private AnimatedImageResult mImageResult;
    private Paint mPaint;
    private boolean mRunning = false;
    private int mCurrentFrame = -1;
    private float mScall = 0F;
    private int mCurLoopCount = 0;
    private AnimCallback mAnimCallback;
    private Bitmap currentBitmap;
    ApngDrawable(AnimatedImageResult imageResult) {
        super();
        this.mImageResult = imageResult;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        //这个值必须不小于0 等于0的时候表示无限循环
        mMaxLoopCount = Math.max(imageResult.getImage().getLoopCount(), 0);
    }


    @Override
    public void start() {
        if (!isRunning()) {
            mRunning = true;
            mCurrentFrame = -1;
            if (mImageResult != null) {
                mCurLoopCount = 0;
                run();
            } else {
                stop();
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            unscheduleSelf(this);
            mRunning = false;
        }
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void run() {
        mCurrentFrame++;
        if (mCurrentFrame < 0) {
            mCurrentFrame = 0;
        } else if (mCurrentFrame > mImageResult.getImage().getFrameCount() - 1) {
            mCurrentFrame = 0;
        }
        CloseableReference<Bitmap> decodedFrame = mImageResult.getDecodedFrame(mCurrentFrame);
        if (decodedFrame != null) {
            currentBitmap = decodedFrame.get();
        }
        int delay = mImageResult.getImage().getFrame(mCurrentFrame).getDurationMs();
        scheduleSelf(this, SystemClock.uptimeMillis() + delay);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCurrentFrame < 0) {
            drawBaseBitmap(canvas);
        } else {
            drawAnimateBitmap(canvas, mCurrentFrame);
        }
        if (isRunning()) {
            if (mCurrentFrame == mImageResult.getImage().getFrameCount() - 1) {
                mCurLoopCount++;
                if (mAnimCallback != null) {
                    mAnimCallback.onAnimationRepeat(this);
                }
                if (mCurLoopCount == mMaxLoopCount) {
                    stop();
                }
                if (mCurLoopCount == 100000) {
                    //防止无线循环次数过大
                    mCurLoopCount = 0;
                }
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private void drawBaseBitmap(Canvas canvas) {
        Bitmap baseBitmap = mImageResult.getPreviewBitmap().get();
        if (mScall == 0F) {
            float scalingByWidth = ((float) canvas.getWidth()) / baseBitmap.getWidth();
            float scalingByHeight = ((float) canvas.getHeight()) / baseBitmap.getHeight();
            mScall = scalingByWidth <= scalingByHeight ? scalingByWidth : scalingByHeight;
        }
        RectF dst = new RectF(0, 0, mScall * baseBitmap.getWidth(), mScall * baseBitmap.getHeight());
        canvas.drawBitmap(baseBitmap, null, dst, mPaint);
    }

    private void drawAnimateBitmap(Canvas canvas, int frameIndex) {
        if (currentBitmap == null) {
            return;
        }
        if (mScall == 0F) {
            float scalingByWidth = ((float) canvas.getWidth()) / currentBitmap.getWidth();
            float scalingByHeight = ((float) canvas.getHeight()) / currentBitmap.getHeight();
            mScall = scalingByWidth <= scalingByHeight ? scalingByWidth : scalingByHeight;
        }
        RectF dst = new RectF(
                0, 0,
                mScall * currentBitmap.getWidth(),
                mScall * currentBitmap.getHeight());

        canvas.drawBitmap(currentBitmap, null, dst, mPaint);
    }
    public void setAnimCallback(AnimCallback animCallback) {
        this.mAnimCallback = animCallback;
    }
    public interface AnimCallback {
        void onAnimationRepeat(ApngDrawable apngDrawable);
    }

}
