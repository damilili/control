package com.hoody.reader.pdf.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.reader.pdf.R;

public class PageHolder extends RecyclerView.ViewHolder {

    private final ImageView iv_pdf_show;

    public PageHolder(@NonNull View itemView) {
        super(itemView);
        iv_pdf_show = ((ImageView) itemView.findViewById(R.id.iv_pdf_show));
        iv_pdf_show.getLayoutParams().width = DeviceInfo.WIDTH;
        iv_pdf_show.getLayoutParams().height = DeviceInfo.HEIGHT;
    }

    public void showPage(Bitmap bitmap) {
        if (bitmap != null) {
            iv_pdf_show.getLayoutParams().height = DeviceInfo.WIDTH * bitmap.getHeight() / bitmap.getWidth();
            iv_pdf_show.getLayoutParams().width = DeviceInfo.WIDTH;
            iv_pdf_show.setBackgroundColor(Color.WHITE);
        }
        iv_pdf_show.setImageBitmap(bitmap);

    }
}
