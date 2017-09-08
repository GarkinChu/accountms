package com.example.garkin.accountms.model;

import java.io.Serializable;

/**
 * 便签信息实体类
 * Created by Garkin on 2017/8/16.
 */

public class Note implements Serializable{
    private long id;  //便签编号
    private String note;  //便签内容

    public Note() {
    }

    public Note(long id, String note) {
        this.id = id;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", note='" + note + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note1 = (Note) o;
        return id == note1.id && note.equals(note1.note);
    }
}
