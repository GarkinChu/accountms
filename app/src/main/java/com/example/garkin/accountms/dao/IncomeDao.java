package com.example.garkin.accountms.dao;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.garkin.accountms.model.Income;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Garkin on 2017/8/17.
 */

public class IncomeDao {
    //格式化时间
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static IncomeDao incomeDao;
    private static ContentResolver cr;  //通过ContentResolver访问ContentProvide共享数据

    //构造方法，传入ContentResolver需要应用Content参数，单实例模式，因此IncomeDao(Context context)构造方法设置为private
    private IncomeDao(Context context) {
        cr = context.getContentResolver();
    }

    public static IncomeDao getIncomeDaoInstance(Context context) {
        if (incomeDao == null) incomeDao = new IncomeDao(context);
        return incomeDao;
    }

    /**
     * 根据收入编号读取收入信息
     *
     * @param id 收入编号
     * @return 收入信息
     */
    public Income read(long id) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, null,
                AccountContentProvider.COLUMN_INCOME_ID + "=" + id, null, null);
        Income entity = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                entity = new Income(
                        // id
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)) ? 0
                                : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)),
                        // amount
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)) ? 0
                                : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)),
                        // time
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME)) ? null
                                : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME))),
                        // type
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)) ? null
                                : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)),
                        // 4: payer
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)) ? null
                                : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)),
                        // 5: comment
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT)) ? null
                                : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT))
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (cursor != null) cursor.close();
        return entity;
    }

    /**
     * 写收入信息，先判别该收入ID是否存在，存在则更新该收入信息。
     *
     * @param income 收入信息
     * @return int 返回更新的收入信息记录数
     */
    public int update(Income income) {
        int count = 0;
        ContentValues values = new ContentValues();
        values.put(AccountContentProvider.COLUMN_INCOME_ID, income.getId());
        values.put(AccountContentProvider.COLUMN_INCOME_AMOUNT, income.getAmount());
        values.put(AccountContentProvider.COLUMN_INCOME_TIME, dateFormat.format(income.getTime()));
        values.put(AccountContentProvider.COLUMN_INCOME_TYPE, income.getType());
        values.put(AccountContentProvider.COLUMN_INCOME_PAYER, income.getPayer());
        values.put(AccountContentProvider.COLUMN_INCOME_COMMENT, income.getComment());
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, null,
                AccountContentProvider.COLUMN_INCOME_ID + "=" + income.getId(), null, null);
        if (cursor != null && cursor.moveToFirst())
            count = cr.update(AccountContentProvider.CONTENT_INCOME_URI, values, AccountContentProvider.COLUMN_INCOME_ID
                    + "=" + income.getId(), null);
        if (cursor != null) cursor.close();
        return count;
    }

    /**
     * 新增收入信息
     *
     * @param income 收入信息
     * @return 新增收入信息的Uri
     */
    public Uri add(Income income) {
        //数据库_id字段设置为自动递增，因此，新增记录不需要赋_id值
        ContentValues values = new ContentValues();
        values.put(AccountContentProvider.COLUMN_INCOME_AMOUNT, income.getAmount());
        values.put(AccountContentProvider.COLUMN_INCOME_TIME, dateFormat.format(income.getTime()));
        values.put(AccountContentProvider.COLUMN_INCOME_TYPE, income.getType());
        values.put(AccountContentProvider.COLUMN_INCOME_PAYER, income.getPayer());
        values.put(AccountContentProvider.COLUMN_INCOME_COMMENT, income.getComment());

        return cr.insert(AccountContentProvider.CONTENT_INCOME_URI, values);
    }

    /**
     * 删除指定编号的收入信息
     *
     * @param id 收入编号
     * @return int 删除收入信息的记录数
     */
    public int delete(long id) {
        return cr.delete(ContentUris.withAppendedId(AccountContentProvider.CONTENT_INCOME_URI, id), null, null);
    }

    /**
     * 删除指定的一系列收入编号的记录
     *
     * @param ids 指定的一系列收入编号
     * @return 删除收入信息的记录数
     */
    public int delete(long... ids) {
        int count = 0;
        for (long id : ids)
            count += delete(id);
        return count;
    }

    /**
     * 获取收入信息总记录数
     *
     * @return int 收入信息总记录数
     */
    public int getCount() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, new String[]{"count("
                + AccountContentProvider.COLUMN_INCOME_ID + ")"}, null, null, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst())
                count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * 获取收入信息最大的编号
     *
     * @return int 收入信息最大的编号
     */
    public long getMaxId() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, new String[]{"max("
                + AccountContentProvider.COLUMN_INCOME_ID + ")"}, null, null, null);
        int maxId = 0;
        if (cursor != null && cursor.moveToFirst()) {
            maxId = cursor.getInt(0);
        }
        if (cursor != null) cursor.close();
        return maxId;
    }

    /**
     * 获取所有收入信息列表
     *
     * @return List<Income>
     */
    public List<Income> getIncomeList() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, null, null, null, null);
        List<Income> incomeList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Income income = null;
                try {
                    income = new Income(
                            // id
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)) ? 0
                                    : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)),
                            // amount
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)) ? 0
                                    : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)),
                            // time
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME)) ? null
                                    : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME))),
                            // type
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)),
                            // 4: payer
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)),
                            // 5: comment
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                incomeList.add(income);
            }
            cursor.close();
        }
        return incomeList;
    }

    /**
     * 获取指定日期范围内所有收入信息记录
     *
     * @param startDate 起始日期
     * @param endDate   终止日期
     * @return List<Income>
     */
    public List<Income> getIncomeList(Date startDate, Date endDate) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, null,
                AccountContentProvider.COLUMN_INCOME_TIME + ">=" + "'" + dateFormat.format(startDate)
                        + "' and " + AccountContentProvider.COLUMN_INCOME_TIME + "<=" + "'"
                        + dateFormat.format(endDate) + "'", null, null);
        List<Income> incomeList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Income income = null;
                try {
                    income = new Income(
                            // id
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)) ? 0
                                    : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)),
                            // amount
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)) ? 0
                                    : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)),
                            // time

                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME)) ? null
                                    : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME))),
                            // type
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)),
                            // 4: payer
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)),
                            // 5: comment
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                incomeList.add(income);
            }
            cursor.close();
        }
        return incomeList;
    }

    /**
     * 获取指定日期所有收入信息记录
     *
     * @param date 指定日期
     * @return List<Income>
     */
    public List<Income> getIncomeList(Date date) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, null,
                AccountContentProvider.COLUMN_INCOME_TIME + "=" + "'" + dateFormat.format(date)
                        + "' ", null, null);
        List<Income> incomeList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Income income = null;
                try {
                    income = new Income(
                            // id
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)) ? 0
                                    : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)),
                            // amount
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)) ? 0
                                    : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)),
                            // time

                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME)) ? null
                                    : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME))),
                            // type
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)),
                            // 4: payer
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)),
                            // 5: comment
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT))
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                incomeList.add(income);
            }
            cursor.close();
        }
        return incomeList;
    }

    /**
     * 从指定索引处开始，获取指定数量的收入信息记录
     *
     * @param start 指定索引值
     * @param count 指定数量,必须大于0
     * @return List<Income> 收入信息记录集合
     */
    public List<Income> getIncomeList(int start, int count) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI, null, null, null,
                AccountContentProvider.COLUMN_INCOME_TIME + " DESC");
        List<Income> incomeList = new ArrayList<>();
        if (cursor != null && count > 0) {
            if (cursor.move(start)) {
                do {
                    Income income = null;
                    try {
                        income = new Income(
                                // id
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)) ? 0
                                        : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_ID)),
                                // amount
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)) ? 0
                                        : cursor.getDouble(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_AMOUNT)),
                                // time
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME)) ? null
                                        : dateFormat.parse(cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TIME))),
                                // type
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)) ? null
                                        : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_TYPE)),
                                // 4: payer
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)) ? null
                                        : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_PAYER)),
                                // 5: comment
                                cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT)) ? null
                                        : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_INCOME_COMMENT))
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    incomeList.add(income);
                } while (cursor.moveToNext() && count-- > 1);
            }
            cursor.close();
        }
        return incomeList;
    }

    /**
     * 获取指定日期范围内所有收入金额
     *
     * @param startDate 起始日期
     * @param endDate   终止日期
     * @return float 总金额
     */
    public float getSumAmount(Date startDate, Date endDate) {
        float amount = 0;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_INCOME_AMOUNT + ")"},
                AccountContentProvider.COLUMN_INCOME_TIME + ">=" + "'" + dateFormat.format(startDate)
                        + "' and " + AccountContentProvider.COLUMN_INCOME_TIME + "<=" + "'"
                        + dateFormat.format(endDate) + "'", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取指定年月所有月收入总金额
     *
     * @param year  年
     * @param month 月
     * @return float 月收入总金额
     */
    public float getSumMonthAmount(int year, int month) {
        float amount = 0;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_INCOME_AMOUNT + ")"},
                "strftime('%Y%m'," + AccountContentProvider.COLUMN_INCOME_TIME + ")='" + (year * 100 + month)
                        + "'", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取指定日期所属月收入总金额
     *
     * @param date 指定日期
     * @return float 月收入总金额
     */
    public float getSumMonthAmount(Date date) {
        float amount = 0;
        if (date == null) return 0;
        String dateString = dateFormat.format(date);
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_INCOME_AMOUNT + ")"},
                "strftime('%Y'," + AccountContentProvider.COLUMN_INCOME_TIME + ")=" + "strftime('%Y','" + dateString
                        + "') and " + "strftime('%m'," + AccountContentProvider.COLUMN_INCOME_TIME + ")="
                        + "strftime('%m','" + dateString + "')", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取所有收入总金额
     *
     * @return float 总金额
     */
    public float getSumAmount() {
        float amount = 0;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI,
                new String[]{"sum(" + AccountContentProvider.COLUMN_INCOME_AMOUNT + ")"},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            amount = cursor.getFloat(0);
        }
        if (cursor != null) cursor.close();
        return amount;
    }

    /**
     * 获取最大的收入日期
     *
     * @return 最大的收入日期
     */
    @Nullable
    public Date getMaxDate() {
        Date date = null;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI,
                new String[]{"max(" + AccountContentProvider.COLUMN_INCOME_TIME + ")"},
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                try {
                    date = dateFormat.parse(cursor.getString(0));
                    //返回值是null，程序会错误
                    //date = dateFormat.parse("2017-8-25");
                } catch (ParseException e) {
                    date = null;
                }
            cursor.close();
        }
        return date;
    }

    /**
     * 获取最小的收入日期
     *
     * @return 最小的收入日期
     */
   @Nullable
    public Date getMinDate() {
        Date date = null;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_INCOME_URI,
                new String[]{"min(" + AccountContentProvider.COLUMN_INCOME_TIME + ")"},
                null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst())
                try {
                    Log.i(TAG, "getMinDate: "+cursor.getString(0));
                    date = dateFormat.parse("cursor.getString(0)");
                    //返回值是null，程序会错误
                    //date = dateFormat.parse("2015-1-1");
                } catch (ParseException e) {
                    date = null;
                }
            cursor.close();
        }
        return date;
    }
}