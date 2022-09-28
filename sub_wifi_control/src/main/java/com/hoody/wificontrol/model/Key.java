package com.hoody.wificontrol.model;

public class Key extends KeboardItem {
    public Key(int id, String name, byte preCode, byte userCode, byte dataCode) {
        this.id = id;
        this.name = name;
    }

    public Key() {
    }

    private int id;
    private String name;
    private String data;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDataCode() {
        return "data";
    }
}
