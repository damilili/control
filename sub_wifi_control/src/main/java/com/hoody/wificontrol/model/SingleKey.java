package com.hoody.wificontrol.model;

import android.graphics.drawable.Drawable;

public class SingleKey extends KeyboardItem {
    public enum DrawablePos {
        top,
        bottom,
        right,
        left
    }

    public SingleKey(int id, String name, String dataCode) {
        this.id = id;
        this.name = name;
        this.data = dataCode;
    }

    public SingleKey() {
    }

    private int id;
    private String name;
    private String data;
    private String drawable;
    private Drawable background;
    private DrawablePos drawablePos;

    public void setName(String name) {
        this.name = name;
    }

    public String getDrawable() {
        return drawable;
    }

    public void setDrawable(String drawable) {
        this.drawable = drawable;
    }

    public Drawable getBackground() {
        return background;
    }

    public void setBackground(Drawable background) {
        this.background = background;
    }

    public DrawablePos getDrawablePos() {
        return drawablePos;
    }

    public void setDrawablePos(DrawablePos drawablePos) {
        this.drawablePos = drawablePos;
    }



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDataCode() {
        return data;
    }
}
