package com.hoody.model.wificontrol;

import android.graphics.Color;

import java.io.Serializable;
import java.lang.reflect.Field;

public class SingleKey extends KeyboardItem implements Serializable {
    public static final int MaxTextSize = 35;

    public SingleKey(int id, String name, String dataCode) {
        this.name = name;
        this.data = dataCode;
    }

    public SingleKey(String id, String name, String dataCode) {
        this.id = id;
        this.name = name;
        this.data = dataCode;
    }

    private String id;
    private String name;
    private String data;
    private String drawable;
    private int backgroundColor = Color.GRAY;
    private int width;
    private int height;
    private int posX;
    private int posY;
    private int textColor = Color.WHITE;
    private int textSize = MaxTextSize / 2;

    public void setName(String name) {
        this.name = name;
    }

    public String getDrawable() {
        return drawable;
    }

    public void setDrawable(String drawable) {
        this.drawable = drawable;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
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

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void copy(SingleKey key) {
        try {
            for (Field declaredField : getClass().getDeclaredFields()) {
                declaredField.set(this, declaredField.get(key));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
