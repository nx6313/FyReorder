package com.fy.niu.fyreorder;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.Constants;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DbsyActivity extends AppCompatActivity {
    private SwipeRefreshLayout dbsySwipeRefresh;
    private LinearLayout dbsyDataLayout;
    private SwipeRefreshLayout noDbsyDataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbsy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(DbsyActivity.this);

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
            DbsyActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        dbsySwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.dbsySwipeRefresh);
        dbsyDataLayout = (LinearLayout) findViewById(R.id.dbsyDataLayout);
        noDbsyDataLayout = (SwipeRefreshLayout) findViewById(R.id.noDbsyDataLayout);
        dbsySwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData(true);
            }
        });
        noDbsyDataLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData(true);
            }
        });
    }

    private void initData(final boolean isRefFlag) {
        ComFun.showLoading(DbsyActivity.this, "加载数据中，请稍后");
        final String userId = SharedPreferencesTool.getFromShared(DbsyActivity.this, "fyLoginUserInfo", "userId");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        ConnectorInventory.getAgreeList(DbsyActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
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
                    Log.d(" ==== 代办事宜数据 === ", " ===> " + orderDataJson);
                    if (orderDataJson.getBoolean("success")) {
                        JSONArray dataList = orderDataJson.getJSONObject("body").getJSONArray("userList");
                        if (dataList.length() > 0) {
//                        List<Order> completeOrderList = getOrderListFromJson(dataList);
                            // 更新数据
                            dbsySwipeRefresh.setVisibility(View.VISIBLE);
                            noDbsyDataLayout.setVisibility(View.GONE);
                        } else {
                            dbsySwipeRefresh.setVisibility(View.GONE);
                            noDbsyDataLayout.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(DbsyActivity.this, "获取代办事宜数据超时", Toast.LENGTH_SHORT);
                } else {
                    ComFun.showToast(DbsyActivity.this, "获取代办事宜数据异常", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    private void hideRefLayout() {
        dbsySwipeRefresh.setRefreshing(false);
        noDbsyDataLayout.setRefreshing(false);
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
