package com.hoody.commonbase.image.apng;

import android.graphics.Bitmap;
import android.util.Log;

import com.facebook.imagepipeline.animated.base.AnimatedDrawableFrameInfo;
import com.facebook.imagepipeline.animated.base.AnimatedImage;

import java.util.ArrayList;
import java.util.List;

import ar.com.hjg.pngj.chunks.PngChunkFCTL;


public class ApngImage implements AnimatedImage {
    private static final String TAG = "ApngImage";
    private static final boolean DEBUG = false;

    private static final float DELAY_FACTOR = 1000F;
    private final int mFrameSize;
    private int[] mDurations;
    //总时长
    private int mSumDuration;
    private int mSizeInBytes;
    private int mWidth;
    private int mHeight;
    private int mLoopCount;
    private List<ApngFrame> mApngFrames;

    ApngImage(ArrayList<PngChunkFCTL> fctlArrayList, List<Bitmap> resultBitmaps, int loopCount) {
        mLoopCount = loopCount;
        mFrameSize = fctlArrayList.size();
        mApngFrames = new ArrayList<>(mFrameSize);
        mDurations = new int[mFrameSize];
        PngChunkFCTL pngChunkFCTL;
        for (int i = 0; i < mFrameSize; i++) {
            pngChunkFCTL = fctlArrayList.get(i);
            if (pngChunkFCTL.getDelayDen() != 0) {
                mDurations[i] = Math.round(pngChunkFCTL.getDelayNum() * DELAY_FACTOR
                        / pngChunkFCTL.getDelayDen());
            }
            mSumDuration += mDurations[i];
            ApngFrame apngFrame = new ApngFrame(pngChunkFCTL);
            mApngFrames.add(apngFrame);
        }
        Bitmap bitmap;
        for (int i = 0; i < resultBitmaps.size(); i++) {
            bitmap = resultBitmaps.get(i);
            if (bitmap == null) {
                continue;
            }
            if (i == 0) {
                mWidth = bitmap.getWidth();
                mHeight = bitmap.getHeight();
            }
            mSizeInBytes += bitmap.getByteCount();
        }
    }

    @Override
    public void dispose() {
        if (DEBUG) Log.d(TAG, "dispose() called");
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public int getFrameCount() {
        return mFrameSize;
    }

    @Override
    public int getDuration() {
        return mSumDuration;
    }

    @Override
    public int[] getFrameDurations() {
        return mDurations;
    }

    @Override
    public int getLoopCount() {
        return mLoopCount;
    }

    @Override
    public ApngFrame getFrame(int frameNumber) {
        return mApngFrames.get(frameNumber);
    }

    @Override
    public boolean doesRenderSupportScaling() {
        return false;
    }

    @Override
    public int getSizeInBytes() {
        return mSizeInBytes;
    }

    @Override
    public AnimatedDrawableFrameInfo getFrameInfo(int frameNumber) {
        ApngFrame frame = getFrame(frameNumber);
        try {
            return new AnimatedDrawableFrameInfo(
                    frameNumber,
                    frame.getXOffset(),
                    frame.getYOffset(),
                    frame.getWidth(),
                    frame.getHeight(),
                    AnimatedDrawableFrameInfo.BlendOperation.BLEND_WITH_PREVIOUS,
                    fromGifDisposalMethod(frame.getChunk().getDisposeOp()));
        } finally {
            frame.dispose();
        }
    }

    private static AnimatedDrawableFrameInfo.DisposalMethod fromGifDisposalMethod(int disposalMode) {
        if (disposalMode == 0 /* DISPOSAL_UNSPECIFIED */) {
            return AnimatedDrawableFrameInfo.DisposalMethod.DISPOSE_DO_NOT;
        } else if (disposalMode == 1 /* DISPOSE_DO_NOT */) {
            return AnimatedDrawableFrameInfo.DisposalMethod.DISPOSE_DO_NOT;
        } else if (disposalMode == 2 /* DISPOSE_BACKGROUND */) {
            return AnimatedDrawableFrameInfo.DisposalMethod.DISPOSE_TO_BACKGROUND;
        } else if (disposalMode == 3 /* DISPOSE_PREVIOUS */) {
            return AnimatedDrawableFrameInfo.DisposalMethod.DISPOSE_TO_PREVIOUS;
        } else {
            return AnimatedDrawableFrameInfo.DisposalMethod.DISPOSE_DO_NOT;
        }
    }
}
