package com.hoody.wificontrol.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoody.wificontrol.R;

import java.util.ArrayList;
import java.util.List;

public class KeyNumGroup extends KeyGroup {

    private GridLayoutManager mLayoutManager;

    public KeyNumGroup() {
        spanSize = 12;
    }

    @Override
    public GridLayoutManager getLayoutManager(Context context) {
        mLayoutManager = new GridLayoutManager(context, 12);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 4;
            }
        });
        return mLayoutManager;
    }

    @Override
    public List<DividerItemDecoration> getDividerItemDecorations(Context context) {
        DividerItemDecoration decorH = new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 5);
            }
        };
        decorH.setDrawable(new ColorDrawable(Color.TRANSPARENT));
        DividerItemDecoration decorV = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(2, 0, 2, 0);
            }
        };
        decorV.setDrawable(new ColorDrawable(Color.TRANSPARENT));
        ArrayList<DividerItemDecoration> dividerItemDecorations = new ArrayList<>();
        dividerItemDecorations.add(decorV);
        dividerItemDecorations.add(decorH);
        return dividerItemDecorations;
    }

    @Override
    public RecyclerView.Adapter getGroupAdapter() {
        return mGroupAdapter;
    }

    private RecyclerView.Adapter mGroupAdapter = new RecyclerView.Adapter() {
        public SingleKey[] mSingleKeys = new SingleKey[]{
                new SingleKey(1, "1", ""),
                new SingleKey(2, "2", ""),
                new SingleKey(3, "3", ""),
                new SingleKey(4, "4", ""),
                new SingleKey(5, "5", ""),
                new SingleKey(6, "6", ""),
                new SingleKey(7, "7", ""),
                new SingleKey(8, "8", ""),
                new SingleKey(9, "9", ""),
                new SingleKey(10, "#", ""),
                new SingleKey(11, "0", ""),
                new SingleKey(11, "*", "")
        };

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new KeyHolder(View.inflate(parent.getContext(), R.layout.item_key_num, null));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof KeyHolder) {
                ((KeyHolder) holder).key.setTag(mSingleKeys[position]);
                ((KeyHolder) holder).key.setText(mSingleKeys[position].getName());
                ((KeyHolder) holder).key.setEnabled(!TextUtils.isEmpty(mSingleKeys[position].getDataCode()));
            }
        }

        @Override
        public int getItemCount() {
            return mSingleKeys.length;
        }
    };

    class KeyHolder extends RecyclerView.ViewHolder {
        private final TextView key;

        public KeyHolder(@NonNull View itemView) {
            super(itemView);
            key = ((TextView) itemView.findViewById(R.id.item_key));
            key.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mOnKeyClickListener != null) {
                        mOnKeyClickListener.OnClick(v, ((SingleKey) v.getTag()));
                    }
                }
            });
            key.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnKeyClickListener != null) {
                        mOnKeyClickListener.OnStudy(v, ((SingleKey) v.getTag()));
                    }
                    return true;
                }
            });
        }
    }
}
