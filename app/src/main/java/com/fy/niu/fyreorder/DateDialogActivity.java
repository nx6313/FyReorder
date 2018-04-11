package com.fy.niu.fyreorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.fy.niu.fyreorder.util.ComFun;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.aigestudio.datepicker.cons.DPMode;
import cn.aigestudio.datepicker.views.DatePicker;

public class DateDialogActivity extends Activity {
    private DatePicker asDatePicker;

    private String defaultSelectDate = null; // 默认选择的日期
    private String curentSelectDateStr = null; // 记录当前选择的日期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_dialog);

        ComFun.addToActiveActivityList(DateDialogActivity.this);

        initView();
    }

    private void initView() {
        defaultSelectDate = getIntent().getStringExtra("defaultSelectDate");

        // 设置弹框Activity页面大小
        WindowManager.LayoutParams p = getWindow().getAttributes();
        //p.height = 1000;
        p.width = ComFun.getScreenWidth() - 40;
        p.alpha = 1.0f; //设置本身透明度
        p.dimAmount = 0.8f; //设置黑暗度
        p.gravity = Gravity.CENTER;
        getWindow().setAttributes(p); //设置生效
        getWindow().setGravity(Gravity.CENTER);

        Calendar c = Calendar.getInstance();

        asDatePicker = (DatePicker) findViewById(R.id.asDatePicker);
        if (ComFun.strNull(defaultSelectDate)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat yearFormat = new SimpleDateFormat("yyyy");
            DateFormat monthFormat = new SimpleDateFormat("MM");
            DateFormat dayFormat = new SimpleDateFormat("dd");
            try {
                Date selectDate = dateFormat.parse(defaultSelectDate);
                c.set(Integer.parseInt(yearFormat.format(selectDate)), Integer.parseInt(monthFormat.format(selectDate)) - 1, Integer.parseInt(dayFormat.format(selectDate)) - 1);
            } catch (ParseException e) {
            }
        }
        asDatePicker.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
        asDatePicker.setMode(DPMode.SINGLE);
        asDatePicker.setOnDatePickedListener(new DatePicker.OnDatePickedListener() {
            @Override
            public void onDatePicked(String date) {
                DateFormat dateFormatOld = new SimpleDateFormat("yyyy-M-d");
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date selectDate = dateFormatOld.parse(date);
                    String formatSelectDate = dateFormat.format(selectDate);
                    curentSelectDateStr = formatSelectDate;
                } catch (ParseException e) {
                }
            }
        });
    }

    public void cancelSelectDate(View view) {
        Intent i = new Intent();
        setResult(0, i);
        DateDialogActivity.this.finish();
    }

    public void clearSelectDate(View view) {
        Intent i = new Intent();
        setResult(2, i);
        DateDialogActivity.this.finish();
    }

    public void sureSelectDate(View view) {
        if (ComFun.strNull(curentSelectDateStr)) {
            Intent i = new Intent();
            i.putExtra("selectDate", curentSelectDateStr);
            setResult(1, i);
            DateDialogActivity.this.finish();
        } else {
            ComFun.showToast(DateDialogActivity.this, "请选择日期", Toast.LENGTH_SHORT);
        }
    }
}
