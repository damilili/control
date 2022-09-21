package com.hoody.commonbase.image;

import android.content.Context;

import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.hoody.commonbase.image.apng.ApngDecoder;
import com.hoody.commonbase.image.apng.ApngDrawableFactory;

public class Initializer {
    private static Context Context = null;
    private static boolean DarkPattern;

    private Initializer() {
    }

    public static void init(Context context, boolean darkPattern) {
        if (Context != null) {
            return;
        }
        if (context == null) {
            throw new NullPointerException("context 不能传 null");
        }
        Context = context.getApplicationContext();
        DarkPattern = darkPattern;

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(Context)
                .setDownsampleEnabled(true)
                .setImageDecoder(ApngDecoder.getInstance())
                .build();
        DraweeConfig build = DraweeConfig.newBuilder().addCustomDrawableFactory(new ApngDrawableFactory()).build();
        Fresco.initialize(Context, config, build);

    }

    public static boolean isDarkPattern() {
        return DarkPattern;
    }

    public static Context getContext() {
        return Context;
    }
}
