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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.customView.CircularImage;
import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.MainOrderFragment;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.util.ComFun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private long exitTime;
    private LinearLayout mainTopLayout; // 主页个人中心按钮

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
        mainUserHeadImg.setOnClickListener(this);

        initView();
        initDatas();

        mainIndicator.setVisibleTabCount(mTitles.size());
        mainIndicator.setTabItemTitles(mTitles);

        mainViewPager.setAdapter(mAdapter);
        mainIndicator.setViewPager(mainViewPager, 0);
    }

    private void initView() {
        mainTopLayout = (LinearLayout) findViewById(R.id.mainTopLayout);
        mainTopLayout.setOnClickListener(this);
        mainTopLayout.setVisibility(View.GONE);

        mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mainIndicator = (ViewPagerIndicator) findViewById(R.id.mainIndicator);
    }

    private void initDatas() {
        // 请求数据库获取数据
        Map<String, List<Order>> orderDataMap = new LinkedHashMap<>();
        List<Order> weiJieOrderList = new ArrayList<>();
        List<Order> yiJieOrderList = new ArrayList<>();
        for(int i = 0; i < 2; i++){
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
    public void onClick(View v) {
        if(v.getId() == R.id.mainTopLayout){
            // 点击主页个人中心按钮
            Intent userCenterIntent = new Intent(MainActivity.this, UserCenterActivity.class);
            startActivity(userCenterIntent);
        }else if(v.getId() == R.id.mainUserHeadImg){
            // 点击主页个人头像按钮
            Intent userCenterIntent = new Intent(MainActivity.this, UserCenterActivity.class);
            startActivity(userCenterIntent);
        }
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
}
