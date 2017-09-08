package com.example.garkin.accountms.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.garkin.accountms.R;
import com.example.garkin.accountms.dao.IncomeDao;
import com.example.garkin.accountms.model.Income;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class IncomeEditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final DecimalFormat decimalFormat = new DecimalFormat("####.00");
    private EditText amountEditText, TimeEditText, payerEditText, commentEditText;// 创建4个EditText对象
    private Spinner typeSpinner;// 创建Spinner对象
    private Income income;  //收入信息
    private boolean isAddNew = false;

    /**
     * DatePickerDialog的回调方法，当DatePickerFragment的Dialog确定以后，会调用该回调方法处理已设置的时间
     *
     * @param view        DatePicker view
     * @param year        年
     * @param monthOfYear 月
     * @param dayOfMonth  日
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth);
        // 显示设置的时间
        TimeEditText.setText(dateFormat.format(c.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_edit);
        amountEditText = (EditText) findViewById(R.id.editIncomeAmountEditText);// 获取金额文本框
        TimeEditText = (EditText) findViewById(R.id.editIncomeTimeEditText);// 获取时间文本框
        payerEditText = (EditText) findViewById(R.id.editIncomePayerEditText);// 获取付款方文本框
        commentEditText = (EditText) findViewById(R.id.editIncomeCommentEditText);// 获取备注文本框
        typeSpinner = (Spinner) findViewById(R.id.editIncomeTypeSpinner);// 获取类别下拉列表
        //禁止SimpleDateFormat的自动计算功能
        dateFormat.setLenient(false);
        //允许应用程序图标导航
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        //获取Intent对象附加信息Extra中Income对象，如果为null说明为新增Income，否则为编辑收入信息
        Object object = getIntent().getSerializableExtra(Income.class.getName());
        if (object != null) income = (Income) object;
        //income==null，新增收入信息
        if (income == null) {
            isAddNew = true;   //新增Income对象，isAddNew设置为true
            setTitle("新增收入");
            // 创建Income对象
            income = new Income();
            // 显示设置的时间为当前时间
            TimeEditText.setText(dateFormat.format(new Date()));
            typeSpinner.setSelection(0);
            amountEditText.setText("");// 设置金额文本框为0
            payerEditText.setText("");// 设置付款方文本框为空
            commentEditText.setText("");// 设置备注文本框为空
        } else {
            //编辑收入信息
            setTitle("编辑收入");
            amountEditText.setText(decimalFormat.format(income.getAmount()));
            TimeEditText.setText(dateFormat.format(income.getTime()));
            payerEditText.setText(income.getPayer());
            commentEditText.setText(income.getComment());
            typeSpinner.setPrompt(income.getType());
        }
        TimeEditText.setOnClickListener(new View.OnClickListener() {// 为时间文本框设置单击监听事件
            @Override
            public void onClick(View v) {
                // 显示日期选择对话框
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    //创建选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_income_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //处理选项菜单项点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //保存
            case R.id.menu_income_edit_save:
                String strAmount = amountEditText.getText().toString();// 获取金额文本框的值
                // 判断金额为空
                if (strAmount.isEmpty()) {
                    Toast.makeText(IncomeEditActivity.this, "请输入收入金额！",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                income.setAmount(Double.parseDouble(strAmount));
                //判断日期是否正确
                try {
                    income.setTime(dateFormat.parse(TimeEditText
                            .getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(IncomeEditActivity.this, "日期格式错误，请重新输入时间！",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                income.setPayer(payerEditText.getText().toString());
                income.setType(typeSpinner.getSelectedItem().toString());
                income.setComment(commentEditText.getText().toString());
                // 创建IncomeDao对象
                IncomeDao incomeDao = IncomeDao.getIncomeDaoInstance(IncomeEditActivity.this);
                if (isAddNew) incomeDao.add(income);// 添加收入信息
                else incomeDao.update(income);// 更新收入信息
                // 弹出信息提示
                Toast.makeText(IncomeEditActivity.this, "〖收入信息〗保存成功！",
                        Toast.LENGTH_SHORT).show();
                IncomeEditActivity.this.finish();
                break;
            //取消
            case R.id.menu_income_edit_cancel:
                IncomeEditActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 日期选择对话框Fragment
     */
    public static class DatePickerFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), (IncomeEditActivity) getActivity(), year, month, day);
        }
    }
}

