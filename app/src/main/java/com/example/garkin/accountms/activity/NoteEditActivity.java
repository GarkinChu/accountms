package com.example.garkin.accountms.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.garkin.accountms.R;
import com.example.garkin.accountms.dao.NoteDao;
import com.example.garkin.accountms.model.Note;

public class NoteEditActivity extends AppCompatActivity {

    private EditText noteEditText;// 创建EditText对象
    private Note note;// Note对象
    private boolean isAddNew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        noteEditText = (EditText) findViewById(R.id.editNoteNoteEditText);// 获取便签文本框
        //允许应用程序图标导航
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        //获取Intent对象附加信息Extra中Note对象，如果为null说明为新增Note，否则为编辑便签信息
        Object object = getIntent().getSerializableExtra(Note.class.getName());
        if (object != null) note = (Note) object;
        //notee==null，新增便签信息
        if (note == null) {
            isAddNew = true;   //新增Note对象，isAddNew设置为true
            setTitle("新增便签");
            // 创建Note对象
            note = new Note();
            noteEditText.setText("");// 设置便签内容文本框为空
        } else {
            //编辑便签信息
            setTitle("编辑便签");
            noteEditText.setText(note.getNote());
        }
    }

    //创建选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //处理选项菜单项点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //保存
            case R.id.menu_note_edit_save:
                String strNote = noteEditText.getText().toString();// 获取便签文本框的值
                // 判断便签是否为空
                if (strNote.isEmpty()) {
                    Toast.makeText(NoteEditActivity.this, "请输入便签内容！",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                note.setNote(strNote.trim());
                // 创建NoteDao对象
                NoteDao noteDao = NoteDao.getNoteDaoInstance(NoteEditActivity.this);
                if (isAddNew) noteDao.add(note);// 添加便签信息
                else noteDao.update(note);// 更新便签信息
                // 弹出信息提示
                Toast.makeText(NoteEditActivity.this, "〖便签信息〗保存成功！",
                        Toast.LENGTH_SHORT).show();
                NoteEditActivity.this.finish();
                break;
            //取消
            case R.id.menu_note_edit_cancel:
                NoteEditActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
