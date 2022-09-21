package com.hoody.annotation.listshower;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class CommonListAdapter extends RecyclerView.Adapter<BaseRecyclerShower<?>> {
    private final static int TAGID = 0x9090909;

    /**
     * Bean 数据对象
     * String showerName
     */
    public ArrayList<Pair<IShowerData, String>> mConverterList = new ArrayList<>();
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                Tag tag = (Tag) v.getTag(TAGID);
                mOnItemClickListener.onItemClick((RecyclerView) v.getParent(), v, tag.pos, tag.data);
            }
        }
    };
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    @Override
    public BaseRecyclerShower<?> onCreateViewHolder(ViewGroup parent, int viewType) {
        Class<? extends BaseRecyclerShower> showerClass = ListShowerProfile.getInstance().findShowerById(viewType);
        if (showerClass == null) {
            return null;
        }
        try {
            Constructor<? extends BaseRecyclerShower> constructor = showerClass.getConstructor(Context.class);
            BaseRecyclerShower viewHolder = constructor.newInstance(parent.getContext());
            viewHolder.itemView.setOnClickListener(mOnClickListener);
            return viewHolder;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return new DefaultShower(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(BaseRecyclerShower holder, @SuppressWarnings("RecyclerView") int position) {
        Tag tag = (Tag) holder.itemView.getTag(TAGID);
        if (tag == null) {
            tag = new Tag();
        }
        tag.data = mConverterList.get(position).first;
        tag.pos = position;
        tag.itemView = holder.itemView;
        holder.itemView.setTag(TAGID, tag);
        ((BaseRecyclerShower<?>) holder).bindData(mConverterList, position);
    }

    @Override
    public int getItemViewType(int position) {
        return ListShowerProfile.getInstance().getShowerId(mConverterList.get(position).second);
    }

    @Override
    public int getItemCount() {
        return mConverterList.size();
    }

    public Pair<IShowerData, String> getItem(int pos) {
        return mConverterList.size() == 0 ? null : mConverterList.get(pos);
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView parent, View view, int position, IShowerData showerData);
    }

    static class Tag {
        int pos;
        IShowerData data;
        View itemView;
    }

}
