package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.MainOrderFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mainPageOrderBt; // 主页订单按钮
    private Button mainPageUserCenterBt; // 主页个人中心按钮

    private ViewPager mainViewPager;
    private ViewPagerIndicator mainIndicator;

    private List<String> mTitles = Arrays.asList("未接订单", "已接订单");
    private List<MainOrderFragment> mContents = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
        initDatas();

        mainIndicator.setVisibleTabCount(mTitles.size());
        mainIndicator.setTabItemTitles(mTitles);

        mainViewPager.setAdapter(mAdapter);
        mainIndicator.setViewPager(mainViewPager, 0);
    }

    private void initView() {
        mainPageOrderBt = (Button) findViewById(R.id.mainPageOrderBt);
        mainPageUserCenterBt = (Button) findViewById(R.id.mainPageUserCenterBt);
        mainPageOrderBt.setOnClickListener(this);
        mainPageUserCenterBt.setOnClickListener(this);

        mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mainIndicator = (ViewPagerIndicator) findViewById(R.id.mainIndicator);
    }

    private void initDatas() {
        for(String title : mTitles){
            MainOrderFragment fragment = MainOrderFragment.newInstance(title);
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
        if(v.getId() == R.id.mainPageOrderBt){
            // 点击主页订单按钮
            Intent orderIntent = new Intent(MainActivity.this, OrderActivity.class);
            startActivity(orderIntent);
        }else if(v.getId() == R.id.mainPageUserCenterBt){
            // 点击主页个人中心按钮
            Intent userCenterIntent = new Intent(MainActivity.this, UserCenterActivity.class);
            startActivity(userCenterIntent);
        }
    }
}
