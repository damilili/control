package com.hoody.user;

/**
 * Created by cdm on 2021/11/12.
 */
public class User {
    public int id;
    public int age;
    public String name;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
