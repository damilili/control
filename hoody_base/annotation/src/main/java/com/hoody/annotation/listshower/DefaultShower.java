package com.hoody.annotation.listshower;

import android.view.View;
import android.widget.TextView;

public class DefaultShower<T extends IShowerData> extends BaseRecyclerShower<T> {

    DefaultShower(View itemView) {
        super(itemView);
    }
    @Override
    public void bindData(T showerData) {
        if (itemView instanceof TextView) {
            ((TextView) itemView).setText(showerData.getClass().getSimpleName());
        }
    }
}
