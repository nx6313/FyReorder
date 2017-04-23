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

import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.HasOrderFragment;
import com.fy.niu.fyreorder.util.ComFun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {
    private ViewPager orderViewPager;
    private ViewPagerIndicator orderIndicator;

    private List<String> mTitles = Arrays.asList("今日接单", "上周接单", "上月接单");
    private List<HasOrderFragment> mContents = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;

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

        initView();
        initDatas();

        orderIndicator.setVisibleTabCount(mTitles.size());
        orderIndicator.setTabItemTitles(mTitles);

        orderViewPager.setAdapter(mAdapter);
        orderIndicator.setViewPager(orderViewPager, 0);
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
    }

    private void initDatas() {
        Map<String, List<Object>> hasOrderDataMap = new LinkedHashMap<>();
        List<Object> todayHasOrderList = new ArrayList<>();
        todayHasOrderList.add(14);
        todayHasOrderList.add(50);
        List<Object> lastWeekHasOrderList = new ArrayList<>();
        lastWeekHasOrderList.add(36);
        lastWeekHasOrderList.add(75);
        List<Object> lastMonthHasOrderList = new ArrayList<>();
        lastMonthHasOrderList.add(68);
        lastMonthHasOrderList.add(158);
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

}
