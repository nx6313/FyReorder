package com.fy.niu.fyreorder.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.util.SerializableObjectList;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by 25596 on 2017/4/17.
 */

public class HasTaskFragment extends Fragment {
    private String hasTaskDataType;
    private List<Object> mHasTaskDataList;
    public static final String BUNDLE_DATA_TYPE = "hasTaskDataType";
    public static final String BUNDLE_DATA_LIST = "hasTaskDataList";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null){
            hasTaskDataType = bundle.getString(BUNDLE_DATA_TYPE);
            Serializable orderDataSerializable = bundle.getSerializable(BUNDLE_DATA_LIST);
            mHasTaskDataList = ((SerializableObjectList) orderDataSerializable).getList();
        }

        View view = inflater.inflate(R.layout.has_task_fragment, null);
        view.setTag(hasTaskDataType);

        return view;
    }

    public static HasTaskFragment newInstance(String hasTaskDataType, List<Object> hasTaskData){
        Bundle bundle = new Bundle();
        SerializableObjectList hasTaskDataList = new SerializableObjectList();
        hasTaskDataList.setList(hasTaskData);
        bundle.putString(BUNDLE_DATA_TYPE, hasTaskDataType);
        bundle.putSerializable(BUNDLE_DATA_LIST, hasTaskDataList);

        HasTaskFragment fragment = new HasTaskFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
