package com.fy.niu.fyreorder;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.fragment.MainOrderFragment;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompleteOrderActivity extends AppCompatActivity {
    private SwipeRefreshLayout completeOrderSwipeRefresh;
    private LinearLayout completeOrderDataLayout;
    private SwipeRefreshLayout noCompleteOrderDataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(CompleteOrderActivity.this);

        // 处理为标题居中
        ((TextView) toolbar.findViewById(R.id.toolbarTitleTv)).setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupActionBar();

        initView();

        initData(false);
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
            CompleteOrderActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        completeOrderSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.completeOrderSwipeRefresh);
        completeOrderDataLayout = (LinearLayout) findViewById(R.id.completeOrderDataLayout);
        noCompleteOrderDataLayout = (SwipeRefreshLayout) findViewById(R.id.noCompleteOrderDataLayout);
        completeOrderSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData(true);
            }
        });
        noCompleteOrderDataLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData(true);
            }
        });
    }

    private void initData(final boolean isRefFlag) {
        ComFun.showLoading(CompleteOrderActivity.this, "正在获取已完成订单列表，请稍后");
        final String userId = SharedPreferencesTool.getFromShared(CompleteOrderActivity.this, "fyLoginUserInfo", "userId");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        ConnectorInventory.getCompleteOrderList(CompleteOrderActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                if (isRefFlag) {
                    hideRefLayout();
                }
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject orderDataJson = new JSONObject(responseObj.toString());
                    JSONArray dataList = orderDataJson.getJSONArray("dataList");
                    Log.d(" ==== 已完成订单列表数据 === ", " ===> " + dataList);
                    if (dataList.length() > 0) {
                        List<Order> completeOrderList = getOrderListFromJson(dataList);
                        // 更新数据
                        MainOrderFragment.createMainOrderView(CompleteOrderActivity.this, "complete", completeOrderDataLayout, completeOrderList);
                        completeOrderSwipeRefresh.setVisibility(View.VISIBLE);
                        noCompleteOrderDataLayout.setVisibility(View.GONE);
                    } else {
                        completeOrderSwipeRefresh.setVisibility(View.GONE);
                        noCompleteOrderDataLayout.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.showToast(CompleteOrderActivity.this, "获取已完成订单数据异常", Toast.LENGTH_SHORT);
            }
        }));
    }

    private void hideRefLayout() {
        completeOrderSwipeRefresh.setRefreshing(false);
        noCompleteOrderDataLayout.setRefreshing(false);
    }

    private List<Order> getOrderListFromJson(JSONArray dataList) {
        List<Order> completeOrderList = new ArrayList<>();
        for (int i = 0; i < dataList.length(); i++) {
            try {
                JSONObject orderDataJson = dataList.getJSONObject(i);
                Order order = new Order();
                order.setId(orderDataJson.getString("id"));
                order.setIdenCode(orderDataJson.getString("idenCode"));
                order.setFloorId(orderDataJson.getString("floorId"));
                order.setFloorName(orderDataJson.getString("floorName"));
                order.setOrderState(orderDataJson.getInt("orderState"));
                order.setOrderType(orderDataJson.getInt("orderType"));
                order.setPayType(orderDataJson.getInt("payType"));
                order.setOrderDate(orderDataJson.getString("orderDate"));
                order.setOrderNumber(orderDataJson.getString("orderNumber"));
                order.setOrderPrice(orderDataJson.getString("orderPrice"));
                order.setCouponPrice(orderDataJson.getString("newUser"));
                order.setServicePrice(orderDataJson.getString("service"));
                order.setAddressDetail(orderDataJson.getString("addressDetail"));
                order.setPersonName(orderDataJson.getString("personName"));
                order.setPersonPhone(orderDataJson.getString("personPhone"));

                if (orderDataJson.has("perName")) {
                    order.setUserName(orderDataJson.getString("perName"));
                }
                if (orderDataJson.has("perTel")) {
                    order.setUserPhone(orderDataJson.getString("perTel"));
                }
                if (orderDataJson.has("remark")) {
                    order.setRemark(orderDataJson.getString("remark"));
                }
                order.setState(3);

                JSONArray buyContentJsonArr = orderDataJson.getJSONArray("children");
                List<Order.BuyContent> buyContentList = new ArrayList<>();
                for (int j = 0; j < buyContentJsonArr.length(); j++) {
                    JSONObject buyContentJsonObj = buyContentJsonArr.getJSONObject(j);
                    Order.BuyContent buyContent = new Order.BuyContent();
                    buyContent.setProId(buyContentJsonObj.getString("proId"));
                    buyContent.setNum(buyContentJsonObj.getInt("num"));
                    buyContent.setPrice(buyContentJsonObj.getString("price"));
                    buyContent.setName(buyContentJsonObj.getString("proName"));
                    buyContent.setUrl(buyContentJsonObj.getString("url"));

                    buyContentList.add(buyContent);
                }
                order.setOrderDetail(buyContentList);

                completeOrderList.add(order);
            } catch (JSONException e) {
            }
        }
        return completeOrderList;
    }
}
