package com.hoody.reader.pdf.view;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoody.annotation.router.Router;
import com.hoody.commonbase.util.SharedPreferenceUtil;
import com.hoody.commonbase.util.ToastUtil;
import com.hoody.commonbase.view.fragment.SwipeBackFragment;
import com.hoody.reader.pdf.R;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Router("reader/pdf")
public class PdfReadFragemt extends SwipeBackFragment {
    private static final String TAG = "PdfReadFragemt";
    private static final String TAG_CURRENT_PAGE_NUM = "TAG_CURRENT_PAGE_NUM";
    private RecyclerView rv_pager;
    private PdfRenderer pdfRenderer;
    private TextView tv_page_num;

    private ConcurrentLinkedQueue<Integer> mPageQueue = new ConcurrentLinkedQueue<Integer>();
    private HashMap<Integer, Bitmap> mPos2Bitmap = new HashMap<Integer, Bitmap>(3);
    private Thread mPagerLoadThread;
    private Runnable mLoadTask = new Runnable() {
        @Override
        public void run() {
            while (mPageQueue.size() > 0) {
                Integer pos = mPageQueue.poll();
                PdfRenderer.Page page = pdfRenderer.openPage(pos);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                RecyclerView.LayoutManager layoutManager = rv_pager.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    if (firstVisibleItemPosition <= pos && pos <= lastVisibleItemPosition) {
                        Set<Integer> integers = mPos2Bitmap.keySet();
                        Iterator<Integer> iterator = integers.iterator();
                        while (iterator.hasNext()) {
                            Integer next = iterator.next();
                            if (firstVisibleItemPosition > next || next > lastVisibleItemPosition) {
                                iterator.remove();
                            }
                        }
                        mPos2Bitmap.put(pos, bitmap);
                    } else {
                        bitmap.recycle();
                    }
                }
                rv_pager.post(mRefreshTask);
            }
            mPagerLoadThread = null;
        }
    };

    private Runnable mRefreshTask = new Runnable() {
        @Override
        public void run() {
            rv_pager.getAdapter().notifyDataSetChanged();
        }
    };

    @Override
    protected void onClose() {
        int firstVisibleItemPosition = ((LinearLayoutManager) rv_pager.getLayoutManager()).findFirstVisibleItemPosition();
        SharedPreferenceUtil.getInstance().saveSharedPreferences(TAG_CURRENT_PAGE_NUM, firstVisibleItemPosition);
    }

    @Override
    protected View createContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.sub_pdf_fragment_pdf_read, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle arguments = getArguments();
            String pdfFilePath = arguments.getString("PdfFilePath");
            File file = new File(pdfFilePath);
            if (!file.exists()) {
                ToastUtil.showToast(getContext(), "文件不存在");
                return;
            }
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fileDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "文件打开失败");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_page_num = ((TextView) findViewById(R.id.tv_page_num));
        rv_pager = ((RecyclerView) findViewById(R.id.rv_pager));
        rv_pager.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        rv_pager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    tv_page_num.setVisibility(View.INVISIBLE);
                } else {
                    tv_page_num.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int sumCount = recyclerView.getAdapter().getItemCount();
                tv_page_num.setText(firstVisibleItemPosition + "/" + sumCount);
            }
        });
        rv_pager.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_pager.setAdapter(new RecyclerView.Adapter() {


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new PageHolder(View.inflate(parent.getContext(), R.layout.sub_pdf_pdf_page, null));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (!mPos2Bitmap.containsKey(position)) {
                    if (!mPageQueue.contains(position)) {
                        if (mPageQueue.size() > 3) {
                            mPageQueue.poll();
                        }
                        mPageQueue.offer(position);
                        if (mPagerLoadThread == null) {
                            mPagerLoadThread = new Thread(mLoadTask);
                            mPagerLoadThread.start();
                        }
                    }
                }
                ((PageHolder) holder).showPage(mPos2Bitmap.get(position));
            }

            @Override
            public int getItemCount() {
                return pdfRenderer == null ? 0 : pdfRenderer.getPageCount();
            }
        });
        int pageNum = SharedPreferenceUtil.getInstance().readSharedPreferences(TAG_CURRENT_PAGE_NUM, 0);
        rv_pager.scrollToPosition(pageNum);
    }
}
