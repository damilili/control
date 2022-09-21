package com.hoody.commonbase.image.apng;

import android.graphics.Bitmap;

import com.facebook.common.internal.ByteStreams;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatedImageResult;
import com.facebook.imagepipeline.animated.factory.AnimatedFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.bitmaps.SimpleBitmapReleaser;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.QualityInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ApngDecoder extends ImageDecoder {
    private static final String TAG = "ApngDecoder";
    private static ImageDecoder instance;
    private ImageDecoder mProxyInstance;
    public static ImageDecoder getInstance() {
        if (instance == null) {
            instance = new ApngDecoder();
        }
        return instance;
    }

    private void newProxyInstanceIfNeed() {
        if (mProxyInstance == null) {
            AnimatedFactory animatedFactory = ImagePipelineFactory.getInstance().getAnimatedFactory();
            AnimatedImageFactory animatedImageFactory;
            if (animatedFactory != null) {
                animatedImageFactory = ImagePipelineFactory.getInstance().getAnimatedFactory().getAnimatedImageFactory();
            } else {
                animatedImageFactory = null;
            }
            mProxyInstance = new ImageDecoder(animatedImageFactory, ImagePipelineFactory.getInstance().getPlatformDecoder(), Bitmap.Config.ARGB_8888);
        }
    }

    private ApngDecoder() {
        super(null, null, null);
    }

    @Override
    public CloseableImage decodeImage(EncodedImage encodedImage, int length, QualityInfo qualityInfo, ImageDecodeOptions options) {
        newProxyInstanceIfNeed();
        if (options.decodeAllFrames) {
            InputStream inputStream = encodedImage.getInputStream();
            boolean isApng = isApng(inputStream);
            if (isApng) {
                ApngReader reader = new ApngReader(inputStream);
                reader.end();
                List<Bitmap> resultBitmaps = reader.getResultBitmaps();
                ApngImage image = new ApngImage(reader.getFctlList(), resultBitmaps, reader.getNumPlays());
                return getCloseableImage(image, resultBitmaps);
            }
        }
        return mProxyInstance.decodeImage(encodedImage, length, qualityInfo, options);
    }

    private boolean isApng(InputStream inputStream) {
        byte[] bytes = new byte[41];
        int read = 0;
        boolean isApng = false;
        if (inputStream.markSupported()) {
            try {
                inputStream.mark(41);
                read = ByteStreams.read(inputStream, bytes, 0, 41);
                inputStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ByteStreams.read(inputStream, bytes, 0, 41);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (read > 0) {
            String s = new String(bytes);
            if (s.endsWith("acTL")) {
                isApng = true;
            }
        }
        try {
            inputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isApng;
    }


    private CloseableAnimatedImage getCloseableImage(ApngImage image, List<Bitmap> resultBitmaps) {
        List<CloseableReference<Bitmap>> decodedFrames = null;
        CloseableReference<Bitmap> previewBitmap = null;
        try {
            decodedFrames = new ArrayList<>();
            for (int i = 0; i < resultBitmaps.size(); i++) {
                CloseableReference<Bitmap> bitmap2 = CloseableReference.of(resultBitmaps.get(i), SimpleBitmapReleaser.getInstance());
                decodedFrames.add(bitmap2);
            }
            previewBitmap = CloseableReference.cloneOrNull(decodedFrames.get(0));
            AnimatedImageResult animatedImageResult = AnimatedImageResult.newBuilder(image)
                    .setPreviewBitmap(previewBitmap)
                    .setFrameForPreview(0)
                    .setDecodedFrames(decodedFrames)
                    .build();
            return new ApngCloseableImage(animatedImageResult);
        } finally {
            CloseableReference.closeSafely(previewBitmap);
            CloseableReference.closeSafely(decodedFrames);
        }
    }
}
