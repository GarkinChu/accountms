package com.example.garkin.accountms.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.garkin.accountms.R;
import com.example.garkin.accountms.dao.PasswordDao;
import com.example.garkin.accountms.model.Password;

public class UserPasswordActivity extends AppCompatActivity {
    EditText passwordEditText;// 创建EditText对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);
        passwordEditText = (EditText) findViewById(R.id.txtPassword);// 获取密码文本框
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_password, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        PasswordDao pwdDao = PasswordDao.getPasswordDaoInstance(UserPasswordActivity.this);// 创建PwdDAO对象
        Password pwd = new Password(1, passwordEditText.getText().toString());// 根据输入的密码创建Tb_pwd对象
        // 判断数据库中是否已经设置了密码
        pwdDao.write(pwd);// 添加用户密码或修改用户密码
        // 弹出信息提示
        Toast.makeText(UserPasswordActivity.this, "〖密码〗设置成功！", Toast.LENGTH_SHORT)
                .show();
        UserPasswordActivity.this.finish();
        return super.onOptionsItemSelected(item);
    }
}
