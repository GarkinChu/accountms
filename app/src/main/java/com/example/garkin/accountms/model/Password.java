package com.example.garkin.accountms.model;

import java.io.Serializable;

/**
 * 密码实体类
 * Created by Garkin on 2017/8/16.
 */

public class Password implements Serializable{
    private long id; //编号
    private String password;  //密码

    public Password() {
    }

    public Password(long id, String password) {
        this.id = id;
        this.password = password;
    }

    public Password(String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Password{" +
                "id=" + id +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Password password1 = (Password) o;
        return id == password1.id && password != null ? password.equals(password1.password) : password1.password == null;
    }
}
