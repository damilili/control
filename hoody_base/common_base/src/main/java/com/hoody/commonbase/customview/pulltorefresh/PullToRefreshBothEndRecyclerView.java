package com.hoody.commonbase.customview.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


/**
 * 对PullToRefreshRecyclerView进行功能扩展
 *          扩展如下：
 *          1 添加ProgressDialog 显示
 *          2 添加addHeader功能
 *          3 添加 item 的ItemClickListener 、ItemLongClickListener 设置功能
 */
public class PullToRefreshBothEndRecyclerView extends PullToRefreshRecyclerView {
    private boolean mHasFixedSize;
    private LinearLayoutManager mManager;
    private BothEndRecyclerViewAdapter mAdapter;
    private OnLoadMoreListener mOnLoadMoreListener;
    private OnRefreshListener mOnRefreshListener;
    private PullToRefreshBase.OnRefreshListener listener = new PullToRefreshBase.OnRefreshListener() {
        @Override
        public void onRefresh(int curMode) {
            if (curMode == PullToRefreshBase.MODE_PULL_DOWN_TO_REFRESH && mOnRefreshListener != null) {
                mOnRefreshListener.onRefresh();
            }
            if (curMode == PullToRefreshBase.MODE_PULL_UP_TO_REFRESH && mOnLoadMoreListener != null) {
                mOnLoadMoreListener.onLoadMore();
            }
        }
    };

    public PullToRefreshBothEndRecyclerView(Context context) {
        this(context, null);
    }

    public PullToRefreshBothEndRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    private void initViews() {
        setOnRefreshListener(listener);
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        mRecyclerView = super.createRefreshableView(context, attrs);
        mRecyclerView.setHasFixedSize(mHasFixedSize);
        if (mManager != null) {
            mRecyclerView.setLayoutManager(mManager);
        }
        if (mAdapter != null) {
            mRecyclerView.setAdapter(mAdapter);
        }
        return mRecyclerView;
    }

    public void setHasFixedSize(boolean hasFixedSize) {
        this.mHasFixedSize = hasFixedSize;
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(hasFixedSize);
        }
    }

    public void setLayoutManager(LinearLayoutManager manager) {
        if (manager != null) {
            this.mManager = manager;
            if (mRecyclerView != null) {
                mRecyclerView.setLayoutManager(manager);
            }
        }
    }

    public void setAdapter(BothEndRecyclerViewAdapter adapter) {
        this.mAdapter = adapter;
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mAdapter);
        }
        if (this.mAdapter != null) {
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    onRefreshComplete();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    onRefreshComplete();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    onRefreshComplete();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    onRefreshComplete();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    onRefreshComplete();
                }
            });
        }
    }

    public void setRefreshEnabled(boolean flag, OnRefreshListener mRefreshListener) {
        this.mOnRefreshListener = mRefreshListener;
        setOnRefreshListener(listener);
        if (flag) {
            switch (getMode()) {
                case MODE_PULL_UP_TO_REFRESH:
                    setMode(MODE_BOTH);
                    break;
                case MODE_DISABLED:
                    setMode(MODE_PULL_DOWN_TO_REFRESH);
                    break;
            }
        } else {
            switch (getMode()) {
                case MODE_BOTH:
                    setMode(MODE_PULL_UP_TO_REFRESH);
                    break;
                case MODE_PULL_DOWN_TO_REFRESH:
                    setMode(MODE_DISABLED);
                    break;
            }
        }
    }

    public void setLoadMoreEnabled(boolean flag, OnLoadMoreListener mLoadMoreListener) {
        mOnLoadMoreListener = mLoadMoreListener;
        if (flag) {
            switch (getMode()) {
                case MODE_DISABLED:
                    setMode(MODE_PULL_UP_TO_REFRESH);
                    break;
                case MODE_PULL_DOWN_TO_REFRESH:
                    setMode(MODE_BOTH);
                    break;
            }
        } else {
            switch (getMode()) {
                case MODE_PULL_UP_TO_REFRESH:
                    setMode(MODE_DISABLED);
                    break;
                case MODE_BOTH:
                    setMode(MODE_PULL_DOWN_TO_REFRESH);
                    break;
            }
        }
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        if (itemDecoration != null)
            mRecyclerView.addItemDecoration(itemDecoration);
    }

    public void setCustomHeader(View header) {
        if (mAdapter != null) {
            mAdapter.addHeaderView(header);
        }
    }

    public void setCustomFooter(View footer) {
        if (mAdapter != null) {
            mAdapter.addFooterView(footer);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * {@link PullToRefreshBothEndRecyclerView }的适配器
     */
    public static abstract class BothEndRecyclerViewAdapter<VH extends BothEndRecyclerBaseViewHolder> extends RecyclerView.Adapter<VH> {


        // 各种视图类型
        public static final int TYPE_HEADER = 0x100;
        public static final int TYPE_FOOTER = 0x101;
        public static final int TYPE_NORMAL = 0x102;


        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;

        private List<View> headers = new ArrayList<>();
        private List<View> footers = new ArrayList<>();

        private OnClickListener mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && tag instanceof BothEndRecyclerBaseViewHolder) {
                    mItemClickListener.onItemClick(((BothEndRecyclerBaseViewHolder) tag).getAdapterPosition());
                }
            }
        };
        private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Object tag = v.getTag();
                if (tag != null && tag instanceof BothEndRecyclerBaseViewHolder) {
                    return mItemLongClickListener.onItemClick(((BothEndRecyclerBaseViewHolder) tag).getAdapterPosition());
                }
                return false;
            }
        };


        public void addHeaderView(View headerView) {
            if (headerView != null) {
                headers.add(headerView);
            }
            notifyDataSetChanged();
        }

        /**
         * 移除指定顶部视图
         *
         * @param headerView
         */
        public void removeHeaderView(View headerView) {
            if (headerView != null) {
                headers.remove(headerView);
            }
            notifyDataSetChanged();
        }

        public void addFooterView(View footerView) {
            if (footerView != null) {
                footers.add(footerView);
            }
            notifyDataSetChanged();
        }

        /**
         * 移除指定底部视图
         *
         * @param footerView
         */
        public void removeFooterView(View footerView) {
            if (footerView != null) {
                footers.remove(footerView);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (headers.size() != 0) {
                if (position < headers.size()) return TYPE_HEADER;
            }
            if (footers.size() != 0) {
                int i = position - headers.size() - getAdapterItemCount();
                if (i >= 0) {
                    return TYPE_FOOTER;
                }
            }
            return TYPE_NORMAL;
        }

        public boolean isHeader(int position) {
            if (headers.size() > 0) {
                return position < headers.size();
            } else {
                return false;
            }
        }

        public boolean isFooter(int position) {
            if (footers.size() > 0) {
                return position >= (headers.size() + getAdapterItemCount());
            } else {
                return false;
            }
        }

        @Override
        public int getItemCount() {
            return getAdapterItemCount() + (hasHeaderView() ? headers.size() : 0) + (hasFooterView() ? footers.size() : 0);
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER || viewType == TYPE_HEADER) {
                return (VH) new SimpleHolder(new FrameLayout(parent.getContext()));
            } else {
                BothEndRecyclerBaseViewHolder viewHolder = onCreateViewHolder(parent);
                viewHolder.itemView.setTag(viewHolder);
                if (mItemClickListener != null) {
                    viewHolder.itemView.setOnClickListener(mOnClickListener);
                }
                if (mItemLongClickListener != null) {
                    viewHolder.itemView.setOnLongClickListener(mOnLongClickListener);
                }
                return (VH) viewHolder;
            }
        }

        @Override
        public void onBindViewHolder(BothEndRecyclerBaseViewHolder holder, int position) {
            if (isHeader(position)) {
                if (holder.itemView instanceof FrameLayout) {
                    View view = headers.get(position);
                    ViewGroup parentView = (ViewGroup) view.getParent();
                    if (parentView != null) {
                        parentView.removeView(view);
                    }
                    ((FrameLayout) holder.itemView).removeAllViews();
                    ((FrameLayout) holder.itemView).addView(headers.get(position));
                }
            } else if (isFooter(position)) {
                int i = position - headers.size() - getAdapterItemCount();
                if (holder.itemView instanceof FrameLayout) {
                    View view = footers.get(i);
                    ViewGroup parentView = (ViewGroup) view.getParent();
                    if (parentView != null) {
                        parentView.removeView(view);
                    }
                    ((FrameLayout) holder.itemView).removeAllViews();
                    ((FrameLayout) holder.itemView).addView(footers.get(i));
                }
            } else {
                OnBindViewHolder(holder, position - headers.size());
            }
        }


        public boolean hasHeaderView() {
            return headers != null && headers.size() > 0;
        }


        public boolean hasFooterView() {
            return footers != null && footers.size() > 0;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.mItemClickListener = listener;
        }

        public void setOnItemLongClickListener(OnItemLongClickListener listener) {
            this.mItemLongClickListener = listener;
        }

        public abstract void OnBindViewHolder(BothEndRecyclerBaseViewHolder holder, int position);

        public abstract int getViewHolderType(int position);

        public abstract VH onCreateViewHolder(ViewGroup parent);

        public abstract int getAdapterItemCount();


        public static class SimpleHolder extends BothEndRecyclerBaseViewHolder {
            public SimpleHolder(View itemView) {
                super(itemView);
            }
        }
    }

    /**
     * 与{@link BothEndRecyclerViewAdapter }向对应的viewholder
     */
    public static abstract class BothEndRecyclerBaseViewHolder<M> extends RecyclerView.ViewHolder {
        private SparseArray<View> mSpary;

        public BothEndRecyclerBaseViewHolder(View itemView) {
            super(itemView);

        }

        public BothEndRecyclerBaseViewHolder(ViewGroup parent, @LayoutRes int res) {
            super(LayoutInflater.from(parent.getContext()).inflate(res, parent, false));
            mSpary = new SparseArray<>();
        }

        public void setData(M data) {
        }

        public void setData(M data, int position) {

        }

        public void setData(M data, int position, int dataType) {
        }

        protected <T extends View> T findViewById(@IdRes int id) {
            return (T) itemView.findViewById(id);
        }

        protected <T extends View> T getView(@IdRes int id) {
            T t = null;
            ;
            View v = mSpary.get(id);
            if (v == null) {
                t = findViewById(id);
                mSpary.put(id, t);
            } else {
                t = (T) v;
            }
            return t;
        }

        protected Context getContext() {
            return itemView.getContext();
        }

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemClick(int position);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(visibility);
        }
    }
}
