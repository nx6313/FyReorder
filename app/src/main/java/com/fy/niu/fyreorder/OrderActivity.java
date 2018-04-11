package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.model.ReceivedOrderData;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.UserDataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderActivity extends AppCompatActivity {
    public final int STARTDATE_REQUEST_CODE = 0x11; // 选择开始日期返回的activity
    public final int ENDDATE_REQUEST_CODE = 0x22; // 选择结束日期返回的activity
    private TextView receiveOrderSumNum;
    private TextView receiveOrderSumCharge;
    private EditText etStartDate;
    private EditText etEndDate;

    private TextView wxOrderNumTv;
    private TextView wxOrderJinTv;
    private TextView hdOrderNumTv;
    private TextView hdOrderJinTv;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(OrderActivity.this);

        // 处理为标题居中
        ((TextView) toolbar.findViewById(R.id.toolbarTitleTv)).setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupActionBar();

        userType = UserDataUtil.getDataByKey(OrderActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_ifGive);

        initView();
        initDatas();

        if (!userType.equals("0")) {
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            OrderActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        receiveOrderSumNum = (TextView) findViewById(R.id.receiveOrderSumNum);
        receiveOrderSumCharge = (TextView) findViewById(R.id.receiveOrderSumCharge);
        if (!userType.equals("0")) {
            receiveOrderSumNum.setText("订单总数：0");
            receiveOrderSumCharge.setText("订单总金额：0 元");
        } else {
            receiveOrderSumNum.setText("接单总数：0");
            receiveOrderSumCharge.setText("接单总工资：0 元");
        }
        etStartDate = (EditText) findViewById(R.id.etStartDate);
        etEndDate = (EditText) findViewById(R.id.etEndDate);

        wxOrderNumTv = (TextView) findViewById(R.id.wxOrderNumTv);
        wxOrderJinTv = (TextView) findViewById(R.id.wxOrderJinTv);
        hdOrderNumTv = (TextView) findViewById(R.id.hdOrderNumTv);
        hdOrderJinTv = (TextView) findViewById(R.id.hdOrderJinTv);
    }

    private void initDatas() {
        // 请求数据库获取数据
        ComFun.showLoading(OrderActivity.this, "正在获取数据，请稍后");
        searchDataPublic(null, null);
    }

    private void updateOrderViewPager(ReceivedOrderData receivedOrderData) {
        if (!userType.equals("0")) {
            receiveOrderSumNum.setText("订单总数：" + receivedOrderData.getAllCount());
            receiveOrderSumCharge.setText("订单总金额：" + receivedOrderData.getAllMoney() + " 元");
        } else {
            receiveOrderSumNum.setText("接单总数：" + receivedOrderData.getAllCount());
            receiveOrderSumCharge.setText("接单总工资：" + receivedOrderData.getAllMoney() + " 元");
        }

        wxOrderNumTv.setText(receivedOrderData.getWeiPayCount() + "");
        wxOrderJinTv.setText(receivedOrderData.getWeiPayMoney() + " 元");
        hdOrderNumTv.setText(receivedOrderData.getHuoDaoCount() + "");
        hdOrderJinTv.setText(receivedOrderData.getHuoDaoMoney() + " 元");
    }

    public void searchData(View view) {
        ComFun.showLoading(OrderActivity.this, "正在获取数据，请稍后");
        String startDate = ComFun.strNull(etStartDate.getText().toString()) ? etStartDate.getText().toString().trim() : null;
        String endDate = ComFun.strNull(etEndDate.getText().toString()) ? etEndDate.getText().toString().trim() : null;
        searchDataPublic(startDate, endDate);
    }

    private void searchDataPublic(String startDate, String endDate) {
        final String userId = UserDataUtil.getUserId(OrderActivity.this);
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        if (ComFun.strNull(startDate) && ComFun.strNull(endDate)) {
            params.put("startDate", startDate);
            params.put("endDate", endDate);
        }
        ConnectorInventory.getOrderChargeByDate(OrderActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject orderDataJson = new JSONObject(responseObj.toString());
                    JSONObject orderData = orderDataJson.getJSONObject("body");
                    String weiPayCount = orderData.getString("weiPayCount"); // 微信支付订单数
                    String weiPayMoney = orderData.getString("weiPayMoney"); // 微信支付总金额
                    String huoDaoCount = orderData.getString("huoDaoCount"); // 货到付款订单数
                    String huoDaoMoney = orderData.getString("huoDaoMoney"); // 货到付款总金额
                    String allCount = orderData.getString("allCount"); // 总订单数
                    String allMoney = orderData.getString("allMoney"); // 总订单金额
                    ReceivedOrderData receivedOrderData = new ReceivedOrderData();
                    receivedOrderData.setWeiPayCount(Integer.parseInt(weiPayCount));
                    receivedOrderData.setWeiPayMoney(weiPayMoney);
                    receivedOrderData.setHuoDaoCount(Integer.parseInt(huoDaoCount));
                    receivedOrderData.setHuoDaoMoney(huoDaoMoney);
                    receivedOrderData.setAllCount(Integer.parseInt(allCount));
                    receivedOrderData.setAllMoney(allMoney);
                    updateOrderViewPager(receivedOrderData);
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.showToast(OrderActivity.this, "获取订单数据失败，请稍后再试", Toast.LENGTH_SHORT);
            }
        }));
    }

    public void selectStartDate(View view) {
        Intent dateSelectDialogIntent = new Intent(OrderActivity.this, DateDialogActivity.class);
        String startDate = etStartDate.getText().toString().trim();
        if (ComFun.strNull(startDate)) {
            dateSelectDialogIntent.putExtra("defaultSelectDate", startDate);
        }
        startActivityForResult(dateSelectDialogIntent, STARTDATE_REQUEST_CODE);
    }

    public void selectEndDate(View view) {
        Intent dateSelectDialogIntent = new Intent(OrderActivity.this, DateDialogActivity.class);
        String endDate = etEndDate.getText().toString().trim();
        if (ComFun.strNull(endDate)) {
            dateSelectDialogIntent.putExtra("defaultSelectDate", endDate);
        }
        startActivityForResult(dateSelectDialogIntent, ENDDATE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STARTDATE_REQUEST_CODE && resultCode == 1) {
            String s = data.getStringExtra("selectDate");
            if (ComFun.strNull(s)) {
                if (ComFun.strNull(etEndDate.getText().toString())) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date startDate = dateFormat.parse(s);
                        Date endDate = dateFormat.parse(etEndDate.getText().toString().trim());
                        if (startDate.getTime() > endDate.getTime()) {
                            ComFun.showToast(OrderActivity.this, "开始日期不能大于结束日期", Toast.LENGTH_SHORT);
                            etStartDate.setText("");
                            return;
                        }
                    } catch (ParseException e) {
                    }
                }
                etStartDate.setText(s);
            }
        } else if (requestCode == ENDDATE_REQUEST_CODE && resultCode == 1) {
            String s = data.getStringExtra("selectDate");
            if (ComFun.strNull(s)) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date startDate = dateFormat.parse(etStartDate.getText().toString().trim());
                    Date endDate = dateFormat.parse(s);
                    if (startDate.getTime() > endDate.getTime()) {
                        ComFun.showToast(OrderActivity.this, "结束日期不能小于开始日期", Toast.LENGTH_SHORT);
                        etEndDate.setText("");
                        return;
                    }
                } catch (ParseException e) {
                }
                etEndDate.setText(s);
            }
        } else if (requestCode == STARTDATE_REQUEST_CODE && resultCode == 2) {
            etStartDate.setText("");
        } else if (requestCode == ENDDATE_REQUEST_CODE && resultCode == 2) {
            etEndDate.setText("");
        }
    }
}
