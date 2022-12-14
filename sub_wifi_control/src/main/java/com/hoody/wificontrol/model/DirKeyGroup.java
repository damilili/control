package com.hoody.wificontrol.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoody.model.wificontrol.SingleKey;
import com.hoody.wificontrol.R;

import java.util.ArrayList;
import java.util.List;

public class DirKeyGroup extends KeyGroup {

    private GridLayoutManager mLayoutManager;

    public DirKeyGroup() {
        spanSize = 12;
    }

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

    public RecyclerView.Adapter getGroupAdapter() {
        return mGroupAdapter;
    }
//#44 99 ??????
//#44 83 ??????
//#44 53 ??????
//#44 4b ??????
//#44 a9 ??????
    private RecyclerView.Adapter mGroupAdapter = new RecyclerView.Adapter() {
        public SingleKey[] mSingleKeys = new SingleKey[]{
                new SingleKey(6, "??????", ""),
                new SingleKey(1, "???", "000101010100010001010011"),
                new SingleKey(7, "??????", ""),
//                00000000 01000100 10011001
                new SingleKey(3, "???", "000101010100010010011001"),
                new SingleKey(5, "??????", ""),
                new SingleKey(4, "???", "000101010100010010000011"),
                new SingleKey(8, "??????", "000101010100010010101001"),
                new SingleKey(2, "???", "000101010100010001001011"),
                new SingleKey(8, "??????", ""),
        };

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {
                return new KeyHolder(View.inflate(parent.getContext(), R.layout.item_key, null));
            }
            return new KeyBlankHolder(View.inflate(parent.getContext(), R.layout.item_key_blank, null));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((KeyHolder) holder).key.setText(mSingleKeys[position].getName());
            ((KeyHolder) holder).key.setTag(mSingleKeys[position]);
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public int getItemCount() {
            return 9;
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

    static class KeyBlankHolder extends RecyclerView.ViewHolder {
        public KeyBlankHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
