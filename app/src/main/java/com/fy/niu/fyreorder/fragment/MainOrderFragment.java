package com.fy.niu.fyreorder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.customView.ElasticScrollView;
import com.fy.niu.fyreorder.util.SerializableList;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 25596 on 2017/4/17.
 */

public class MainOrderFragment extends Fragment {
    private List<String> mOrderDataList;
    public static final String BUNDLE_DATA_LIST = "title";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null){
            Serializable orderDataSerializable = bundle.getSerializable(BUNDLE_DATA_LIST);
            mOrderDataList = ((SerializableList) orderDataSerializable).getList();
        }

        View view = inflater.inflate(R.layout.main_order_fragment, null);
        ElasticScrollView mainOrderScrollView = (ElasticScrollView) view.findViewById(R.id.mainOrderScrollView);
        LinearLayout noMainOrderDataLayout = (LinearLayout) view.findViewById(R.id.noMainOrderDataLayout);
        if(mOrderDataList != null && mOrderDataList.size() > 0){
            noMainOrderDataLayout.setVisibility(View.GONE);
            mainOrderScrollView.setVisibility(View.VISIBLE);
            LinearLayout mainOrderDataLayout = (LinearLayout) view.findViewById(R.id.mainOrderDataLayout);
            mainOrderDataLayout.removeAllViews();
            // 生成订单数据
            createMainOrderView(mainOrderDataLayout, mOrderDataList);
        }else{
            mainOrderScrollView.setVisibility(View.GONE);
            noMainOrderDataLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    private void createMainOrderView(LinearLayout mainOrderDataLayout, List<String> mOrderDataList) {
        for(String orderData : mOrderDataList){
            TextView tv = new TextView(getActivity());
            tv.setGravity(Gravity.CENTER);
            tv.setText(orderData);

            mainOrderDataLayout.addView(tv);
        }
    }

    public static MainOrderFragment newInstance(List<String> orderData){
        Bundle bundle = new Bundle();
        SerializableList orderDataList = new SerializableList();
        orderDataList.setList(orderData);
        bundle.putSerializable(BUNDLE_DATA_LIST, orderDataList);

        MainOrderFragment fragment = new MainOrderFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
