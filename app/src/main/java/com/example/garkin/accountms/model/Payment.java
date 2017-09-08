package com.example.garkin.accountms.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 支出信息实体类
 * Created by Garkin on 2017/8/16.
 */

public class Payment implements Serializable{
    private long id; //支出编号
    private double amount; //支出金额
    private Date time;  //支出时间
    private String type;  //支出类别
    private String address; //支出地点
    private String comment; //支出备注

    public Payment() {
    }

    public Payment(long id, double amount, Date time, String type, String address, String comment) {
        this.id = id;
        this.amount = amount;
        this.time = time;
        this.type = type;
        this.address = address;
        this.comment = comment;
    }

    public long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getComment() {
        return comment;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", amount=" + amount +
                ", time=" + time +
                ", type='" + type + '\'' +
                ", address='" + address + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id == payment.id && payment.amount == amount && time.equals(payment.time)
                && type.equals(payment.type) && address.equals(payment.address)
                && comment != null ? comment.equals(payment.comment) : payment.comment == null;
    }
}
