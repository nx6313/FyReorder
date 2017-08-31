package com.fy.niu.fyreorder;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.customView.CircularImage;
import com.fy.niu.fyreorder.customView.ElasticScrollView;
import com.fy.niu.fyreorder.customView.SlideMenu;
import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.MainOrderFragment;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.DBOpenHelper;
import com.fy.niu.fyreorder.util.DBUtil;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private long exitTime;
    private SlideMenu leftMenu; // 左侧菜单
    private CircularImage userHeadImg;

    private ViewPager mainViewPager;
    private ViewPagerIndicator mainIndicator;

    private LinearLayout orderUserIfOpenLayout;

    private List<String> mTitles = Arrays.asList("未接订单", "已接订单");
    private List<MainOrderFragment> mContents = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;

    private CircularImage mainUserHeadImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(MainActivity.this);

        // 处理为标题居中
        ((TextView) toolbar.findViewById(R.id.toolbarTitleTv)).setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mainUserHeadImg = (CircularImage) toolbar.findViewById(R.id.mainUserHeadImg);
        mainUserHeadImg.setImageResource(R.drawable.default_user_head_1);

        initView();

        // 用户静默登录
        boolean needSilentLogin = getIntent().getBooleanExtra("needSilentLogin", false);
        if(needSilentLogin){
            String userAccountNum = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userLoginName");
            String userAccountPass = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userLoginPass");
            userSilentLogin(userAccountNum, userAccountPass);
        }else{
            initUserData();
            initDatas();
        }

        mainIndicator.setVisibleTabCount(mTitles.size());
        mainIndicator.setTabItemTitles(mTitles);

        mainViewPager.setAdapter(mAdapter);
        mainIndicator.setViewPager(mainViewPager, 0);
    }

    private void userSilentLogin(String userAccountNum, String userAccountPass) {
        ComFun.showLoading(MainActivity.this, "初始化数据中，请稍后");
        String devToken = SharedPreferencesTool.getFromShared(MainActivity.this, "fyBaseData", "userToken");
        RequestParams params = new RequestParams();
        Log.d("静默登陆中 ====== ", "设备 Token：" + devToken);
        params.put("token", devToken);
        params.put("loginName", userAccountNum);
        params.put("passWord", userAccountPass);
        ConnectorInventory.userLogin(MainActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject data = new JSONObject(responseObj.toString());
                    if(data.get("result").equals("success")){
                        Log.d("静默登陆完成 ====== ", "开始获取用户数据");
                        initUserData();
                        initDatas();
                    }else{
                        // 登录失败，提示用户，需要手动重新登录
                        ComFun.showToast(MainActivity.this, "登录失效，需重新登录", Toast.LENGTH_SHORT);
                    }
                } catch (JSONException e) {}
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                // 登录失败，提示用户，需要手动重新登录
                ComFun.showToast(MainActivity.this, "登录失效，请重新登录", Toast.LENGTH_SHORT);
            }
        }));
    }

    private void initUserData() {
        String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userId");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        ConnectorInventory.getUserInfo(MainActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject userInfoJson = new JSONObject(responseObj.toString());
                    String userId = userInfoJson.getString("userId");
                    String floor = userInfoJson.getString("floor");
                    String floorName = userInfoJson.getString("floorName");
                    String zhi = userInfoJson.getString("zhi");
                    String wei = userInfoJson.getString("wei");
                    String grade = userInfoJson.getString("grade");
                    String userName = userInfoJson.getString("userName");
                    String roleName = userInfoJson.getString("roleName");
                    String type = userInfoJson.getString("type");
                    String orgName = userInfoJson.getString("orgName");
                    String telephone = userInfoJson.getString("telephone");
                    ContentValues values = new ContentValues();
                    values.put("userId", userId);
                    values.put("floor", floor);
                    values.put("floorName", floorName);
                    values.put("zhi", zhi);
                    values.put("wei", wei);
                    values.put("grade", grade);
                    values.put("userName", userName);
                    values.put("roleName", roleName);
                    values.put("type", type);
                    values.put("orgName", orgName);
                    values.put("telephone", telephone);
                    DBUtil.deleteAll(new DBOpenHelper(MainActivity.this), "userInfo");
                    DBUtil.save(new DBOpenHelper(MainActivity.this), "userInfo", values);
                    // 更新UI
                    ((TextView) findViewById(R.id.leftMenuUserName)).setText(userName);
                } catch (JSONException e) {}
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.showToast(MainActivity.this, "获取个人信息异常", Toast.LENGTH_SHORT);
            }
        }));
    }

    private void initView() {
        leftMenu = (SlideMenu) findViewById(R.id.leftMenu);
        userHeadImg = (CircularImage) findViewById(R.id.userHeadImg);
        userHeadImg.setImageResource(R.drawable.default_user_head_1);

        mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mainIndicator = (ViewPagerIndicator) findViewById(R.id.mainIndicator);

        orderUserIfOpenLayout = (LinearLayout) findViewById(R.id.orderUserIfOpenLayout);

        String ifOpen = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "ifOpen");
        if(ifOpen.equals("1")){
            orderUserIfOpenLayout.setTag("true");
            ((TextView) orderUserIfOpenLayout.getChildAt(0)).setText("当前正在听单中，点击停止接单");
        }

        Map<String, List<Order>> orderDataMap = new LinkedHashMap<>();
        orderDataMap.put("weiJie", new ArrayList<Order>());
        orderDataMap.put("yiJie", new ArrayList<Order>());

        for(Map.Entry<String, List<Order>> orderMap : orderDataMap.entrySet()){
            MainOrderFragment fragment = MainOrderFragment.newInstance(orderMap.getKey(), orderMap.getValue());
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
        final Map<String, List<Order>> orderDataMap = new LinkedHashMap<>();
        orderDataMap.put("weiJie", new ArrayList<Order>());
        orderDataMap.put("yiJie", new ArrayList<Order>());

        // 请求数据库获取数据
        ComFun.showLoading(MainActivity.this, "正在获取订单列表，请稍后");
        final String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userId");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("ifTake", "0");
        ConnectorInventory.getOrderList(MainActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject orderDataJson = new JSONObject(responseObj.toString());
                    JSONArray dataList = orderDataJson.getJSONArray("dataList");
                    Log.d(" ==== 未接订单列表数据 === ", " ===> " + dataList);
                    if(dataList.length() > 0){
                        List<Order> weiJieOrderList = getOrderListFromJson(dataList);
                        orderDataMap.put("weiJie", weiJieOrderList);
                    }else{
                        List<Order> weiJieOrderList = new ArrayList<>();
                        orderDataMap.put("weiJie", weiJieOrderList);
                    }
                } catch (JSONException e) {}
                RequestParams params2 = new RequestParams();
                params2.put("userId", userId);
                params2.put("ifTake", "1");
                ConnectorInventory.getOrderList(MainActivity.this, params2, new DisposeDataHandle(new DisposeDataListener() {
                    @Override
                    public void onFinish() {
                        ComFun.hideLoading();
                    }

                    @Override
                    public void onSuccess(Object responseObj) {
                        try {
                            JSONObject orderDataJson = new JSONObject(responseObj.toString());
                            JSONArray dataList = orderDataJson.getJSONArray("dataList");
                            Log.d(" ==== 已接订单列表数据 === ", " ===> " + dataList);
                            if(dataList.length() > 0){
                                List<Order> yiJieOrderList = getOrderListFromJson(dataList);
                                orderDataMap.put("yiJie", yiJieOrderList);
                            }else{
                                List<Order> yiJieOrderList = new ArrayList<>();
                                orderDataMap.put("yiJie", yiJieOrderList);
                            }
                        } catch (JSONException e) {}
                        // 更新数据
                        updateOrderViewPager(orderDataMap);
                    }

                    @Override
                    public void onFailure(OkHttpException okHttpE) {
                        ComFun.showToast(MainActivity.this, "获取已接订单数据异常", Toast.LENGTH_SHORT);
                    }
                }));
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.hideLoading();
                ComFun.showToast(MainActivity.this, "获取未接订单数据异常", Toast.LENGTH_SHORT);
            }
        }));
    }

    private List<Order> getOrderListFromJson(JSONArray dataList) {
        List<Order> orderList = new ArrayList<>();
        for(int i = 0; i < dataList.length(); i++){
            try {
                JSONObject orderDataJson = dataList.getJSONObject(i);
                Order order = new Order();
                order.setId(orderDataJson.getString("id"));
                order.setIdenCode(orderDataJson.getString("idenCode"));
                order.setFloorId(orderDataJson.getString("floorId"));
                order.setFloorName(orderDataJson.getString("floorName"));
                order.setOrderState(orderDataJson.getInt("orderState"));
                order.setOrderType(orderDataJson.getInt("orderType"));
                order.setOrderDate(orderDataJson.getString("orderDate"));
                order.setOrderNumber(orderDataJson.getString("orderNumber"));
                order.setOrderPrice(orderDataJson.getString("orderPrice"));
                order.setAddressDetail(orderDataJson.getString("addressDetail"));

                order.setUserName("用户" + i);
                order.setUserPhone("213542336" + i);
                order.setSchool("魔鬼学院");
                order.setRemark("哇哈哈哈");
                order.setState(2);

                JSONArray buyContentJsonArr = orderDataJson.getJSONArray("children");
                List<Order.BuyContent> buyContentList = new ArrayList<>();
                for(int j = 0; j < buyContentJsonArr.length(); j++){
                    JSONObject buyContentJsonObj = buyContentJsonArr.getJSONObject(j);
                    Order.BuyContent buyContent = new Order.BuyContent();
                    buyContent.setProId(buyContentJsonObj.getString("proId"));
                    buyContent.setNum(buyContentJsonObj.getInt("num"));
                    buyContent.setPrice(buyContentJsonObj.getString("price"));
                    buyContent.setUrl(buyContentJsonObj.getString("url"));

                    buyContent.setName("冰红茶");
                    buyContentList.add(buyContent);
                }
                order.setOrderDetail(buyContentList);
                orderList.add(order);
            } catch (JSONException e) {}
        }
        return orderList;
    }

    private void updateOrderViewPager(Map<String, List<Order>> orderDataMap) {
        ElasticScrollView mainOrderScrollView_weiJie = (ElasticScrollView) mainViewPager.findViewWithTag("orderScrollView_weiJie");
        LinearLayout noMainOrderDataLayout_weiJie = (LinearLayout) mainViewPager.findViewWithTag("noDataLayout_weiJie");
        ElasticScrollView mainOrderScrollView_yiJie = (ElasticScrollView) mainViewPager.findViewWithTag("orderScrollView_yiJie");
        LinearLayout noMainOrderDataLayout_yiJie = (LinearLayout) mainViewPager.findViewWithTag("noDataLayout_yiJie");
        if(mainOrderScrollView_weiJie != null && noMainOrderDataLayout_weiJie != null &&
                mainOrderScrollView_yiJie != null && noMainOrderDataLayout_yiJie != null){
            List<Order> weiJieOrderListNew = orderDataMap.get("weiJie");
            List<Order> yiJieOrderListNew = orderDataMap.get("yiJie");
            if(ComFun.strNull(weiJieOrderListNew, true)){
                noMainOrderDataLayout_weiJie.setVisibility(View.GONE);
                mainOrderScrollView_weiJie.setVisibility(View.VISIBLE);
                LinearLayout mainOrderDataLayout = (LinearLayout) mainOrderScrollView_weiJie.findViewById(R.id.mainOrderDataLayout);
                mainOrderDataLayout.removeAllViews();
                MainOrderFragment.createMainOrderView(MainActivity.this, mainOrderDataLayout, weiJieOrderListNew);
            }else{
                mainOrderScrollView_weiJie.setVisibility(View.GONE);
                noMainOrderDataLayout_weiJie.setVisibility(View.VISIBLE);
            }
            if(ComFun.strNull(yiJieOrderListNew, true)){
                noMainOrderDataLayout_yiJie.setVisibility(View.GONE);
                mainOrderScrollView_yiJie.setVisibility(View.VISIBLE);
                LinearLayout mainOrderDataLayout = (LinearLayout) mainOrderScrollView_yiJie.findViewById(R.id.mainOrderDataLayout);
                mainOrderDataLayout.removeAllViews();
                MainOrderFragment.createMainOrderView(MainActivity.this, mainOrderDataLayout, yiJieOrderListNew);
            }else{
                mainOrderScrollView_yiJie.setVisibility(View.GONE);
                noMainOrderDataLayout_yiJie.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(leftMenu.isOpen()){
                leftMenu.toggle();
            }else{
                if (System.currentTimeMillis() - exitTime > 2000) {
                    ComFun.showToast(this, "再按一次离开", 2000);
                    exitTime = System.currentTimeMillis();
                } else {
                    System.exit(0);
                }
            }
        }
        return true;
    }

    /**
     * 打开或关闭菜单
     * @param view
     */
    public void toggleMenu(View view){
        leftMenu.toggle();
    }

    /**
     * 跳转到任务列表页面
     * @param view
     */
    public void toTaskListActivity(View view) {
        Intent taskIntent = new Intent(MainActivity.this, TaskActivity.class);
        MainActivity.this.startActivity(taskIntent);
    }

    /**
     * 跳转到个人资料
     * @param view
     */
    public void toUserData(View view){
        Intent userDataIntent = new Intent(MainActivity.this, UserDataActivity.class);
        startActivity(userDataIntent);
    }

    /**
     * 跳转到已接订单
     * @param view
     */
    public void toHasReceiveOrder(View view){
        Intent hasReceiveOrderIntent = new Intent(MainActivity.this, OrderActivity.class);
        startActivity(hasReceiveOrderIntent);
    }

    /**
     * 用户退出登录
     * @param view
     */
    public void toUserLoginOut(View view){
        SharedPreferencesTool.addOrUpdate(MainActivity.this, "fyLoginUserInfo", "needLogin", true);
        // 清空后退栈
        ComFun.clearAllActiveActivity();
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    /**
     * 用户开启或关闭接单
     * @param view
     */
    public void toUserIfOpen(View view){
        final View thisView = view;
        final String ifOpenFlag = (String) view.getTag();
        ComFun.showLoading(MainActivity.this, "正在处理，请稍后");
        final String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userId");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        if(ifOpenFlag.equals("true")){
            params.put("ifOpen", "0");
        }else{
            params.put("ifOpen", "1");
        }
        ConnectorInventory.setUserIfOpen(MainActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                if(ifOpenFlag.equals("true")){
                    thisView.setTag("false");
                    ((TextView) ((LinearLayout) thisView).getChildAt(0)).setText("点击开始接单");
                    ComFun.showToast(MainActivity.this, "成功停止接单啦", Toast.LENGTH_SHORT);
                }else{
                    thisView.setTag("true");
                    ((TextView) ((LinearLayout) thisView).getChildAt(0)).setText("当前正在听单中，点击停止接单");
                    ComFun.showToast(MainActivity.this, "成功开始接单啦", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if(ifOpenFlag.equals("true")){
                    ComFun.showToast(MainActivity.this, "停止接单失败，请稍后重试", Toast.LENGTH_SHORT);
                }else{
                    ComFun.showToast(MainActivity.this, "开启接单失败，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }
}
