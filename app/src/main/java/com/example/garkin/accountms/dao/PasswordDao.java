package com.example.garkin.accountms.dao;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.example.garkin.accountms.model.Password;

/**
 * Created by Garkin on 2017/8/17.
 */

public class PasswordDao {
    private static ContentResolver cr;
    private static PasswordDao passwordDao;

    private PasswordDao(Context context) {
        cr = context.getContentResolver();
    }

    //单实例模式，PasswordDao(Context context)构造方法设置为private
    public static PasswordDao getPasswordDaoInstance(Context context) {
        if (passwordDao == null) passwordDao = new PasswordDao(context);
        return passwordDao;
    }

    /**
     * 读取密码信息,只读取第一条密码记录
     *
     * @return 密码信息
     */
    @Nullable
    public Password read() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PASSWORD_URI, null, null, null, null);
        Password entity = null;
        if (cursor != null && cursor.moveToFirst()) {
            entity = new Password(
                    // id
                    cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PASSWORD_ID)) ? 0
                            : cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_PASSWORD_ID)),
                    // 1: password
                    cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_PASSWORD_PASSWORD)) ? null
                            : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_PASSWORD_PASSWORD))
            );
        }
        if (cursor != null) cursor.close();
        return entity;
    }

    /**
     * 写密码信息，先判别是否有记录，有更新第一条记录的密码信息,否则新增密码记录。
     *
     * @param password 密码信息
     */
    public int write(@Nullable Password password) {
        if (password == null) return 0;
        int count = 0;
        ContentValues values = new ContentValues();
        values.put(AccountContentProvider.COLUMN_PASSWORD_PASSWORD, password.getPassword());
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PASSWORD_URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndex(AccountContentProvider.COLUMN_PASSWORD_ID));
            count = cr.update(ContentUris.withAppendedId(AccountContentProvider.CONTENT_PASSWORD_URI, id), values, null, null);
        } else {
            if (cr.insert(AccountContentProvider.CONTENT_PASSWORD_URI, values) != null) count = 1;
        }
        if (cursor != null) cursor.close();
        return count;
    }

    /**
     * 删除密码信息
     *
     * @return 删除密码信息记录数
     */
    public int delete() {
        return cr.delete(AccountContentProvider.CONTENT_PASSWORD_URI, null, null);
    }

    /**
     * 判别是否有密码
     *
     * @return boolean true:有密码，false:无密码
     */
    public boolean hasPassword() {
        boolean ret = false;
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_PASSWORD_URI, null, null, null, null);
        if (cursor != null) {
            cursor.close();
            ret = cursor.moveToFirst();
        }
        return ret;
    }
}
