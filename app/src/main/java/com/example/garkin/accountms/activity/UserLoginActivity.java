package com.example.garkin.accountms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.garkin.accountms.R;
import com.example.garkin.accountms.dao.PasswordDao;
import com.example.garkin.accountms.model.Password;

public class UserLoginActivity extends AppCompatActivity {
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        passwordEditText = (EditText) findViewById(R.id.loginPasswordEditText);// 获取密码文本框
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(UserLoginActivity.this, UserMainActivity.class);// 创建Intent对象
        PasswordDao pwdDao = PasswordDao.getPasswordDaoInstance(UserLoginActivity.this);// 创建PasswordDao对象
        // 判断是否有密码及是否输入了密码
        Password pwd = pwdDao.read();
        // 判断输入的密码是否与数据库中的密码一致
        if ((pwd == null && passwordEditText.getText().toString().isEmpty()) ||
                (pwd != null && pwd.getPassword().equals(passwordEditText.getText().toString()))) {
            startActivity(intent);// 启动主Activity
            UserLoginActivity.this.finish();
        } else {
            // 弹出信息提示
            Toast.makeText(UserLoginActivity.this, "请输入正确的密码！",
                    Toast.LENGTH_SHORT).show();
        }
        passwordEditText.setText("");// 清空密码文本框
        return super.onOptionsItemSelected(item);
    }
}


