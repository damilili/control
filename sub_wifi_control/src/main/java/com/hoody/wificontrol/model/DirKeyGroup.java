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

    private RecyclerView.Adapter mGroupAdapter = new RecyclerView.Adapter() {
        public Key[] mKeys = new Key[]{
                new Key(1, "上", (byte) 49, (byte) 0x44, (byte) 0x53),
                new Key(3, "左", (byte) 49, (byte) 0x44, (byte) 0x99),
                new Key(5, "确定", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(4, "右", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(2, "下", (byte) 49, (byte) 0x44, (byte) 0x4b)
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
            if (holder instanceof KeyHolder) {
                switch (position) {
                    case 1:
                        ((KeyHolder) holder).key.setText(mKeys[0].getName());
                        break;
                    case 3:
                        ((KeyHolder) holder).key.setText(mKeys[1].getName());
                        break;
                    case 4:
                        ((KeyHolder) holder).key.setText(mKeys[2].getName());
                        break;
                    case 5:
                        ((KeyHolder) holder).key.setText(mKeys[3].getName());
                        break;
                    case 7:
                        ((KeyHolder) holder).key.setText(mKeys[4].getName());
                        break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == 2 ||
                    position == 6 || position == 8) {
                return 0;
            }
            return 1;
        }

        @Override
        public int getItemCount() {
            return 9;
        }
    };

    static class KeyHolder extends RecyclerView.ViewHolder {
        private final TextView key;

        public KeyHolder(@NonNull View itemView) {
            super(itemView);
            key = ((TextView) itemView.findViewById(R.id.item_key));
        }
    }

    static class KeyBlankHolder extends RecyclerView.ViewHolder {
        public KeyBlankHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
