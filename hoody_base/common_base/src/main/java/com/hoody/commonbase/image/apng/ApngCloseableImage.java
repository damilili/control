package com.hoody.commonbase.image.apng;

import android.util.Log;

import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;


public class ApngCloseableImage extends CloseableAnimatedImage {
    private static final String TAG = "ApngCloseableImage";
    private static final boolean DEBUG = false;

    ApngCloseableImage(AnimatedImageResult imageResult) {
        super(imageResult);
    }

    @Override
    public int getSizeInBytes() {
        int sizeInBytes = super.getSizeInBytes();
        if (DEBUG) Log.d(TAG, "getSizeInBytes() sizeInBytes =" + sizeInBytes);
        return sizeInBytes;
    }

    @Override
    public void close() {
        super.close();
        if (DEBUG) Log.d(TAG, "close() called");
    }

    @Override
    public boolean isClosed() {
        return super.isClosed();
    }

    @Override
    public int getWidth() {
        return getImage().getWidth();
    }

    @Override
    public int getHeight() {
        return getImage().getHeight();
    }

}
