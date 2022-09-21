package com.hoody.commonbase.image;

import android.graphics.Bitmap;

public interface FrescoBitmapCallback<T> {
    void onSuccess(Bitmap bitmap);
    void onSuccess(Bitmap bitmap, Object o);
}
