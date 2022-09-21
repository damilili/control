package com.hoody.wificontrol;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
            return new KeyHolder(View.inflate(parent.getContext(), R.layout.item_key, null));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof KeyHolder) {
                ((KeyHolder) holder).key.setText(mKeys[position].getName());
            }
        }

        @Override
        public int getItemCount() {
            return mKeys.length;
        }
    };

    static class KeyHolder extends RecyclerView.ViewHolder {
        private final TextView key;

        public KeyHolder(@NonNull View itemView) {
            super(itemView);
            key = ((TextView) itemView.findViewById(R.id.item_key));
        }
    }
}
