package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.fy.niu.fyreorder.customView.CircularImage;
import com.fy.niu.fyreorder.customView.SlideMenu;
import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.MainOrderFragment;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.util.ComFun;

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
        initDatas();

        mainIndicator.setVisibleTabCount(mTitles.size());
        mainIndicator.setTabItemTitles(mTitles);

        mainViewPager.setAdapter(mAdapter);
        mainIndicator.setViewPager(mainViewPager, 0);
    }

    private void initView() {
        leftMenu = (SlideMenu) findViewById(R.id.leftMenu);
        userHeadImg = (CircularImage) findViewById(R.id.userHeadImg);
        userHeadImg.setImageResource(R.drawable.default_user_head_1);

        mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mainIndicator = (ViewPagerIndicator) findViewById(R.id.mainIndicator);
    }

    private void initDatas() {
        // 请求数据库获取数据
        Map<String, List<Order>> orderDataMap = new LinkedHashMap<>();
        List<Order> weiJieOrderList = new ArrayList<>();
        List<Order> yiJieOrderList = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            Order order = new Order();
            order.setUserName("用户" + i);
            order.setUserPhone("213542336" + i);
            order.setSchool("魔鬼学院");
            order.setAddress("鬼楼 603室");
            order.setPayWay("微信支付");
            order.setOrderTime("2017-10-16 13:39");
            order.setRemark("哇哈哈哈");
            order.setState(2);
            List<Order.BuyContent> buyContentList = new ArrayList<>();
            for(int j = 0; j < 2; j++){
                Order.BuyContent buyContent = new Order.BuyContent();
                buyContent.setName("冰红茶");
                buyContent.setImgPath("https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=2821201063,1180291039&fm=117&gp=0.jpg");
                buyContent.setBuyCount(2);
                buyContent.setMoney("4");
                buyContentList.add(buyContent);
            }
            order.setOrderDetail(buyContentList);
            weiJieOrderList.add(order);
        }
        orderDataMap.put("weiJie", weiJieOrderList);
        orderDataMap.put("yiJie", yiJieOrderList);
        for(Map.Entry<String, List<Order>> orderMap : orderDataMap.entrySet()){
            MainOrderFragment fragment = MainOrderFragment.newInstance(orderMap.getValue());
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                ComFun.showToast(this, "再按一次离开", 2000);
                exitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
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
        // 清空后退栈
        ComFun.clearAllActiveActivity();
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
