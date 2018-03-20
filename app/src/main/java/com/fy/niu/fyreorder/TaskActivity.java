package com.fy.niu.fyreorder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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

import com.fy.niu.fyreorder.customView.ViewPagerIndicator;
import com.fy.niu.fyreorder.fragment.HasTaskFragment;
import com.fy.niu.fyreorder.model.Task;
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

public class TaskActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_REF_TASK_LSIT = 1;
    public static final int MSG_GET_TASK = 2;
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

        mHandler = new TaskActivity.mHandler();

        setupActionBar();

        initView();
        initDatas(false);

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

        Map<String, List<Task>> hasTaskDataMap = new LinkedHashMap<>();
        hasTaskDataMap.put("weiJie", new ArrayList<Task>());
        hasTaskDataMap.put("yiJie", new ArrayList<Task>());
        hasTaskDataMap.put("finished", new ArrayList<Task>());

        for (Map.Entry<String, List<Task>> hasTaskMap : hasTaskDataMap.entrySet()) {
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

    private void initDatas(final boolean isRefFlag) {
        ComFun.showLoading(TaskActivity.this, "正在获取任务列表，请稍后");
        String userId = UserDataUtil.getUserId(TaskActivity.this);
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        ConnectorInventory.getTaskList(TaskActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
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
                    Log.d(" ==== 任务列表数据 === ", " ===> " + dataList);
                    if (dataList.length() > 0) {
                        Map<String, List<Task>> hasTaskDataMap = getTaskListFromJson(dataList);
                        // 刷新布局
                        refTaskListView(hasTaskDataMap);
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                Log.d(" ==== 获取任务数据error === ", " ===> " + okHttpE);
                ComFun.showToast(TaskActivity.this, "获取任务数据异常，请稍后重试", Toast.LENGTH_LONG);
            }
        }));
    }

    private void hideRefLayout() {
        SwipeRefreshLayout mainTaskSwipeRefresh_weiJie = (SwipeRefreshLayout) taskViewPager.findViewWithTag("taskSwipeRefresh_weiJie");
        SwipeRefreshLayout noMainTaskDataLayout_weiJie = (SwipeRefreshLayout) taskViewPager.findViewWithTag("noDataLayout_weiJie");
        SwipeRefreshLayout mainTaskSwipeRefresh_yiJie = (SwipeRefreshLayout) taskViewPager.findViewWithTag("taskSwipeRefresh_yiJie");
        SwipeRefreshLayout noMainTaskDataLayout_yiJie = (SwipeRefreshLayout) taskViewPager.findViewWithTag("noDataLayout_yiJie");
        SwipeRefreshLayout mainTaskSwipeRefresh_finished = (SwipeRefreshLayout) taskViewPager.findViewWithTag("taskSwipeRefresh_finished");
        SwipeRefreshLayout noMainTaskDataLayout_finished = (SwipeRefreshLayout) taskViewPager.findViewWithTag("noDataLayout_finished");
        mainTaskSwipeRefresh_weiJie.setRefreshing(false);
        noMainTaskDataLayout_weiJie.setRefreshing(false);
        mainTaskSwipeRefresh_yiJie.setRefreshing(false);
        noMainTaskDataLayout_yiJie.setRefreshing(false);
        mainTaskSwipeRefresh_finished.setRefreshing(false);
        noMainTaskDataLayout_finished.setRefreshing(false);
    }

    private Map<String, List<Task>> getTaskListFromJson(JSONArray dataList) {
        Map<String, List<Task>> hasTaskDataMap = new LinkedHashMap<>();
        List<Task> weiJie = new ArrayList<>();
        List<Task> yiJie = new ArrayList<>();
        List<Task> finished = new ArrayList<>();

        for (int i = 0; i < dataList.length(); i++) {
            try {
                JSONObject taskJson = dataList.getJSONObject(i);
                if (taskJson.has("id")) {
                    String id = taskJson.getString("id");
                    if (ComFun.strNull(id) && !id.equals("null")) {
                        String date = taskJson.getString("date");
                        String name = taskJson.getString("name");
                        String content = taskJson.has("content") ? taskJson.getString("content") : "";
                        Task task = new Task();
                        task.setId(id);
                        task.setDate(date);
                        task.setName(name);
                        task.setContent(content);
                        weiJie.add(task);
                    }
                } else if (taskJson.has("idOnder")) {
                    String idOnder = taskJson.getString("idOnder");
                    if (ComFun.strNull(idOnder) && !idOnder.equals("null")) {
                        String dateOnder = taskJson.getString("dateOnder");
                        String nameOnder = taskJson.getString("nameOnder");
                        String contentOnder = taskJson.has("contentOnder") ? taskJson.getString("contentOnder") : "";
                        Task task = new Task();
                        task.setId(idOnder);
                        task.setDate(dateOnder);
                        task.setName(nameOnder);
                        task.setContent(contentOnder);
                        yiJie.add(task);
                    }
                } else if (taskJson.has("idFin")) {
                    String idFin = taskJson.getString("idFin");
                    if (ComFun.strNull(idFin) && !idFin.equals("null")) {
                        String dateFin = taskJson.getString("dateFin");
                        String nameFin = taskJson.getString("nameFin");
                        String contentFin = taskJson.has("contentFin") ? taskJson.getString("contentFin") : "";
                        Task task = new Task();
                        task.setId(idFin);
                        task.setDate(dateFin);
                        task.setName(nameFin);
                        task.setContent(contentFin);
                        finished.add(task);
                    }
                }
            } catch (JSONException e) {
            }
        }

        hasTaskDataMap.put("weiJie", weiJie);
        hasTaskDataMap.put("yiJie", yiJie);
        hasTaskDataMap.put("finished", finished);
        return hasTaskDataMap;
    }

    private void refTaskListView(Map<String, List<Task>> hasTaskDataMap) {
        for (Map.Entry<String, List<Task>> map : hasTaskDataMap.entrySet()) {
            SwipeRefreshLayout mainTaskSwipeRefresh = (SwipeRefreshLayout) taskViewPager.findViewWithTag("taskSwipeRefresh_" + map.getKey());
            SwipeRefreshLayout noMainTaskDataLayout = (SwipeRefreshLayout) taskViewPager.findViewWithTag("noDataLayout_" + map.getKey());
            if (map.getValue().size() > 0) {
                noMainTaskDataLayout.setVisibility(View.GONE);
                mainTaskSwipeRefresh.setVisibility(View.VISIBLE);
                LinearLayout mainTaskDataLayout = (LinearLayout) mainTaskSwipeRefresh.findViewById(R.id.mainTaskDataLayout);
                HasTaskFragment.createMainTaskView(TaskActivity.this, map.getKey(), mainTaskDataLayout, map.getValue());
            } else {
                mainTaskSwipeRefresh.setVisibility(View.GONE);
                noMainTaskDataLayout.setVisibility(View.VISIBLE);
            }
        }
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
                case MSG_REF_TASK_LSIT:
                    initDatas(true);
                    break;
                case MSG_GET_TASK:
                    ComFun.showLoading(TaskActivity.this, "正在领取任务，请稍后");
                    String userId = UserDataUtil.getUserId(TaskActivity.this);
                    String taskId = b.getString("taskId");
                    RequestParams params = new RequestParams();
                    params.put("id", taskId);
                    params.put("userId", userId);
                    ConnectorInventory.updateTaskState(TaskActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
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
                                    ComFun.showToast(TaskActivity.this, "任务领取成功", Toast.LENGTH_SHORT);
                                    // 刷新订单列表
                                    initDatas(false);
                                } else {
                                    ComFun.showToast(TaskActivity.this, "任务领取失败，请稍后重试", Toast.LENGTH_SHORT);
                                }
                            } catch (JSONException e) {
                            }
                        }

                        @Override
                        public void onFailure(OkHttpException okHttpE) {
                            Log.d(" ==== 领取任务error === ", " ===> " + okHttpE);
                            ComFun.showToast(TaskActivity.this, "领取任务异常，请稍后重试", Toast.LENGTH_LONG);
                        }
                    }));
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
