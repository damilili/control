package com.hoody.commonbase.image.apng;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ar.com.hjg.pngj.ChunkReader;
import ar.com.hjg.pngj.ChunkSeqReaderPng;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngHelperInternal;
import ar.com.hjg.pngj.PngReaderApng;
import ar.com.hjg.pngj.PngjException;
import ar.com.hjg.pngj.chunks.ChunkHelper;
import ar.com.hjg.pngj.chunks.ChunkRaw;
import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkACTL;
import ar.com.hjg.pngj.chunks.PngChunkFCTL;
import ar.com.hjg.pngj.chunks.PngChunkFDAT;
import ar.com.hjg.pngj.chunks.PngChunkIDAT;
import ar.com.hjg.pngj.chunks.PngChunkIEND;
import ar.com.hjg.pngj.chunks.PngChunkIHDR;

public class ApngReader extends PngReaderApng {
    private static final String TAG = "ApngReader";
    private static final boolean DEBUG = false;
    private ApngProcessor mApngProcesser;

    ApngReader(InputStream file) {
        super(file);
        mApngProcesser = new ApngProcessor();
    }

    private int mCurReadFrameIndex;
    private ByteArrayOutputStream mOutputStream = null;
    private ImageInfo mFrameInfo;
    private int mFrameIndex = -1;

    @Override
    protected ChunkSeqReaderPng createChunkSeqReader() {
        return new ChunkSeqReaderPng(false) {
            @Override
            public boolean shouldSkipContent(int len, String id) {
                return false;
            }

            @Override
            protected boolean isIdatKind(String id) {
                return false;
            }

            @Override
            protected void postProcessChunk(ChunkReader chunkR) {
                super.postProcessChunk(chunkR);
                try {
                    if (DEBUG)
                        Log.d(TAG, "postProcessChunk() called with: chunkR = [" + chunkR + "]");
                    String id = chunkR.getChunkRaw().id;
                    PngChunk lastChunk = chunksList.getChunks().get(chunksList.getChunks().size() - 1);
                    if (id.equals(PngChunkFCTL.ID)) {
                        mFrameIndex++;
                        mFrameInfo = ((PngChunkFCTL) lastChunk).getEquivImageInfo();
                        startNewFile();
                    }
                    if (id.equals(PngChunkFDAT.ID) || id.equals(PngChunkIDAT.ID)) {
                        if (id.equals(PngChunkIDAT.ID)) {
                            // copy IDAT as is (only if file is open == if FCTL previous == if IDAT is part of the animation
                            if (mOutputStream != null)
                                chunkR.getChunkRaw().writeChunk(mOutputStream);
                        } else {
                            // copy fDAT as IDAT, trimming the first 4 bytes
                            ChunkRaw crawi =
                                    new ChunkRaw(chunkR.getChunkRaw().len - 4, ChunkHelper.b_IDAT, true);
                            System.arraycopy(chunkR.getChunkRaw().data, 4, crawi.data, 0, crawi.data.length);
                            crawi.writeChunk(mOutputStream);
                        }
                        chunkR.getChunkRaw().data = null; // be kind, release memory
                    }
                    if (id.equals(PngChunkIEND.ID)) {
                        if (mOutputStream != null) {
                            endFile();
                        }
                        //开始加工解析出来的bitmap
                        mApngProcesser.process(getChunksList().getChunks());
                    }
                } catch (Exception e) {
                    throw new PngjException(e);
                }
            }
        };
    }


    private void startNewFile() throws Exception {
        if (mOutputStream != null) endFile();
        mCurReadFrameIndex = mFrameIndex;
        mOutputStream = new ByteArrayOutputStream();
        mOutputStream.write(PngHelperInternal.getPngIdSignature());
        PngChunkIHDR ihdr = new PngChunkIHDR(mFrameInfo);
        ihdr.createRawChunk().writeChunk(mOutputStream);
        for (PngChunk chunk : getChunksList(false).getChunks()) {
            String id = chunk.id;
            if (id.equals(PngChunkIHDR.ID) || id.equals(PngChunkFCTL.ID) || id.equals(PngChunkACTL.ID)) {
                continue;
            }
            if (id.equals(PngChunkIDAT.ID)) {
                break;
            }
            chunk.getRaw().writeChunk(mOutputStream);
        }
    }

    private void endFile() throws IOException {
        new PngChunkIEND(null).createRawChunk().writeChunk(mOutputStream);
        byte[] data = mOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        mApngProcesser.cacheRawBitmap(mCurReadFrameIndex, bitmap);
        mOutputStream.close();
        mOutputStream = null;
    }

    /**
     * @return 序列帧
     */
    int getNumPlays() {
        return mApngProcesser.getNumPlays();
    }

    /**
     * @return 序列帧
     */
    List<Bitmap> getResultBitmaps() {
        return mApngProcesser.getResultBitmaps();
    }

    /**
     * @return 帧控制数据块
     */
    ArrayList<PngChunkFCTL> getFctlList() {
        return mApngProcesser.getFctlList();
    }
}

