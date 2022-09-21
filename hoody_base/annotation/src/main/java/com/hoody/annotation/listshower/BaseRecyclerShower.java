package com.hoody.annotation.listshower;

import android.content.Context;
import android.util.Pair;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public abstract class BaseRecyclerShower<T extends IShowerData> extends RecyclerView.ViewHolder {

    protected final Context context;
    protected T data;

    public BaseRecyclerShower(Context context, int layoutId) {
        super(View.inflate(context, layoutId, null));
        this.context = context;
    }

    public BaseRecyclerShower(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
    }

    public abstract void bindData(T bean);

    public void bindData(ArrayList<Pair<IShowerData, String>> mConverterList, int position) {
        data = (T) mConverterList.get(position).first;
        bindData(data);
    }
}
