package com.fy.niu.fyreorder;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.customView.CircularImage;
import com.fy.niu.fyreorder.customView.HorizontalProgressbarWithProgress;
import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.MainOrderFragment;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.model.Version;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.DBOpenHelper;
import com.fy.niu.fyreorder.util.DBUtil;
import com.fy.niu.fyreorder.util.DisplayUtil;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;
import com.fy.niu.fyreorder.util.VersionUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static Handler mHandler = null;
    public static final int MSG_UPDATE_ORDER_STATE = 1;
    public static final int MSG_REF_ORDER_LSIT = 2;
    public static final int MSG_CALL_USER_PHONE = 3;
    public static final int MSG_START_DOWN_NEW_VERSION = 4;
    private long exitTime;
    private NavigationView navigationView;
    private CircularImage userHeadImg;
    private TextView leftMenuUserName;

    private ViewPager mainViewPager;
    private ViewPagerIndicator mainIndicator;

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

        mHandler = new MainActivity.mHandler();

        mainUserHeadImg = (CircularImage) toolbar.findViewById(R.id.mainUserHeadImg);
        mainUserHeadImg.setImageResource(R.drawable.default_user_head);
        mainUserHeadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLeftMenu();
            }
        });

        initView();

        // 用户静默登录
        boolean needSilentLogin = getIntent().getBooleanExtra("needSilentLogin", false);
        if (needSilentLogin) {
            String userAccountNum = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userLoginName");
            String userAccountPass = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userLoginPass");
            userSilentLogin(userAccountNum, userAccountPass);
        } else {
            initUserData();
            initDatas(false);
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
                    Log.d("静默登陆成功，用户信息", responseObj.toString());
                    JSONObject data = new JSONObject(responseObj.toString());
                    if (data.get("result").equals("success")) {
                        initUserData();
                        initDatas(false);
                    } else {
                        // 登录失败，提示用户，需要手动重新登录
                        leftMenuUserName.setTag("needLogin");
                        leftMenuUserName.setText("登录失效，点击登录");
                        ComFun.showToast(MainActivity.this, "登录失效，需重新登录", Toast.LENGTH_SHORT);
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                // 登录失败，提示用户，需要手动重新登录
                leftMenuUserName.setTag("needLogin");
                leftMenuUserName.setText("登录失效，点击登录");
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
                MenuItem navMenuItemLogginOut = navigationView.getMenu().findItem(R.id.nav_exit);
                navMenuItemLogginOut.setVisible(true);
                try {
                    Log.d("用户信息成功", responseObj.toString());
                    DBUtil.deleteAll(new DBOpenHelper(MainActivity.this), "userInfo");
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
                    DBUtil.save(new DBOpenHelper(MainActivity.this), "userInfo", values);
                    // 更新UI
                    leftMenuUserName.setTag("hasLogin");
                    leftMenuUserName.setText(userName);
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                MenuItem navMenuItemLogginOut = navigationView.getMenu().findItem(R.id.nav_exit);
                navMenuItemLogginOut.setVisible(false);
                leftMenuUserName.setTag("needLogin");
                leftMenuUserName.setText("登录失效，点击登录");
                ComFun.showToast(MainActivity.this, "获取个人信息异常", Toast.LENGTH_SHORT);
            }
        }));
    }

    private void initView() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        userHeadImg = (CircularImage) navigationView.getHeaderView(0).findViewById(R.id.userHeadImg);
        userHeadImg.setImageResource(R.drawable.default_user_head);

        leftMenuUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.leftMenuUserName);
        userHeadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftMenuUserName.getTag().toString().equals("initLoginUserInfo")) {
                    // 正在初始化用户信息
                } else if (leftMenuUserName.getTag().toString().equals("hasLogin")) {
                    // 已经登录
                } else if (leftMenuUserName.getTag().toString().equals("needLogin")) {
                    // 需要登录
                    toUserLoginOut();
                }
            }
        });

        mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mainIndicator = (ViewPagerIndicator) findViewById(R.id.mainIndicator);

        MenuItem navMenuItemLogginOut = navigationView.getMenu().findItem(R.id.nav_exit);
        navMenuItemLogginOut.setVisible(false);

        String ifGive = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "ifGive");
        String floorName = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "floorName");
        MenuItem navMenuItemPayImg = navigationView.getMenu().findItem(R.id.nav_pay_img);
        MenuItem navMenuItemSelectSelfFloor = navigationView.getMenu().findItem(R.id.nav_select_self_floor);
        String receiveSelfFloor = SharedPreferencesTool.getFromShared(MainActivity.this, "fySet", "receiveSelfFloor");
        if (!ifGive.equals("0")) {
            navMenuItemPayImg.setVisible(false);
            navMenuItemSelectSelfFloor.setVisible(false);
        } else {
            if (receiveSelfFloor.equals("self")) {
                navMenuItemSelectSelfFloor.setTitleCondensed("self");
                navMenuItemSelectSelfFloor.setTitle("楼层切换『 当前为：" + floorName + " 』");
            } else {
                navMenuItemSelectSelfFloor.setTitleCondensed("all");
                navMenuItemSelectSelfFloor.setTitle("楼层切换『 当前为：所有 』");
            }
        }

        MenuItem navMenuItemOpenGive = navigationView.getMenu().findItem(R.id.nav_open_give);
        String ifOpen = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "ifOpen");
        if (ifOpen.equals("1")) {
            navMenuItemOpenGive.setTitleCondensed("true");
            navMenuItemOpenGive.setTitle("当前正在听单，点击停止");
        }

        String currentVersionName = ComFun.getVersionName(MainActivity.this);
        MenuItem navMenuItemUpdate = navigationView.getMenu().findItem(R.id.nav_update);
        if (ComFun.strNull(currentVersionName)) {
            navMenuItemUpdate.setTitle("检查新版本『 当前版本：" + currentVersionName + " 』");
        }

        Map<String, List<Order>> orderDataMap = new LinkedHashMap<>();
        orderDataMap.put("weiJie", new ArrayList<Order>());
        orderDataMap.put("yiJie", new ArrayList<Order>());

        for (Map.Entry<String, List<Order>> orderMap : orderDataMap.entrySet()) {
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

    private void initDatas(final boolean isRefFlag) {
        final Map<String, List<Order>> orderDataMap = new LinkedHashMap<>();
        orderDataMap.put("weiJie", new ArrayList<Order>());
        orderDataMap.put("yiJie", new ArrayList<Order>());

        // 请求数据库获取数据
        ComFun.showLoading(MainActivity.this, "正在获取订单列表，请稍后");
        final String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userId");
        String ifGive = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "ifGive");
        String floor = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "floor");
        String receiveSelfFloor = SharedPreferencesTool.getFromShared(MainActivity.this, "fySet", "receiveSelfFloor");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("ifTake", "0");
        if (ifGive.equals("0") && receiveSelfFloor.equals("self")) {
            params.put("floor", floor);
        }
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
                    if (dataList.length() > 0) {
                        List<Order> weiJieOrderList = getOrderListFromJson(dataList);
                        orderDataMap.put("weiJie", weiJieOrderList);
                    } else {
                        List<Order> weiJieOrderList = new ArrayList<>();
                        orderDataMap.put("weiJie", weiJieOrderList);
                    }
                } catch (JSONException e) {
                }
                RequestParams params2 = new RequestParams();
                params2.put("userId", userId);
                params2.put("ifTake", "1");
                ConnectorInventory.getOrderList(MainActivity.this, params2, new DisposeDataHandle(new DisposeDataListener() {
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
                            Log.d(" ==== 已接订单列表数据 === ", " ===> " + dataList);
                            if (dataList.length() > 0) {
                                List<Order> yiJieOrderList = getOrderListFromJson(dataList);
                                orderDataMap.put("yiJie", yiJieOrderList);
                            } else {
                                List<Order> yiJieOrderList = new ArrayList<>();
                                orderDataMap.put("yiJie", yiJieOrderList);
                            }
                        } catch (JSONException e) {
                        }
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
                if (isRefFlag) {
                    hideRefLayout();
                }
                ComFun.hideLoading();
                ComFun.showToast(MainActivity.this, "获取未接订单数据异常", Toast.LENGTH_SHORT);
            }
        }));
    }

    private void hideRefLayout() {
        SwipeRefreshLayout mainOrderSwipeRefresh_weiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("orderSwipeRefresh_weiJie");
        SwipeRefreshLayout noMainOrderDataLayout_weiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("noDataLayout_weiJie");
        SwipeRefreshLayout mainOrderSwipeRefresh_yiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("orderSwipeRefresh_yiJie");
        SwipeRefreshLayout noMainOrderDataLayout_yiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("noDataLayout_yiJie");
        mainOrderSwipeRefresh_weiJie.setRefreshing(false);
        noMainOrderDataLayout_weiJie.setRefreshing(false);
        mainOrderSwipeRefresh_yiJie.setRefreshing(false);
        noMainOrderDataLayout_yiJie.setRefreshing(false);
    }

    private List<Order> getOrderListFromJson(JSONArray dataList) {
        List<Order> orderList = new ArrayList<>();
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

                if (orderDataJson.has("perName")) {
                    order.setUserName(orderDataJson.getString("perName"));
                }
                if (orderDataJson.has("perTel")) {
                    order.setUserPhone(orderDataJson.getString("perTel"));
                }
                if (orderDataJson.has("remark")) {
                    order.setRemark(orderDataJson.getString("remark"));
                }
                order.setState(2);

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
                orderList.add(order);
            } catch (JSONException e) {
            }
        }
        return orderList;
    }

    private void updateOrderViewPager(Map<String, List<Order>> orderDataMap) {
        SwipeRefreshLayout mainOrderSwipeRefresh_weiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("orderSwipeRefresh_weiJie");
        SwipeRefreshLayout noMainOrderDataLayout_weiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("noDataLayout_weiJie");
        SwipeRefreshLayout mainOrderSwipeRefresh_yiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("orderSwipeRefresh_yiJie");
        SwipeRefreshLayout noMainOrderDataLayout_yiJie = (SwipeRefreshLayout) mainViewPager.findViewWithTag("noDataLayout_yiJie");
        if (mainOrderSwipeRefresh_weiJie != null && noMainOrderDataLayout_weiJie != null &&
                mainOrderSwipeRefresh_yiJie != null && noMainOrderDataLayout_yiJie != null) {
            List<Order> weiJieOrderListNew = orderDataMap.get("weiJie");
            List<Order> yiJieOrderListNew = orderDataMap.get("yiJie");
            if (ComFun.strNull(weiJieOrderListNew, true)) {
                noMainOrderDataLayout_weiJie.setVisibility(View.GONE);
                mainOrderSwipeRefresh_weiJie.setVisibility(View.VISIBLE);
                LinearLayout mainOrderDataLayout = (LinearLayout) mainOrderSwipeRefresh_weiJie.findViewById(R.id.mainOrderDataLayout);
                mainOrderDataLayout.removeAllViews();
                MainOrderFragment.createMainOrderView(MainActivity.this, "weiJie", mainOrderDataLayout, weiJieOrderListNew);
            } else {
                mainOrderSwipeRefresh_weiJie.setVisibility(View.GONE);
                noMainOrderDataLayout_weiJie.setVisibility(View.VISIBLE);
            }
            if (ComFun.strNull(yiJieOrderListNew, true)) {
                noMainOrderDataLayout_yiJie.setVisibility(View.GONE);
                mainOrderSwipeRefresh_yiJie.setVisibility(View.VISIBLE);
                LinearLayout mainOrderDataLayout = (LinearLayout) mainOrderSwipeRefresh_yiJie.findViewById(R.id.mainOrderDataLayout);
                mainOrderDataLayout.removeAllViews();
                MainOrderFragment.createMainOrderView(MainActivity.this, "yiJie", mainOrderDataLayout, yiJieOrderListNew);
            } else {
                mainOrderSwipeRefresh_yiJie.setVisibility(View.GONE);
                noMainOrderDataLayout_yiJie.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_user_info:
                drawer.closeDrawer(GravityCompat.START);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toUserData();
                    }
                }, 200);
                break;
            case R.id.nav_has_order:
                drawer.closeDrawer(GravityCompat.START);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toHasReceiveOrder();
                    }
                }, 200);
                break;
            case R.id.nav_pay_img:
                drawer.closeDrawer(GravityCompat.START);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toPayDialogActivity();
                    }
                }, 200);
                break;
            case R.id.nav_open_give:
                toUserIfOpen();
                break;
            case R.id.nav_select_self_floor:
                drawer.closeDrawer(GravityCompat.START);
                toFloorChange();
                break;
            case R.id.nav_update:
                drawer.closeDrawer(GravityCompat.START);
                VersionUtil.checkNewVersion(MainActivity.this);
                break;
            case R.id.nav_exit:
                toUserLoginOut();
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.ACTION_DOWN:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    if (System.currentTimeMillis() - exitTime > 2000) {
                        ComFun.showToast(this, "再按一次离开", 2000);
                        exitTime = System.currentTimeMillis();
                    } else {
                        System.exit(0);
                    }
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                break;
        }

        return true;
    }

    /**
     * 跳转到支付二维码弹框页面
     */
    public void toPayDialogActivity() {
        Intent payDialogIntent = new Intent(MainActivity.this, PayDialogActivity.class);
        startActivity(payDialogIntent);
    }

    /**
     * 打开或关闭菜单
     */
    public void toggleLeftMenu() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    /**
     * 跳转到任务列表页面
     *
     * @param view
     */
    public void toTaskListActivity(View view) {
        Intent taskIntent = new Intent(MainActivity.this, TaskActivity.class);
        MainActivity.this.startActivity(taskIntent);
    }

    /**
     * 跳转到个人资料
     */
    public void toUserData() {
        Intent userDataIntent = new Intent(MainActivity.this, UserDataActivity.class);
        startActivity(userDataIntent);
    }

    /**
     * 跳转到已接订单
     */
    public void toHasReceiveOrder() {
        Intent hasReceiveOrderIntent = new Intent(MainActivity.this, OrderActivity.class);
        startActivity(hasReceiveOrderIntent);
    }

    /**
     * 用户退出登录
     */
    public void toUserLoginOut() {
        // 解除XPush绑定
        SharedPreferencesTool.addOrUpdate(MainActivity.this, "fyLoginUserInfo", "needLogin", true);
        // 清空后退栈
        ComFun.clearAllActiveActivity();
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    /**
     * 用户开启或关闭接单
     */
    public void toUserIfOpen() {
        final MenuItem navMenuItemOpenGive = navigationView.getMenu().findItem(R.id.nav_open_give);
        final String ifOpenFlag = navMenuItemOpenGive.getTitleCondensed().toString();
        ComFun.showLoading(MainActivity.this, "正在处理，请稍后");
        final String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userId");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        if (ifOpenFlag.equals("true")) {
            params.put("ifOpen", "0");
        } else {
            params.put("ifOpen", "1");
        }
        ConnectorInventory.setUserIfOpen(MainActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                if (ifOpenFlag.equals("true")) {
                    SharedPreferencesTool.addOrUpdate(MainActivity.this, "fyLoginUserInfo", "ifOpen", "0");
                    navMenuItemOpenGive.setTitleCondensed("false");
                    navMenuItemOpenGive.setTitle("点击开始接单");
                    ComFun.showToast(MainActivity.this, "成功停止接单啦", Toast.LENGTH_SHORT);
                } else {
                    SharedPreferencesTool.addOrUpdate(MainActivity.this, "fyLoginUserInfo", "ifOpen", "1");
                    navMenuItemOpenGive.setTitleCondensed("true");
                    navMenuItemOpenGive.setTitle("当前正在听单，点击停止");
                    ComFun.showToast(MainActivity.this, "成功开始接单啦", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (ifOpenFlag.equals("true")) {
                    ComFun.showToast(MainActivity.this, "停止接单失败，请稍后重试", Toast.LENGTH_SHORT);
                } else {
                    ComFun.showToast(MainActivity.this, "开启接单失败，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 切换接单楼层
     */
    public void toFloorChange() {
        String floorName = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "floorName");
        MenuItem navMenuItemFlooeChange = navigationView.getMenu().findItem(R.id.nav_select_self_floor);
        String ifFloorChange = navMenuItemFlooeChange.getTitleCondensed().toString();
        if (ifFloorChange.equals("all")) {
            SharedPreferencesTool.addOrUpdate(MainActivity.this, "fySet", "receiveSelfFloor", "self");
            navMenuItemFlooeChange.setTitleCondensed("self");
            navMenuItemFlooeChange.setTitle("楼层切换『 当前为：" + floorName + " 』");
            ComFun.showToast(MainActivity.this, "楼层已切换为：" + floorName, Toast.LENGTH_SHORT);
        } else {
            SharedPreferencesTool.addOrUpdate(MainActivity.this, "fySet", "receiveSelfFloor", "all");
            navMenuItemFlooeChange.setTitleCondensed("all");
            navMenuItemFlooeChange.setTitle("楼层切换『 当前为：所有 』");
            ComFun.showToast(MainActivity.this, "楼层已切换为：所有", Toast.LENGTH_SHORT);
        }
        // 刷新订单列表
        initDatas(false);
    }

    class mHandler extends Handler {
        public mHandler() {
        }

        public mHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            switch (msg.what) {
                case MSG_UPDATE_ORDER_STATE:
                    final String orderId = b.getString("orderId");
                    int orderState = b.getInt("orderState");
                    // 送货的  未接单 3 已接单 4
                    // 商家    未接单 2 已接单 3
                    if (orderState == 4) {
                        // 需要完成订单验证码
                        final EditText etCompCode = new EditText(MainActivity.this);
                        etCompCode.setPadding(DisplayUtil.dip2px(MainActivity.this, 30), DisplayUtil.dip2px(MainActivity.this, 16), DisplayUtil.dip2px(MainActivity.this, 30), DisplayUtil.dip2px(MainActivity.this, 16));
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("请输入订单验收码").setIcon(R.drawable.edit).setView(etCompCode).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ComFun.hideLoading();
                            }
                        });
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String compCode = etCompCode.getText().toString();
                                if (ComFun.strNull(compCode.trim())) {
                                    updateOrderState(orderId, compCode.trim());
                                } else {
                                    updateOrderState(orderId, "unCode");
                                }
                            }
                        });
                        builder.show();
                    } else {
                        updateOrderState(orderId, null);
                    }
                    break;
                case MSG_REF_ORDER_LSIT:
                    initDatas(true);
                    break;
                case MSG_CALL_USER_PHONE:
                    String userPhone = b.getString("userPhone");
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + userPhone));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainActivity.this.startActivity(callIntent);
                    } catch (SecurityException e) {
                        ComFun.showToast(MainActivity.this, "您可能没有提供拨打电话的权限，给小渔开放权限后再试试吧", Toast.LENGTH_LONG);
                    }
                    break;
                case MSG_START_DOWN_NEW_VERSION:
                    String appUrl = b.getString("appUrl");
                    // 弹框下载新版本
                    VersionUtil.versionDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogStyle).setCancelable(false).create();
                    VersionUtil.versionDialog.show();
                    Window win = VersionUtil.versionDialog.getWindow();
                    View downloadVersionView = MainActivity.this.getLayoutInflater().inflate(R.layout.download_version_dialog, null);
                    win.setContentView(downloadVersionView);
                    VersionUtil.downloadProgressBar = (HorizontalProgressbarWithProgress) downloadVersionView.findViewById(R.id.downloadProgressBar);
                    VersionUtil.addDownLoadHandler(MainActivity.this);
                    // 开启线程下载
                    VersionUtil.beginDownload(MainActivity.this, appUrl);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void updateOrderState(String orderId, String orderCode) {
        if (orderCode == null) {
            ComFun.showLoading(MainActivity.this, "操作处理中，请稍后");
        } else {
            if (!orderCode.equals("unCode")) {
                ComFun.showLoading(MainActivity.this, "操作处理中，请稍后");
            } else {
                ComFun.hideLoading();
            }
        }
        if (orderCode == null || (orderCode != null && !orderCode.equals("unCode"))) {
            final String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "fyLoginUserInfo", "userId");
            RequestParams params = new RequestParams();
            params.put("userId", userId);
            params.put("orderId", orderId);
            if (orderCode != null && !orderCode.equals("unCode")) {
                params.put("idenCode", orderCode);
            }
            ConnectorInventory.updateOrderState(MainActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
                @Override
                public void onFinish() {
                    ComFun.hideLoading();
                }

                @Override
                public void onSuccess(Object responseObj) {
                    try {
                        JSONObject resuleJson = new JSONObject(responseObj.toString());
                        String code = resuleJson.getString("code");
                        if (code.equals("ajaxSuccess")) {
                            ComFun.showToast(MainActivity.this, "操作成功", Toast.LENGTH_SHORT);
                            // 刷新订单列表
                            initDatas(false);
                        } else if (code.equals("ajaxNone")) {
                            ComFun.showToast(MainActivity.this, "接单失败，请稍后重试", Toast.LENGTH_SHORT);
                        } else if (code.equals("ajaxfailure")) {
                            ComFun.showToast(MainActivity.this, "对不起，该订单已被抢", Toast.LENGTH_SHORT);
                            // 刷新订单列表
                            initDatas(false);
                        } else if (code.equals("error")) {
                            ComFun.showToast(MainActivity.this, "验证码错误，请重新输入验证码", Toast.LENGTH_SHORT);
                        } else {
                            ComFun.showToast(MainActivity.this, "操作处理异常，请稍后重试", Toast.LENGTH_SHORT);
                        }
                    } catch (JSONException e) {
                    }
                }

                @Override
                public void onFailure(OkHttpException okHttpE) {
                    ComFun.showToast(MainActivity.this, "操作处理失败，请稍后重试", Toast.LENGTH_SHORT);
                }
            }));
        }
    }
}
