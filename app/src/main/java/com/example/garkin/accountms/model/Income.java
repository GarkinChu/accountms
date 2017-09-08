package com.example.garkin.accountms.model;

import java.util.Date;

/**
 * 收入信息实体类
 * Created by Garkin on 2017/8/16.
 */

public class Income {
    private long id;  //收入编号
    private double amount;  //收入金额
    private Date time;  //收入时间
    private String type;  //收入类别
    private String payer;  //付款方
    private String comment;  //收入备注

    /**
     * 默认构造方法
     */
    public Income(){
    }

    public Income(long id, double amount, Date time, String type, String payer, String comment) {
        this.id = id;
        this.amount = amount;
        this.time = time;
        this.type = type;
        this.payer = payer;
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

    public String getPayer() {
        return payer;
    }

    public String getComment() {
        return comment;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Income{" +
                "id=" + id +
                ", amount=" + amount +
                ", time=" + time +
                ", type='" + type + '\'' +
                ", payer='" + payer + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Income income = (Income) o;
        return id == income.id && income.amount == amount && time.equals(income.time)
                && type.equals(income.type) && payer.equals(income.payer)
                && comment != null ? comment.equals(income.comment) : income.comment == null;
    }
}
