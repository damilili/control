package com.hoody.wificontrol.model;

import android.content.Context;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class KeyGroup extends KeyboardItem {
    public abstract GridLayoutManager getLayoutManager(Context context);

    public abstract List<DividerItemDecoration> getDividerItemDecorations(Context context);

    public abstract RecyclerView.Adapter getGroupAdapter();
}
