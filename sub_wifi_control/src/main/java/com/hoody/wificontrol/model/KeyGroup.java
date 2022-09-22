package com.hoody.wificontrol.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoody.wificontrol.R;

public class KeyGroup extends KeboardItem {

    private GridLayoutManager mLayoutManager;

    public KeyGroup() {
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

    public RecyclerView.Adapter getGroupAdapter() {
        return mGroupAdapter;
    }

    private RecyclerView.Adapter mGroupAdapter = new RecyclerView.Adapter() {
        public Key[] mKeys = new Key[]{
                new Key(1, "上", (byte) 49, (byte) 0x44, (byte) 0x53),
                new Key(2, "下", (byte) 49, (byte) 0x44, (byte) 0x4b),
                new Key(3, "左", (byte) 49, (byte) 0x44, (byte) 0x99),
                new Key(4, "右", (byte) 49, (byte) 0x44, (byte) 0x83)
        };

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {
                return new KeyHolder(View.inflate(parent.getContext(), R.layout.item_key, null));
            }
            return new KeyBlankHolder(View.inflate(parent.getContext(), R.layout.item_key, null));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof KeyHolder) {
                ((KeyHolder) holder).key.setText(mKeys[position / 2].getName());
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
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
