package com.hoody.commonbase.image.apng;

import android.graphics.drawable.Drawable;

import com.facebook.drawee.backends.pipeline.DrawableFactory;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.image.CloseableImage;


public class ApngDrawableFactory implements DrawableFactory {
    private static final String TAG = "ApngDrawableFactory";

    @Override
    public boolean supportsImageType(CloseableImage image) {
        return image instanceof ApngCloseableImage;
    }

    @Override
    public Drawable createDrawable(CloseableImage image) {
        Drawable apngDrawable = null;
        if (image instanceof ApngCloseableImage) {
            AnimatedImageResult imageResult = ((ApngCloseableImage) image).getImageResult();
            apngDrawable = new ApngDrawable(imageResult);
        }
        return apngDrawable;
    }
}
