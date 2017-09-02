package com.fy.niu.fyreorder.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fy.niu.fyreorder.OrderActivity;
import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.util.SerializableObjectList;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by 25596 on 2017/4/17.
 */

public class HasOrderFragment extends Fragment {
    private String hasOrderDataType;
    private List<Object> mHasOrderDataList;
    public static final String BUNDLE_DATA_TYPE = "hasOrderDataType";
    public static final String BUNDLE_DATA_LIST = "hasOrderDataList";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String userType = SharedPreferencesTool.getFromShared(getActivity(), "fyLoginUserInfo", "ifGive");
        Bundle bundle = getArguments();
        if(bundle != null){
            hasOrderDataType = bundle.getString(BUNDLE_DATA_TYPE);
            Serializable orderDataSerializable = bundle.getSerializable(BUNDLE_DATA_LIST);
            mHasOrderDataList = ((SerializableObjectList) orderDataSerializable).getList();
        }

        View view = inflater.inflate(R.layout.has_order_fragment, null);
        view.setTag(hasOrderDataType);
        TextView orderNumLabelTv = (TextView) view.findViewById(R.id.orderNumLabelTv);
        TextView orderNumTv = (TextView) view.findViewById(R.id.orderNumTv);
        TextView orderMoneyLabelTv = (TextView) view.findViewById(R.id.orderMoneyLabelTv);
        TextView orderMoneyTv = (TextView) view.findViewById(R.id.orderMoneyTv);
        if(hasOrderDataType.equals("today")){
            if(!userType.equals("0")){
                orderNumLabelTv.setText("日订单数");
                orderMoneyLabelTv.setText("日所得");
            }else{
                orderNumLabelTv.setText("日接单数");
                orderMoneyLabelTv.setText("日工资");
            }
            orderNumTv.setTag("tvDayNum");
            orderMoneyTv.setTag("tvDayCharge");
        }else if(hasOrderDataType.equals("lastWeek")){
            if(!userType.equals("0")){
                orderNumLabelTv.setText("周订单数");
                orderMoneyLabelTv.setText("周所得");
            }else{
                orderNumLabelTv.setText("周接单数");
                orderMoneyLabelTv.setText("周工资");
            }
            orderNumTv.setTag("tvWeekNum");
            orderMoneyTv.setTag("tvWeekCharge");
        }else if(hasOrderDataType.equals("lastMonth")){
            if(!userType.equals("0")){
                orderNumLabelTv.setText("月订单数");
                orderMoneyLabelTv.setText("月所得");
            }else{
                orderNumLabelTv.setText("月接单数");
                orderMoneyLabelTv.setText("月工资");
            }
            orderNumTv.setTag("tvMonthNum");
            orderMoneyTv.setTag("tvMonthCharge");
        }
        orderNumTv.setText(String.valueOf(mHasOrderDataList.get(0)));
        orderMoneyTv.setText("￥" + String.valueOf(new BigDecimal(Double.parseDouble(String.valueOf(mHasOrderDataList.get(1))))));

        // 设置字体
        Typeface faceLabel = Typeface.createFromAsset(getContext().getAssets(), "fonts/nsjmmt.ttf");
        orderNumLabelTv.setTypeface(faceLabel);
        orderMoneyLabelTv.setTypeface(faceLabel);
        Typeface faceMoney = Typeface.createFromAsset(getContext().getAssets(), "fonts/font_309.ttf");
        orderNumTv.setTypeface(faceMoney);
        orderMoneyTv.setTypeface(faceMoney);

        return view;
    }

    public static HasOrderFragment newInstance(String hasOrderDataType, List<Object> hasOrderData){
        Bundle bundle = new Bundle();
        SerializableObjectList hasOrderDataList = new SerializableObjectList();
        hasOrderDataList.setList(hasOrderData);
        bundle.putString(BUNDLE_DATA_TYPE, hasOrderDataType);
        bundle.putSerializable(BUNDLE_DATA_LIST, hasOrderDataList);

        HasOrderFragment fragment = new HasOrderFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
