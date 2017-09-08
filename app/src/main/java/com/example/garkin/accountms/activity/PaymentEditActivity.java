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
import com.example.garkin.accountms.dao.PaymentDao;
import com.example.garkin.accountms.model.Payment;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PaymentEditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final DecimalFormat decimalFormat = new DecimalFormat("####.00");
    //    protected static final int DATE_DIALOG_ID = 0;// 创建日期对话框常量
    private EditText amountEditText, TimeEditText, addressEditText, commentEditText;// 创建4个EditText对象
    private Spinner typeSpinner;// 创建Spinner对象
    private Payment payment;  //支出信息
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
        setContentView(R.layout.activity_payment_edit);
        amountEditText = (EditText) findViewById(R.id.editPaymentAmountEditText);// 获取金额文本框
        TimeEditText = (EditText) findViewById(R.id.editPaymentTimeEditText);// 获取时间文本框
        addressEditText = (EditText) findViewById(R.id.editPaymentAddressEditText);// 获取付款方文本框
        commentEditText = (EditText) findViewById(R.id.editPaymentCommentEditText);// 获取备注文本框
        typeSpinner = (Spinner) findViewById(R.id.editPaymentTypeSpinner);// 获取类别下拉列表
        //禁止SimpleDateFormat的自动计算功能
        dateFormat.setLenient(false);
        //允许应用程序图标导航
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        //获取Intent对象附加信息Extra中Payment对象，如果为null说明为新增Payment，否则为编辑支出信息
        Object object = getIntent().getSerializableExtra(Payment.class.getName());
        if (object != null) payment = (Payment) object;
        //payment==null，新增支出信息
        if (payment == null) {
            isAddNew = true;   //新增Payment对象，isAddNew设置为true
            setTitle("新增支出");
            // 创建Payment对象
            payment = new Payment();
            // 显示设置的时间为当前时间
            TimeEditText.setText(dateFormat.format(new Date()));
            typeSpinner.setSelection(0);
            amountEditText.setText("");// 设置金额文本框为0
            addressEditText.setText("");// 设置付款方文本框为空
            commentEditText.setText("");// 设置备注文本框为空
        } else {
            //编辑支出信息
            setTitle("编辑支出");
            amountEditText.setText(decimalFormat.format(payment.getAmount()));
            TimeEditText.setText(dateFormat.format(payment.getTime()));
            addressEditText.setText(payment.getAddress());
            commentEditText.setText(payment.getComment());
            typeSpinner.setPrompt(payment.getType());
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
        getMenuInflater().inflate(R.menu.menu_payment_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //处理选项菜单项点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //保存
            case R.id.menu_payment_edit_save:
                String strAmount = amountEditText.getText().toString();// 获取金额文本框的值
                // 判断金额为空
                if (strAmount.isEmpty()) {
                    Toast.makeText(PaymentEditActivity.this, "请输入支出金额！",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                payment.setAmount(Double.parseDouble(strAmount));
                //判断日期是否正确
                try {
                    payment.setTime(dateFormat.parse(TimeEditText
                            .getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(PaymentEditActivity.this, "日期格式错误，请重新输入时间！",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                payment.setAddress(addressEditText.getText().toString());
                payment.setType(typeSpinner.getSelectedItem().toString());
                payment.setComment(commentEditText.getText().toString());
                // 创建PaymentDao对象
                PaymentDao paymentDao = PaymentDao.getPaymentDaoInstance(PaymentEditActivity.this);
                if (isAddNew) paymentDao.add(payment);// 添加支出信息
                else paymentDao.update(payment);// 更新支出信息
                // 弹出信息提示
                Toast.makeText(PaymentEditActivity.this, "〖支出信息〗保存成功！",
                        Toast.LENGTH_SHORT).show();
                PaymentEditActivity.this.finish();
                break;
            //取消
            case R.id.menu_payment_edit_cancel:
                PaymentEditActivity.this.finish();
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
            return new DatePickerDialog(getActivity(), (PaymentEditActivity) getActivity(), year, month, day);
        }
    }
}
