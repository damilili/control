package com.hoody.commonbase.image.apng;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkACTL;
import ar.com.hjg.pngj.chunks.PngChunkFCTL;

/**
 * 对解析出来的原始帧进行处理
 */
public class ApngProcessor {
    private ArrayList<PngChunkFCTL> mFctlArrayList;

    private int mBaseWidth;
    private int mBaseHeight;

    /**
     * 播放次数
     */
    private int mNumPlays;

    /**
     * 根据传入的控制数据块，对原始帧进行加工处理，生成结果帧
     */
    public void process(List<PngChunk> pngChunks) {
        PngChunk chunk;
        mFctlArrayList = new ArrayList<>();
        for (int i = 0; i < pngChunks.size(); i++) {
            chunk = pngChunks.get(i);
            if (chunk instanceof PngChunkFCTL) {
                mFctlArrayList.add((PngChunkFCTL) chunk);
            }
            if (chunk instanceof PngChunkACTL) {
                mNumPlays = ((PngChunkACTL) chunk).getNumPlays();
            }
        }
        Bitmap bitmap = getRawBitmap(0);
        mBaseWidth = bitmap.getWidth();
        mBaseHeight = bitmap.getHeight();
        for (int i = 0; i < mFctlArrayList.size(); i++) {
            Bitmap animateBitmap = createAnimateBitmap(i);
            cacheResultBitmap(i, animateBitmap);
        }
    }

    private Bitmap createAnimateBitmap(int frameIndex) {
        Bitmap bitmap = null;
        PngChunkFCTL previousChunk = frameIndex > 0 ? mFctlArrayList.get(frameIndex - 1) : null;
        if (previousChunk != null) {
            bitmap = handleDisposeOperation(frameIndex, previousChunk);
        }

        Bitmap frameBitmap = getRawBitmap(frameIndex);

        PngChunkFCTL pngChunkFCTL = mFctlArrayList.get(frameIndex);
        byte blendOp = pngChunkFCTL.getBlendOp();
        int offsetX = pngChunkFCTL.getxOff();
        int offsetY = pngChunkFCTL.getyOff();
        return handleBlendingOperation(offsetX, offsetY, blendOp, frameBitmap, bitmap);
    }

    private Bitmap handleDisposeOperation(int frameIndex, PngChunkFCTL previousChunk) {
        Bitmap bitmap = null;

        byte disposeOp = previousChunk.getDisposeOp();
        int offsetX = previousChunk.getxOff();
        int offsetY = previousChunk.getyOff();
        Canvas tempCanvas;
        Bitmap frameBitmap;
        Bitmap tempBitmap;
        switch (disposeOp) {
            case PngChunkFCTL.APNG_DISPOSE_OP_NONE:
                bitmap = frameIndex > 0 ? getCacheResultBitmap(frameIndex - 1) : null;
                break;

            case PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND:
                bitmap = frameIndex > 0 ? getCacheResultBitmap(frameIndex - 1) : null;
                if (bitmap == null) break;
                frameBitmap = getRawBitmap(frameIndex - 1);
                tempBitmap = Bitmap.createBitmap(mBaseWidth, mBaseHeight, Bitmap.Config.ARGB_8888);
                tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(bitmap, 0, 0, null);
                tempCanvas.clipRect(offsetX, offsetY, offsetX + frameBitmap.getWidth(), offsetY + frameBitmap.getHeight());
                tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                tempCanvas.clipRect(0, 0, mBaseWidth, mBaseHeight);
                bitmap = tempBitmap;
                break;

            case PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS:
                if (frameIndex > 1) {
                    PngChunkFCTL tempPngChunk;
                    for (int i = frameIndex - 2; i >= 0; i--) {
                        tempPngChunk = mFctlArrayList.get(i);
                        int tempDisposeOp = tempPngChunk.getDisposeOp();
                        int tempOffsetX = tempPngChunk.getxOff();
                        int tempOffsetY = tempPngChunk.getyOff();
                        frameBitmap = getRawBitmap(i);
                        if (tempDisposeOp != PngChunkFCTL.APNG_DISPOSE_OP_PREVIOUS) {
                            if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_NONE) {
                                bitmap = getCacheResultBitmap(i);
                            } else if (tempDisposeOp == PngChunkFCTL.APNG_DISPOSE_OP_BACKGROUND) {
                                tempBitmap = Bitmap.createBitmap(mBaseWidth, mBaseHeight, Bitmap.Config.ARGB_8888);
                                tempCanvas = new Canvas(tempBitmap);
                                tempCanvas.drawBitmap(getCacheResultBitmap(i), 0, 0, null);
                                tempCanvas.clipRect(tempOffsetX, tempOffsetY, tempOffsetX + frameBitmap.getWidth(), tempOffsetY + frameBitmap.getHeight());
                                tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                tempCanvas.clipRect(0, 0, mBaseWidth, mBaseHeight);
                                bitmap = tempBitmap;
                            }
                            break;
                        }
                    }
                }
                break;
        }
        return bitmap;
    }

    private Bitmap handleBlendingOperation(
            int offsetX, int offsetY, byte blendOp,
            Bitmap frameBitmap, Bitmap baseBitmap) {
        Bitmap redrawnBitmap = Bitmap.createBitmap(mBaseWidth, mBaseHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(redrawnBitmap);
        if (baseBitmap != null) {
            canvas.drawBitmap(baseBitmap, 0, 0, null);
            if (blendOp == PngChunkFCTL.APNG_BLEND_OP_SOURCE) {
                canvas.clipRect(offsetX, offsetY, offsetX + frameBitmap.getWidth(), offsetY + frameBitmap.getHeight());
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.clipRect(0, 0, mBaseWidth, mBaseHeight);
            }
        }
        canvas.drawBitmap(frameBitmap, offsetX, offsetY, null);
        return redrawnBitmap;
    }

    /**
     * 处理过的帧
     */
    private Map<String, Bitmap> mResultbitmap = new HashMap<>();

    /**
     * 缓存结果帧
     */
    private void cacheResultBitmap(int frameIndex, Bitmap bitmap) {
        if (bitmap == null) return;
        mResultbitmap.put(getKey(frameIndex), bitmap);
    }

    private Bitmap getCacheResultBitmap(int frameIndex) {
        return mResultbitmap.get(getKey(frameIndex));
    }

    /**
     * @return 处理过的有序帧
     */
    List<Bitmap> getResultBitmaps() {
        List<Bitmap> result = new ArrayList<>(mResultbitmap.size());
        for (int i = 0; i < mResultbitmap.size(); i++) {
            Bitmap bitmap = getCacheResultBitmap(i);
            result.add(bitmap);
        }
        return result;
    }

    /**
     * @return 播放次数
     */
    int getNumPlays() {
        return mNumPlays;
    }

    /**
     * @return 帧控制数据块
     */
    ArrayList<PngChunkFCTL> getFctlList() {
        return mFctlArrayList;
    }

    /**
     * 原始的bitmap帧
     */
    private Map<String, Bitmap> mRawBitmap = new HashMap<>();

    /**
     * 缓存原始帧
     */
    void cacheRawBitmap(int frameIndex, Bitmap bitmap) {
        if (bitmap == null) return;
        mRawBitmap.put(getKey(frameIndex), bitmap);
    }

    private Bitmap getRawBitmap(int frameIndex) {
        return mRawBitmap.get(getKey(frameIndex));
    }

    private String getKey(int frameIndex) {
        return "index" + frameIndex;
    }
}
