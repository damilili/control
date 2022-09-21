package com.hoody.commonbase.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

/**
 * Created by zhaole on 2016/3/23.
 */
public class FrescoUtils {
    private static PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder();
    private static ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse("def"));


    /**
     * 加载指定宽高的图片
     *
     * @param imageView
     * @param url
     * @param resId     加载本地图片
     * @param defResId  占位图
     * @param width
     * @param height
     */
    private static void display(SimpleDraweeView imageView, String url, int resId, int defResId, int width, int height, boolean animPic, OnImageLoadingListener onImageLoadingListener) {
        if (imageView == null) {
            return;
        }
        ImageRequest request;
        ImageRequestBuilder imageRequestBuilder = null;
        GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
        if (hierarchy != null && defResId != -1) {
            hierarchy.setPlaceholderImage(defResId);
        }
        if (!TextUtils.isEmpty(url)) {
            imageRequestBuilder = requestBuilder.setSource(Uri.parse(url));
        } else if (resId != -1) {
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
            imageRequestBuilder = requestBuilder.setSource(uri);
        }

        if (imageRequestBuilder != null) {
            ImageDecodeOptionsBuilder imageDecodeOptionsBuilder = new ImageDecodeOptionsBuilder();
            imageDecodeOptionsBuilder.setFrom(requestBuilder.getImageDecodeOptions());
            imageDecodeOptionsBuilder.setDecodeAllFrames(animPic);
            imageRequestBuilder.setImageDecodeOptions(imageDecodeOptionsBuilder.build());
            int densityDpi = Initializer.getContext().getResources().getConfiguration().densityDpi;
            int widthDp = Initializer.getContext().getResources().getConfiguration().screenWidthDp;
            int screenHeightDp = Initializer.getContext().getResources().getConfiguration().screenHeightDp;
            int width1 = width > 0 ? width : widthDp * densityDpi;
            int height1 = height > 0 ? height : screenHeightDp * densityDpi;
            if (Initializer.isDarkPattern()) {
                request = imageRequestBuilder.setResizeOptions(new ResizeOptions(width1, height1))
                        .setPostprocessor(darkPostprocessor)
                        .build();
            } else {
                request = imageRequestBuilder.setResizeOptions(new ResizeOptions(width1, height1))
                        .build();
            }

            imageView.setController(builder.setOldController(imageView.getController())
                    .setControllerListener(onImageLoadingListener)
                    .setImageRequest(request)
                    .setAutoPlayAnimations(true)
                    .build());
        } else {
            imageView.setImageURI(url);
        }
    }

    /**
     * 以高斯模糊显示
     *
     * @param draweeView View
     * @param url        url.
     * @param iterations 迭代次数，越大越魔化。
     * @param blurRadius 模糊图半径，必须大于0，越大越模糊。
     */
    public static void showUrlBlur(SimpleDraweeView draweeView, String url, int iterations, int blurRadius) {
        try {
            Uri uri = Uri.parse(url);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setPostprocessor(new IterativeBoxBlurPostProcessor(iterations, blurRadius))
                    .build();
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .build();
            draweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showUrlBlur(SimpleDraweeView draweeView, int resId, int iterations, int blurRadius) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
            showUrlBlur(draweeView, uri.toString(), iterations, blurRadius);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 展示gif图
     *
     * @param draweeView View
     * @param url        url.
     */
    public static void displayApng(SimpleDraweeView draweeView, String url) {
        if (draweeView == null) {
            return;
        }
        try {
            display(draweeView, url, -1, -1, 0, 0, true, null);
            Animatable animation = draweeView.getController().getAnimatable();
            animation.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止gif图
     *
     * @param draweeView View
     */
    public static void stopApng(SimpleDraweeView draweeView) {
        if (draweeView == null || draweeView.getController() == null) {
            return;
        }
        try {
            Animatable animation = draweeView.getController().getAnimatable();
            animation.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setOverlayImage(SimpleDraweeView imageView, int resId) {
        if (imageView == null) {
            return;
        }
        GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
        if (resId > 0) {
            Drawable drawable = imageView.getResources().getDrawable(resId);
            hierarchy.setOverlayImage(drawable);
        } else {
            hierarchy.setOverlayImage(null);
        }
    }

    /**
     * 以高斯模糊+半透明遮罩
     */
    public static void showUrlOverlayBlur(SimpleDraweeView draweeView, String url, int iterations, int blurRadius) {
        try {
            Uri uri = Uri.parse(url);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setPostprocessor(new IterativeBoxBlurPostProcessor(iterations, blurRadius) {
                        @Override
                        public void process(Bitmap bitmap) {
                            Bitmap overlayBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                            overlayBitmap.eraseColor(Color.parseColor("#B2000000"));
                            Canvas canvas = new Canvas(bitmap);
                            canvas.drawBitmap(overlayBitmap, 0, 0, null);
                            super.process(bitmap);
                            overlayBitmap.recycle();
                            overlayBitmap = null;
                        }
                    })
                    .build();
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .build();
            draweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDarkImage(SimpleDraweeView draweeView, int resId) {
        if (resId == -1) {
            return;
        }
        try {
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
//            Uri uri = Uri.parse(url);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setPostprocessor(darkPostprocessor)
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController)
                    Fresco.newDraweeControllerBuilder()
                            .setImageRequest(request)
                            .setOldController(draweeView.getController())
                            .build();
            draweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Postprocessor darkPostprocessor = new BasePostprocessor() {
        @Override
        public String getName() {
            return "darkPostprocessor";
        }

        @Override
        public void process(Bitmap bitmap) {
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();

            ColorMatrix cMatrix = new ColorMatrix();
            cMatrix.setSaturation(0.15f);
            paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    };

    /**
     * 监听网络图片下载
     */
    public static class OnImageLoadingListener extends BaseControllerListener<ImageInfo> {
        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
            super.onFinalImageSet(id, imageInfo, animatable);
        }

        @Override
        public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
            super.onIntermediateImageSet(id, imageInfo);
        }

        @Override
        public void onIntermediateImageFailed(String id, Throwable throwable) {
            super.onIntermediateImageFailed(id, throwable);
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            super.onFailure(id, throwable);
        }
    }

    /**
     * 获取缓存图片
     *
     * @param url
     * @return
     */
    public static Bitmap getCacheBitmap(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Uri uri = Uri.parse(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        if (imagePipeline == null) {
            return null;
        }
//        boolean inMemoryCache = imagePipeline.isInBitmapMemoryCache(uri);
//        if (!inMemoryCache){
//            return null;
//        }
        ImageRequest request = ImageRequest.fromUri(uri);
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchImageFromBitmapCache(request, null);
        CloseableReference<CloseableImage> imageReference = dataSource.getResult();
        try {
            if (imageReference == null) {
                imageReference = imagePipeline.fetchDecodedImage(request, null).getResult();
            }
            if (imageReference == null) {
                return null;
            }
            CloseableBitmap closeableImage = (CloseableBitmap) imageReference.get();
            return closeableImage.getUnderlyingBitmap();
        } finally {
            dataSource.close();
            CloseableReference.closeSafely(imageReference);
        }
    }

    public static void getFrescoBitmap(final String url, final FrescoBitmapCallback<Bitmap> callback) {
        Fresco.getImagePipeline().fetchDecodedImage(ImageRequest.fromUri(url), null).subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(final Bitmap bitmap) {
                if (callback == null)
                    return;
                if (bitmap != null && !bitmap.isRecycled()) {
                    Bitmap resultBitmap = bitmap.copy(bitmap.getConfig(), bitmap.isMutable());
                    if (resultBitmap != null && !resultBitmap.isRecycled())
                        callback.onSuccess(resultBitmap);
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            }
        }, UiThreadImmediateExecutorService.getInstance());
    }

    public static void getFrescoBitmap(final String url, final FrescoBitmapCallback<Bitmap> callback, final Object o) {
        Fresco.getImagePipeline().fetchDecodedImage(ImageRequest.fromUri(url), null).subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(final Bitmap bitmap) {
                if (callback == null)
                    return;
                if (bitmap != null && !bitmap.isRecycled()) {
                    Bitmap resultBitmap = bitmap.copy(bitmap.getConfig(), bitmap.isMutable());
                    if (resultBitmap != null && !resultBitmap.isRecycled())
                        callback.onSuccess(resultBitmap, o);
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            }
        }, UiThreadImmediateExecutorService.getInstance());
    }

    public static class Loader {
        private Loader() {
        }

        public static Loader getInstance() {
            return new Loader();
        }

        SimpleDraweeView imageView;
        String url;
        int resId = -1;
        int defResId = -1;
        int width = 0;
        int height = 0;
        boolean animPic;
        OnImageLoadingListener onImageLoadingListener;

        public Loader setImageView(SimpleDraweeView imageView) {
            this.imageView = imageView;
            return this;
        }

        public Loader setUrl(String url) {
            this.url = url;
            this.resId = -1;
            return this;
        }

        public Loader setResId(int resId) {
            this.url = null;
            this.resId = resId;
            return this;
        }

        public Loader setDefResId(int defResId) {
            this.defResId = defResId;
            return this;
        }

        public Loader setWidth(int width) {
            this.width = width;
            return this;
        }

        public Loader setHeight(int height) {
            this.height = height;
            return this;
        }

        public Loader setAnimPic(boolean animPic) {
            this.animPic = animPic;
            return this;
        }

        public Loader setOnImageLoadingListener(OnImageLoadingListener onImageLoadingListener) {
            this.onImageLoadingListener = onImageLoadingListener;
            return this;
        }

        public void load() {
            if (Initializer.getContext() == null) {
                throw new IllegalStateException("请初始化 :com.hoody.image.Initializer.init()");
            }
            display(imageView, url, resId, defResId, width, height, animPic, onImageLoadingListener);
        }
    }
}
