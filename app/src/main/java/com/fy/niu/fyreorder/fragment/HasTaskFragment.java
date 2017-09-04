package com.fy.niu.fyreorder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.TaskActivity;
import com.fy.niu.fyreorder.model.Task;
import com.fy.niu.fyreorder.util.DateFormatUtil;
import com.fy.niu.fyreorder.util.SerializableTaskList;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by 25596 on 2017/4/17.
 */

public class HasTaskFragment extends Fragment {
    private String hasTaskDataType;
    private List<Task> mHasTaskDataList;
    public static final String BUNDLE_DATA_TYPE = "hasTaskDataType";
    public static final String BUNDLE_DATA_LIST = "hasTaskDataList";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            hasTaskDataType = bundle.getString(BUNDLE_DATA_TYPE);
            Serializable taskDataSerializable = bundle.getSerializable(BUNDLE_DATA_LIST);
            mHasTaskDataList = ((SerializableTaskList) taskDataSerializable).getList();
        }

        View view = inflater.inflate(R.layout.has_task_fragment, null);
        SwipeRefreshLayout mainTaskSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.mainTaskSwipeRefresh);
        SwipeRefreshLayout noMainTaskDataLayout = (SwipeRefreshLayout) view.findViewById(R.id.noMainTaskDataLayout);
        mainTaskSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 发送刷新订单Handler
                Message msg = new Message();
                msg.what = TaskActivity.MSG_REF_TASK_LSIT;
                TaskActivity.mHandler.sendMessage(msg);
            }
        });
        noMainTaskDataLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 发送刷新订单Handler
                Message msg = new Message();
                msg.what = TaskActivity.MSG_REF_TASK_LSIT;
                TaskActivity.mHandler.sendMessage(msg);
            }
        });
        mainTaskSwipeRefresh.setTag("taskSwipeRefresh_" + hasTaskDataType);
        noMainTaskDataLayout.setTag("noDataLayout_" + hasTaskDataType);
        if (mHasTaskDataList != null && mHasTaskDataList.size() > 0) {
            noMainTaskDataLayout.setVisibility(View.GONE);
            mainTaskSwipeRefresh.setVisibility(View.VISIBLE);
            LinearLayout mainTaskDataLayout = (LinearLayout) view.findViewById(R.id.mainTaskDataLayout);
            createMainTaskView(getActivity(), hasTaskDataType, mainTaskDataLayout, mHasTaskDataList);
        } else {
            mainTaskSwipeRefresh.setVisibility(View.GONE);
            noMainTaskDataLayout.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public static void createMainTaskView(final Context context, String pageType, LinearLayout mainTaskDataLayout, List<Task> mTaskDataList) {
        mainTaskDataLayout.removeAllViews();
        for (final Task taskData : mTaskDataList) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View taskItemView = inflater.inflate(R.layout.task_fragment_item, null);

            TextView taskItemName = (TextView) taskItemView.findViewWithTag("task_item_name");
            TextView taskItemDate = (TextView) taskItemView.findViewWithTag("task_item_date");
            TextView taskItemContent = (TextView) taskItemView.findViewWithTag("task_item_content");
            LinearLayout taskItemGetBtn = (LinearLayout) taskItemView.findViewWithTag("task_item_get_btn");

            taskItemName.setText(taskData.getName());
            taskItemDate.setText("发布日期：" + DateFormatUtil.dateToStr(new Date(Long.parseLong(taskData.getDate())), DateFormatUtil.MMDDHHMM));
            taskItemContent.setText(taskData.getContent());
            if (pageType.equals("weiJie")) {
                taskItemGetBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 发送刷新订单Handler
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        msg.what = TaskActivity.MSG_GET_TASK;
                        data.putString("taskId", taskData.getId());
                        msg.setData(data);
                        TaskActivity.mHandler.sendMessage(msg);
                    }
                });
            } else {
                taskItemGetBtn.setVisibility(View.GONE);
            }

            mainTaskDataLayout.addView(taskItemView);
        }
    }

    public static HasTaskFragment newInstance(String hasTaskDataType, List<Task> hasTaskData) {
        Bundle bundle = new Bundle();
        SerializableTaskList hasTaskDataList = new SerializableTaskList();
        hasTaskDataList.setList(hasTaskData);
        bundle.putString(BUNDLE_DATA_TYPE, hasTaskDataType);
        bundle.putSerializable(BUNDLE_DATA_LIST, hasTaskDataList);

        HasTaskFragment fragment = new HasTaskFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
