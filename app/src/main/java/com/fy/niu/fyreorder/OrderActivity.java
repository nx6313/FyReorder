package com.fy.niu.fyreorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.HasOrderFragment;
import com.fy.niu.fyreorder.model.ReceivedOrderData;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.UserDataUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {
    private ViewPager orderViewPager;
    private ViewPagerIndicator orderIndicator;

    private TextView receiveOrderSumNum;
    private TextView receiveOrderSumCharge;

    private List<String> mTitles = Arrays.asList("昨日接单", "上周接单", "上月接单");
    private List<HasOrderFragment> mContents = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;

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

        if(!userType.equals("0")){
            mTitles = Arrays.asList("昨日订单", "上周订单", "上月订单");
        }

        orderIndicator.setVisibleTabCount(mTitles.size());
        orderIndicator.setTabItemTitles(mTitles);

        orderViewPager.setAdapter(mAdapter);
        orderIndicator.setViewPager(orderViewPager, 0);
        orderViewPager.setOffscreenPageLimit(mTitles.size());
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
        orderViewPager = (ViewPager) findViewById(R.id.orderViewPager);
        orderIndicator = (ViewPagerIndicator) findViewById(R.id.orderIndicator);

        receiveOrderSumNum = (TextView) findViewById(R.id.receiveOrderSumNum);
        receiveOrderSumCharge = (TextView) findViewById(R.id.receiveOrderSumCharge);
        if(!userType.equals("0")){
            receiveOrderSumNum.setText("总订单数：0");
            receiveOrderSumCharge.setText("总赚得：0 元");
        }else{
            receiveOrderSumNum.setText("总接单数：0");
            receiveOrderSumCharge.setText("总工资：0 元");
        }

        Map<String, List<Object>> hasOrderDataMap = new LinkedHashMap<>();
        List<Object> todayHasOrderList = new ArrayList<>();
        todayHasOrderList.add(0);
        todayHasOrderList.add(0);
        List<Object> lastWeekHasOrderList = new ArrayList<>();
        lastWeekHasOrderList.add(0);
        lastWeekHasOrderList.add(0);
        List<Object> lastMonthHasOrderList = new ArrayList<>();
        lastMonthHasOrderList.add(0);
        lastMonthHasOrderList.add(0);
        hasOrderDataMap.put("today", todayHasOrderList);
        hasOrderDataMap.put("lastWeek", lastWeekHasOrderList);
        hasOrderDataMap.put("lastMonth", lastMonthHasOrderList);
        for(Map.Entry<String, List<Object>> hasOrderMap : hasOrderDataMap.entrySet()){
            HasOrderFragment fragment = HasOrderFragment.newInstance(hasOrderMap.getKey(), hasOrderMap.getValue());
            mContents.add(fragment);
        }

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mContents.get(position);
            }

            @Override
            public int getCount() {
                return mContents.size();
            }
        };
    }

    private void initDatas() {
        // 请求数据库获取数据
        ComFun.showLoading(OrderActivity.this, "正在获取数据，请稍后");
        final String userId = UserDataUtil.getUserId(OrderActivity.this);
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        ConnectorInventory.getOrderNumChargeByday(OrderActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject orderDataJson = new JSONObject(responseObj.toString());
                    JSONArray orderDataJsonArr = orderDataJson.getJSONArray("dataList");
                    JSONObject orderCeilDataJson = orderDataJsonArr.getJSONObject(0);
                    String dayNum = orderCeilDataJson.getString("dayNum"); // 昨日订单数
                    String dayCharge = orderCeilDataJson.getString("dayCharge"); // 昨日订单金额
                    String weekNum = orderCeilDataJson.getString("weekNum"); // 上周订单数
                    String weekCharge = orderCeilDataJson.getString("weekCharge"); // 上周订单金额
                    String monthNum = orderCeilDataJson.getString("monthNum"); // 上月订单数
                    String monthCharge = orderCeilDataJson.getString("monthCharge"); // 上月订单金额
                    String sumNum = orderCeilDataJson.getString("sumNum"); // 总订单数
                    String sumCharge = orderCeilDataJson.getString("sumCharge"); // 总订单金额
                    ReceivedOrderData receivedOrderData = new ReceivedOrderData();
                    receivedOrderData.setDayNum(Integer.parseInt(dayNum));
                    receivedOrderData.setDayCharge(dayCharge);
                    receivedOrderData.setWeekNum(Integer.parseInt(weekNum));
                    receivedOrderData.setWeekCharge(weekCharge);
                    receivedOrderData.setMonthNum(Integer.parseInt(monthNum));
                    receivedOrderData.setMonthCharge(monthCharge);
                    receivedOrderData.setSumNum(Integer.parseInt(sumNum));
                    receivedOrderData.setSumCharge(sumCharge);
                    updateOrderViewPager(receivedOrderData);
                } catch (JSONException e) {}
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.showToast(OrderActivity.this, "获取订单数据失败，请稍后再试", Toast.LENGTH_SHORT);
            }
        }));
    }

    private void updateOrderViewPager(ReceivedOrderData receivedOrderData) {
        if(!userType.equals("0")){
            receiveOrderSumNum.setText("总订单数：" + receivedOrderData.getSumNum());
            receiveOrderSumCharge.setText("总赚得：" + receivedOrderData.getSumCharge() + " 元");
        }else{
            receiveOrderSumNum.setText("总接单数：" + receivedOrderData.getSumNum());
            receiveOrderSumCharge.setText("总工资：" + receivedOrderData.getSumCharge() + " 元");
        }

        TextView tvDayNum = (TextView) orderViewPager.findViewWithTag("tvDayNum");
        TextView tvDayCharge = (TextView) orderViewPager.findViewWithTag("tvDayCharge");
        TextView tvWeekNum = (TextView) orderViewPager.findViewWithTag("tvWeekNum");
        TextView tvWeekCharge = (TextView) orderViewPager.findViewWithTag("tvWeekCharge");
        TextView tvMonthNum = (TextView) orderViewPager.findViewWithTag("tvMonthNum");
        TextView tvMonthCharge = (TextView) orderViewPager.findViewWithTag("tvMonthCharge");
        if(tvDayNum != null && tvDayCharge != null &&
                tvWeekNum != null && tvWeekCharge != null &&
                tvMonthNum != null && tvMonthCharge != null){
            tvDayNum.setText("" + receivedOrderData.getDayNum());
            tvDayCharge.setText("￥" + receivedOrderData.getDayCharge());
            tvWeekNum.setText("" + receivedOrderData.getWeekNum());
            tvWeekCharge.setText("￥" + receivedOrderData.getWeekCharge());
            tvMonthNum.setText("" + receivedOrderData.getMonthNum());
            tvMonthCharge.setText("￥" + receivedOrderData.getMonthCharge());
        }
    }

}
