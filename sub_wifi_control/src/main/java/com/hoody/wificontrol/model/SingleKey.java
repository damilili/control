package com.hoody.wificontrol.model;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import java.io.Serializable;

public class SingleKey extends KeyboardItem implements Serializable {
    public enum DrawablePos {
        top,
        bottom,
        right,
        left
    }

    public SingleKey(int id, String name, String dataCode) {
        this.name = name;
        this.data = dataCode;
    }

    public SingleKey(String id, String name, String dataCode) {
        this.id = id;
        this.name = name;
        this.data = dataCode;
    }

    public SingleKey() {
    }

    private String id;
    private String name;
    private String data;
    private String drawable;
    private Drawable background;
    private DrawablePos drawablePos;
    private int width;
    private int height;
    private int posX;
    private int posY;
    private int nameColor;

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


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDataCode() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}
