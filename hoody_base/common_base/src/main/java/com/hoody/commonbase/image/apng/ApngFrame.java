package com.hoody.commonbase.image.apng;

import android.graphics.Bitmap;

import com.facebook.imagepipeline.animated.base.AnimatedImageFrame;

import ar.com.hjg.pngj.chunks.PngChunkFCTL;

public class ApngFrame implements AnimatedImageFrame {
    private static final String TAG = "ApngFrame";
    private static final boolean DEBUG = false;
    private static final float DELAY_FACTOR = 1000F;
    private PngChunkFCTL mChunk;

    ApngFrame(PngChunkFCTL pngChunk) {
        this.mChunk = pngChunk;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void renderFrame(int width, int height, Bitmap bitmap) {
    }

    @Override
    public int getDurationMs() {
        int delayNum = mChunk.getDelayNum();
        int delayDen = mChunk.getDelayDen();
        return Math.round(delayNum * DELAY_FACTOR / delayDen);
    }

    @Override
    public int getWidth() {
        return mChunk.getWidth();
    }

    @Override
    public int getHeight() {
        return mChunk.getHeight();
    }

    @Override
    public int getXOffset() {
        return mChunk.getxOff();
    }

    @Override
    public int getYOffset() {
        return mChunk.getyOff();
    }

    public PngChunkFCTL getChunk() {
        return mChunk;
    }

    byte getBlendOp() {
        return mChunk.getBlendOp();
    }
}
