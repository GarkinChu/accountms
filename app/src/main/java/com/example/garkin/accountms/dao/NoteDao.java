package com.example.garkin.accountms.dao;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.garkin.accountms.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Garkin on 2017/8/17.
 */

public class NoteDao {
    private static ContentResolver cr;
    private static NoteDao noteDao;

    private NoteDao(Context context) {
        cr = context.getContentResolver();
    }

    //单实例模式，NoteDao(Context context)构造方法设置为private
    public static NoteDao getNoteDaoInstance(Context context) {
        if (noteDao == null) noteDao = new NoteDao(context);
        return noteDao;
    }

    /**
     * 根据便签编号读取便签信息
     *
     * @param id 便签编号
     * @return 便签信息
     */
    @Nullable
    public Note read(long id) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_NOTE_URI, null,
                AccountContentProvider.COLUMN_NOTE_ID + "=" + id, null, null);
        Note entity = null;
        if (cursor != null && cursor.moveToFirst()) {
            entity = new Note(
                    // id
                    cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_ID)) ? 0
                            : cursor.getInt(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_ID)),
                    // 1: note
                    cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_NOTE)) ? null
                            : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_NOTE))
            );
        }
        if (cursor != null) cursor.close();
        return entity;
    }

    /**
     * 写便签信息，先判别该便签ID是否存在，存在则更新该便签信息。
     *
     * @param note 便签信息
     * @return int 返回更新的便签信息记录数
     */
    public int update(Note note) {
        int count = 0;
        ContentValues values = new ContentValues();
        values.put(AccountContentProvider.COLUMN_NOTE_ID, note.getId());
        values.put(AccountContentProvider.COLUMN_NOTE_NOTE, note.getNote());
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_NOTE_URI, null,
                AccountContentProvider.COLUMN_NOTE_ID + "=" + note.getId(), null, null);
        if (cursor != null && cursor.moveToFirst())
            count = cr.update(AccountContentProvider.CONTENT_NOTE_URI, values, AccountContentProvider.COLUMN_NOTE_ID
                    + "=" + note.getId(), null);
        if (cursor != null) cursor.close();
        return count;
    }

    /**
     * 新增便签信息
     *
     * @param note 便签信息
     * @return 新增便签信息的Uri
     */
    public Uri add(Note note) {
        //数据库_id字段设置为自动递增，因此，新增记录不需要赋_id值
        ContentValues values = new ContentValues();
        values.put(AccountContentProvider.COLUMN_NOTE_NOTE, note.getNote());
        return cr.insert(AccountContentProvider.CONTENT_NOTE_URI, values);
    }

    /**
     * 删除指定编号的便签信息
     *
     * @param id 便签编号
     * @return int 删除便签信息的记录数
     */
    public int delete(long id) {
        return cr.delete(ContentUris.withAppendedId(AccountContentProvider.CONTENT_NOTE_URI, id), null, null);
    }

    /**
     * 删除指定的一系列便签编号的记录
     *
     * @param ids 指定的一系列便签编号
     * @return 删除便签信息的记录数
     */
    public int delete(long... ids) {
        int count = 0;
        for (long id : ids)
            count += delete(id);
        return count;
    }

    /**
     * 获取便签信息总记录数
     *
     * @return int 便签信息总记录数
     */
    public int getCount() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_NOTE_URI, new String[]{"count("
                + AccountContentProvider.COLUMN_NOTE_ID + ")"}, null, null, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst())
                count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * 获取便签信息最大的编号
     *
     * @return int 便签信息最大的编号
     */
    public int getMaxId() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_NOTE_URI, new String[]{"max("
                + AccountContentProvider.COLUMN_NOTE_ID + ")"}, null, null, null);
        int maxId = 0;
        if (cursor != null) {
            if (cursor.moveToFirst())
                maxId = cursor.getInt(0);
            cursor.close();
        }
        return maxId;
    }

    /**
     * 获取所有便签信息记录
     *
     * @return List<Note> 便签信息记录集合
     */
    public List<Note> getNoteList() {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_NOTE_URI, null, null, null, null);
        List<Note> noteList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Note note = new Note(
                        // id
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_ID)) ? 0
                                : cursor.getInt(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_ID)),
                        // 1: note
                        cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_NOTE)) ? null
                                : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_NOTE))
                );
                noteList.add(note);
            }
            cursor.close();
        }
        return noteList;
    }

    /**
     * 从指定索引处开始，获取指定数量的便签信息记录
     *
     * @param start 指定指定索引
     * @param count 指定数量,必须大于0
     * @return List<Note> 便签信息记录集合
     */
    public List<Note> getNoteList(int start, int count) {
        Cursor cursor = cr.query(AccountContentProvider.CONTENT_NOTE_URI, null, null, null, null);
        List<Note> noteList = new ArrayList<>();
        if (cursor != null && count > 0) {
            if (cursor.move(start)) {
                do {
                    Note note = new Note(
                            // id
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_ID)) ? 0
                                    : cursor.getInt(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_ID)),
                            // 1: note
                            cursor.isNull(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_NOTE)) ? null
                                    : cursor.getString(cursor.getColumnIndex(AccountContentProvider.COLUMN_NOTE_NOTE))
                    );
                    noteList.add(note);
                } while (cursor.moveToNext() && count-- > 1);
            }
        }
        if (cursor != null) cursor.close();
        return noteList;
    }
}
