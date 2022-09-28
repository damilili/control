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

import com.hoody.commonbase.util.ToastUtil;
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
        public Key[] mKeys = new Key[]{
                new Key(1, "1", (byte) 49, (byte) 0x44, (byte) 0x53),
                new Key(2, "2", (byte) 49, (byte) 0x44, (byte) 0x4b),
                new Key(3, "3", (byte) 49, (byte) 0x44, (byte) 0x99),
                new Key(4, "4", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(5, "5", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(6, "6", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(7, "7", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(8, "8", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(9, "9", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(10, "#", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(11, "0", (byte) 49, (byte) 0x44, (byte) 0x83),
                new Key(11, "*", (byte) 49, (byte) 0x44, (byte) 0x83)
        };

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new KeyHolder(View.inflate(parent.getContext(), R.layout.item_key_num, null));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof KeyHolder) {
                ((KeyHolder) holder).key.setTag(mKeys[position]);
                ((KeyHolder) holder).key.setText(mKeys[position].getName());
                ((KeyHolder) holder).key.setEnabled(!TextUtils.isEmpty(mKeys[position].getDataCode()));
            }
        }

        @Override
        public int getItemCount() {
            return mKeys.length;
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

                }
            });
            key.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnStudyListener != null) {
                        mOnStudyListener.OnStudy(v, ((Key) v.getTag()));
                    }
                    return true;
                }
            });
        }
    }
}
