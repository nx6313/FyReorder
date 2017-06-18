package com.fy.niu.fyreorder.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.customView.ElasticScrollView;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.util.DisplayUtil;
import com.fy.niu.fyreorder.util.DrawableManager;
import com.fy.niu.fyreorder.util.SerializableOrderList;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 25596 on 2017/4/17.
 */

public class MainOrderFragment extends Fragment {
    private List<Order> mOrderDataList;
    public static final String BUNDLE_DATA_LIST = "orderDataList";
    public DrawableManager drawableManager = new DrawableManager();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null){
            Serializable orderDataSerializable = bundle.getSerializable(BUNDLE_DATA_LIST);
            mOrderDataList = ((SerializableOrderList) orderDataSerializable).getList();
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

    private void createMainOrderView(LinearLayout mainOrderDataLayout, List<Order> mOrderDataList) {
        for(Order orderData : mOrderDataList){
            LinearLayout receivedOrderLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams receivedOrderLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            receivedOrderLayoutLp.setMargins(DisplayUtil.dip2px(getActivity(), 10), 0, DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 5));
            receivedOrderLayout.setLayoutParams(receivedOrderLayoutLp);
            receivedOrderLayout.setBackgroundResource(R.drawable.order_card_bg);
            receivedOrderLayout.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10));
            receivedOrderLayout.setOrientation(LinearLayout.VERTICAL);

            // 添加姓名、手机号
            LinearLayout line1 = new LinearLayout(getActivity());
            LinearLayout.LayoutParams line1Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            line1.setLayoutParams(line1Lp);
            line1.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10));
            line1.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvUserName = new TextView(getActivity());
            LinearLayout.LayoutParams tvUserNameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvUserName.setLayoutParams(tvUserNameLp);
            tvUserName.setGravity(Gravity.LEFT);
            tvUserName.setText("客户名称：" + orderData.getUserName());
            line1.addView(tvUserName);

            TextView tvUserPhone = new TextView(getActivity());
            LinearLayout.LayoutParams tvUserPhoneLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            tvUserPhone.setLayoutParams(tvUserPhoneLp);
            tvUserPhone.setGravity(Gravity.RIGHT);
            tvUserPhone.setText("联系电话：" + orderData.getUserPhone());
            line1.addView(tvUserPhone);

            receivedOrderLayout.addView(line1);

            // 添加学校、详细地址
            LinearLayout line2 = new LinearLayout(getActivity());
            LinearLayout.LayoutParams line2Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            line2.setLayoutParams(line2Lp);
            line2.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10));
            line2.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvSchool = new TextView(getActivity());
            LinearLayout.LayoutParams tvSchoolLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvSchool.setLayoutParams(tvSchoolLp);
            tvSchool.setGravity(Gravity.LEFT);
            tvSchool.setText("学校：" + orderData.getSchool());
            line2.addView(tvSchool);

            TextView tvAddress = new TextView(getActivity());
            LinearLayout.LayoutParams tvAddressLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            tvAddress.setLayoutParams(tvAddressLp);
            tvAddress.setGravity(Gravity.RIGHT);
            tvAddress.setText("详细地址：" + orderData.getAddress());
            line2.addView(tvAddress);

            receivedOrderLayout.addView(line2);

            // 添加支付方式、下单时间
            LinearLayout line3 = new LinearLayout(getActivity());
            LinearLayout.LayoutParams line3Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            line3.setLayoutParams(line3Lp);
            line3.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10));
            line3.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvPayWay = new TextView(getActivity());
            LinearLayout.LayoutParams tvPayWayLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvPayWay.setLayoutParams(tvPayWayLp);
            tvPayWay.setGravity(Gravity.LEFT);
            tvPayWay.setText("支付方式：" + orderData.getPayWay());
            line3.addView(tvPayWay);

            TextView tvOrderTime = new TextView(getActivity());
            LinearLayout.LayoutParams tvOrderTimeLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            tvOrderTime.setLayoutParams(tvOrderTimeLp);
            tvOrderTime.setGravity(Gravity.RIGHT);
            tvOrderTime.setText("下单时间：" + orderData.getOrderTime());
            line3.addView(tvOrderTime);

            receivedOrderLayout.addView(line3);

            // 添加备注
            LinearLayout line4 = new LinearLayout(getActivity());
            LinearLayout.LayoutParams line4Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            line4.setLayoutParams(line4Lp);
            line4.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10));
            line4.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvRemark = new TextView(getActivity());
            LinearLayout.LayoutParams tvRemarkLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvRemark.setLayoutParams(tvRemarkLp);
            tvRemark.setGravity(Gravity.LEFT);
            tvRemark.setText("给商家留言：" + orderData.getRemark());
            line4.addView(tvRemark);

            receivedOrderLayout.addView(line4);

            // 添加购买详情信息部分
            if(orderData.getOrderDetail().size() > 0){
                View line = new View(getActivity());
                line.setBackgroundColor(Color.parseColor("#242424"));
                LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(getActivity(), 1));
                line.setLayoutParams(lineLp);
                receivedOrderLayout.addView(line);
                for(Order.BuyContent buyContent : orderData.getOrderDetail()){
                    LinearLayout line5 = new LinearLayout(getActivity());
                    LinearLayout.LayoutParams line5Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    line5.setLayoutParams(line5Lp);
                    line5.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 10));
                    line5.setOrientation(LinearLayout.HORIZONTAL);

                    ImageView imgShow = new ImageView(getActivity());
                    LinearLayout.LayoutParams imgShowLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(getActivity(), 80), DisplayUtil.dip2px(getActivity(), 80));
                    imgShow.setLayoutParams(imgShowLp);
                    drawableManager.fetchDrawableOnThread(buyContent.getImgPath(), imgShow);
                    line5.addView(imgShow);

                    receivedOrderLayout.addView(line5);
                }
            }

            mainOrderDataLayout.addView(receivedOrderLayout);
        }
    }

    public static MainOrderFragment newInstance(List<Order> orderData){
        Bundle bundle = new Bundle();
        SerializableOrderList orderDataList = new SerializableOrderList();
        orderDataList.setList(orderData);
        bundle.putSerializable(BUNDLE_DATA_LIST, orderDataList);

        MainOrderFragment fragment = new MainOrderFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
