package com.example.garkin.accountms.dao;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.garkin.accountms.model.Payment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Garkin on 2017/8/17.
 */

public class PaymentDao {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static PaymentDao paymentDao;
    private static ContentResolver cr;

    //构造方法，传入ContentResolver需要应用Content参数
    private PaymentDao(Context context) {
        cr = context.getContentResolver();
    }

    //单实例模式，因此PaymentDao(Context context)构造方法设置为private
    public static PaymentDao getPaymentDaoInstance(Context context) {
        if (paymentDao == null) paymentDao = new PaymentDao(context);
        return paymentDao;
    }

    /**
     * 根据支出编号读取支出信息
     *
     * @param id 支出编号
     * @return 支出信息
     */
    public Payment read(long id) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, null,
                AccountContentProvider.COLUMN_PAYMENT_ID + "=" + id, null, null);
        Payment entity = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                entity = new Payment(
                        // id
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)) ? 0
                                : cursor.getInt(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)),
                        // amount
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)) ? 0
                                : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)),
                        // time
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME)) ? null
                                : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME))),
                        // type
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)) ? null
                                : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)),
                        // 4: address
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)) ? null
                                : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)),
                        // 5: comment
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT)) ? null
                                : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT))
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (cursor != null) cursor.close();
        return entity;
    }

    /**
     * 写支出信息，先判别该支出ID是否存在，存在则更新该支出信息。
     *
     * @param payment 支出信息
     * @return int 返回更新的支出信息记录数
     */
    public int update(Payment payment) {
        int count = 0;
        ContentValues values = new ContentValues();
        values.put(AccountContentProvider.COLUMN_PAYMENT_ID, payment.getId());
        values.put(AccountContentProvider.COLUMN_PAYMENT_AMOUNT, payment.getAmount());
        values.put(AccountContentProvider.COLUMN_PAYMENT_TIME, dateFormat.format(payment.getTime()));
        values.put(AccountContentProvider.COLUMN_PAYMENT_TYPE, payment.getType());
        values.put(AccountContentProvider.COLUMN_PAYMENT_ADDRESS, payment.getAddress());
        values.put(AccountContentProvider.COLUMN_PAYMENT_COMMENT, payment.getComment());
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, null,
                AccountContentProvider.COLUMN_PAYMENT_ID + "=" + payment.getId(), null, null);
        if (cursor != null && cursor.moveToFirst())
            count = cr.update(AccountContentProvider.CONTENT_PAYMENT_URI, values, AccountContentProvider.COLUMN_PAYMENT_ID
                    + "=" + payment.getId(), null);
        if (cursor != null) cursor.close();
        return count;
    }

    /**
     * 新增支出信息
     *
     * @param payment 支出信息
     * @return 新增支出信息的Uri
     */
    public Uri add(Payment payment) {
        //数据库_id字段设置为自动递增，因此，新增记录不需要赋_id值
        ContentValues values = new ContentValues();
        values.put(AccountContentProvider.COLUMN_PAYMENT_AMOUNT, payment.getAmount());
        values.put(AccountContentProvider.COLUMN_PAYMENT_TIME, dateFormat.format(payment.getTime()));
        values.put(AccountContentProvider.COLUMN_PAYMENT_TYPE, payment.getType());
        values.put(AccountContentProvider.COLUMN_PAYMENT_ADDRESS, payment.getAddress());
        values.put(AccountContentProvider.COLUMN_PAYMENT_COMMENT, payment.getComment());

        return cr.insert(AccountContentProvider.CONTENT_PAYMENT_URI, values);
    }

    /**
     * 删除指定编号的支出信息
     *
     * @param id 支出编号
     * @return int 删除支出信息的记录数
     */
    public int delete(long id) {
        return cr.delete(ContentUris.withAppendedId(AccountContentProvider.CONTENT_PAYMENT_URI, id), null, null);
    }

    /**
     * 删除指定的一系列支出编号的记录
     *
     * @param ids 指定的一系列支出编号
     * @return 删除支出信息的记录数
     */
    public int delete(long... ids) {
        int count = 0;
        for (long id : ids)
            count += delete(id);
        return count;
    }

    /**
     * 获取支出信息总记录数
     *
     * @return int 支出信息总记录数
     */
    public int getCount() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, new String[]{"count("
                + AccountContentProvider.COLUMN_PAYMENT_ID + ")"}, null, null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        if (cursor != null) cursor.close();
        return count;
    }

    /**
     * 获取支出信息最大的编号
     *
     * @return int 支出信息最大的编号
     */
    public int getMaxId() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, new String[]{"max("
                + AccountContentProvider.COLUMN_PAYMENT_ID + ")"}, null, null, null);
        int maxId = 0;
        if (cursor != null) {
            if (cursor.moveToFirst())
                maxId = cursor.getInt(0);
            cursor.close();
        }
        return maxId;
    }

    /**
     * 获取所有支出信息列表
     *
     * @return List<Payment>
     */
    public List<Payment> getPaymentList() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, null, null, null, null);
        List<Payment> paymentList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Payment payment = null;
                try {
                    payment = new Payment(
                            // id
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)) ? 0
                                    : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)),
                            // amount
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)) ? 0
                                    : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)),
                            // time
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME)) ? null
                                    : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME))),
                            // type
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)),
                            // 4: address
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)),
                            // 5: comment
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                paymentList.add(payment);
            }
            cursor.close();
        }
        return paymentList;
    }

    /**
     * 获取指定日期范围内所有支出信息记录
     *
     * @param startDate 起始日期
     * @param endDate   终止日期
     * @return List<Payment>
     */
    public List<Payment> getPaymentList(Date startDate, Date endDate) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, null,
                AccountContentProvider.COLUMN_PAYMENT_TIME + ">=" + "'" + dateFormat.format(startDate)
                        + "' and " + AccountContentProvider.COLUMN_PAYMENT_TIME + "<=" + "'"
                        + dateFormat.format(endDate) + "'", null, null);
        List<Payment> paymentList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Payment payment = null;
                try {
                    payment = new Payment(
                            // id
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)) ? 0
                                    : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)),
                            // amount
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)) ? 0
                                    : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)),
                            // time

                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME)) ? null
                                    : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME))),
                            // type
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)),
                            // 4: address
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)),
                            // 5: comment
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                paymentList.add(payment);
            }
            cursor.close();
        }
        return paymentList;
    }

    /**
     * 获取指定日期所有支出信息记录
     *
     * @param date 指定日期
     * @return List<Payment>
     */
    public List<Payment> getPaymentList(Date date) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, null,
                AccountContentProvider.COLUMN_PAYMENT_TIME + "=" + "'" + dateFormat.format(date)
                        + "' ", null, null);
        List<Payment> paymentList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Payment payment = null;
                try {
                    payment = new Payment(
                            // id
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)) ? 0
                                    : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)),
                            // amount
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)) ? 0
                                    : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)),
                            // time

                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME)) ? null
                                    : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME))),
                            // type
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)),
                            // 4: payer
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)),
                            // 5: comment
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                paymentList.add(payment);
            }
            cursor.close();
        }
        return paymentList;
    }

    /**
     * 从指定索引处开始，获取指定数量的支出信息记录
     *
     * @param start 指定指定索引
     * @param count 指定数量,必须大于0
     * @return List<Payment> 支出信息记录集合
     */
    public List<Payment> getPaymentList(int start, int count) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI, null, null, null, AccountContentProvider.COLUMN_PAYMENT_TIME + " DESC");
        List<Payment> paymentList = new ArrayList<>();
        if (cursor != null && count > 0) {
            if (cursor.move(start)) {
                do {
                    Payment payment = null;
                    try {
                        payment = new Payment(
                                // id
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)) ? 0
                                        : cursor.getInt(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ID)),
                                // amount
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)) ? 0
                                        : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_AMOUNT)),
                                // time
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME)) ? null
                                        : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TIME))),
                                // type
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)) ? null
                                        : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_TYPE)),
                                // 4: address
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)) ? null
                                        : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_ADDRESS)),
                                // 5: comment
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT)) ? null
                                        : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PAYMENT_COMMENT))
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    paymentList.add(payment);
                } while (cursor.moveToNext() && count-- > 1);
            }
            cursor.close();
        }
        return paymentList;
    }

    /**
     * 获取指定日期范围内所有支出金额
     *
     * @param startDate 起始日期
     * @param endDate   终止日期
     * @return float 总金额
     */
    public float getSumAmount(Date startDate, Date endDate) {
        float amount = 0;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_PAYMENT_AMOUNT + ")"},
                AccountContentProvider.COLUMN_PAYMENT_TIME + ">=" + "'" + dateFormat.format(startDate)
                        + "' and " + AccountContentProvider.COLUMN_PAYMENT_TIME + "<=" + "'"
                        + dateFormat.format(endDate) + "'", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取指定年月所有月支出总金额
     *
     * @param year  年
     * @param month 月
     * @return float 月支出总金额
     */
    public float getSumMonthAmount(int year, int month) {
        float amount = 0;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_PAYMENT_AMOUNT + ")"},
                "strftime('%Y%m'," + AccountContentProvider.COLUMN_PAYMENT_TIME + ")='" + (year * 100 + month)
                        + "'", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取指定日期所属月支出总金额
     *
     * @param date 指定日期
     * @return double 月支出总金额
     */
    public float getSumMonthAmount(Date date) {
        float amount = 0;
        if (date == null) return 0;
        String dateString = dateFormat.format(date);
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_PAYMENT_AMOUNT + ")"},
                "strftime('%Y'," + AccountContentProvider.COLUMN_PAYMENT_TIME + ")=" + "strftime('%Y','" + dateString
                        + "') and " + "strftime('%m'," + AccountContentProvider.COLUMN_PAYMENT_TIME + ")="
                        + "strftime('%m','" + dateString + "')", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取所有支出总金额
     *
     * @return double 总金额
     */
    public float getSumAmount() {
        float amount = 0;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_PAYMENT_AMOUNT + ")"},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取最大的支出日期
     *
     * @return 最大的支出日期
     */
    @Nullable
    public Date getMaxDate() {
        Date date = null;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI,
                new String[]{"max(" + AccountContentProvider.COLUMN_PAYMENT_TIME + ")"},
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                try {
                    date = dateFormat.parse(cursor.getString(0));
                } catch (ParseException e) {
                    date = null;
                }
            cursor.close();
        }
        return date;
    }

    /**
     * 获取最小的支出日期
     *
     * @return 最小的支出日期
     */
    @Nullable
    public Date getMinDate() {
        Date date = null;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PAYMENT_URI,
                new String[]{"min(" + AccountContentProvider.COLUMN_PAYMENT_TIME + ")"},
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                try {
                    date = dateFormat.parse(cursor.getString(0));
                } catch (ParseException e) {
                    date = null;
                }
            cursor.close();
        }
        return date;
    }
}
