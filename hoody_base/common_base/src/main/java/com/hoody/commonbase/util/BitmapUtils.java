package com.hoody.commonbase.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;

import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
	public static boolean isBitmapNeedResize(String srcBmpFile, int upWidth, int upHeight) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;  //先获取一下源文件大小
		BitmapFactory.decodeFile(srcBmpFile,opts);
		int upMaxSize = upWidth>upHeight?upWidth:upHeight;
		int srcMaxSize = opts.outWidth>opts.outHeight?opts.outWidth:opts.outHeight;
		if(srcMaxSize>upMaxSize*2){
			return true;  //如果源图大于目标二倍，则必须缩放加载
		}
		return false;
	}

	public static void saveNv21ByteArrayToPicture(byte[] nv21bytearray,int width,int height,String filePath){
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			YuvImage yuvImage = new YuvImage(nv21bytearray, ImageFormat.NV21, width, height, null);
			yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, fos);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取图片的旋转角度
	 * @param path
	 * @return
	 */
	public static int getPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 根据图片的旋转角度 还原图片的旋转
	 * @param bitmap
	 * @param rotate
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
		if (bitmap == null)
			return null;
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix mtx = new Matrix();
		mtx.postRotate(rotate);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

}
