package com.fy.niu.fyreorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.HasOrderFragment;
import com.fy.niu.fyreorder.fragment.HasTaskFragment;
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {
    private ViewPager taskViewPager;
    private ViewPagerIndicator taskIndicator;

    private List<String> mTitles = Arrays.asList("未接任务", "已接任务", "我完成的任务");
    private List<HasTaskFragment> mContents = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(TaskActivity.this);

        // 处理为标题居中
        ((TextView) toolbar.findViewById(R.id.toolbarTitleTv)).setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupActionBar();

        initView();
        initDatas();

        taskIndicator.setVisibleTabCount(mTitles.size());
        taskIndicator.setTabItemTitles(mTitles);

        taskViewPager.setAdapter(mAdapter);
        taskIndicator.setViewPager(taskViewPager, 0);
        taskViewPager.setOffscreenPageLimit(mTitles.size());
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
            TaskActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        taskViewPager = (ViewPager) findViewById(R.id.taskViewPager);
        taskIndicator = (ViewPagerIndicator) findViewById(R.id.taskIndicator);
    }

    private void initDatas() {
        ComFun.showLoading(TaskActivity.this, "正在获取任务列表，请稍后");
        String userId = SharedPreferencesTool.getFromShared(TaskActivity.this, "fyLoginUserInfo", "userId");
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        ConnectorInventory.getTaskList(TaskActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject orderDataJson = new JSONObject(responseObj.toString());
                    JSONArray dataList = orderDataJson.getJSONArray("dataList");
                    Log.d(" ==== 任务列表数据 === ", " ===> " + dataList);
                    if(dataList.length() > 0){

                    }else{

                    }
                } catch (JSONException e) {}
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                Log.d(" ==== 获取任务数据error === ", " ===> " + okHttpE);
            }
        }));

        Map<String, List<Object>> hasTaskDataMap = new LinkedHashMap<>();
        List<Object> todayHasOrderList = new ArrayList<>();
        todayHasOrderList.add(14);
        todayHasOrderList.add(50);
        List<Object> lastWeekHasOrderList = new ArrayList<>();
        lastWeekHasOrderList.add(36);
        lastWeekHasOrderList.add(75);
        List<Object> lastMonthHasOrderList = new ArrayList<>();
        lastMonthHasOrderList.add(68);
        lastMonthHasOrderList.add(158);
        hasTaskDataMap.put("today", todayHasOrderList);
        hasTaskDataMap.put("lastWeek", lastWeekHasOrderList);
        hasTaskDataMap.put("lastMonth", lastMonthHasOrderList);
        for(Map.Entry<String, List<Object>> hasTaskMap : hasTaskDataMap.entrySet()){
            HasTaskFragment fragment = HasTaskFragment.newInstance(hasTaskMap.getKey(), hasTaskMap.getValue());
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
