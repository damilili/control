package com.hoody.wificontrol;

public class Key extends KeboardItem {
    public Key(int id, String name, byte preCode, byte userCode, byte dataCode) {
        this.id = id;
        this.name = name;
        this.preCode = preCode;
        this.userCode = userCode;
        this.dataCode = dataCode;
    }

    public Key() {
    }

    private int id;
    private String name;
    private byte preCode;
    private byte userCode;
    private byte dataCode;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte getPreCode() {
        return preCode;
    }

    public byte getUserCode() {
        return userCode;
    }

    public byte getDataCode() {
        return dataCode;
    }
}
