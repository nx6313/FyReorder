package com.fy.niu.fyreorder.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.LoginActivity;
import com.fy.niu.fyreorder.MainActivity;
import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.customView.ElasticScrollView;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.DateFormatUtil;
import com.fy.niu.fyreorder.util.DisplayUtil;
import com.fy.niu.fyreorder.util.DrawableManager;
import com.fy.niu.fyreorder.util.SerializableOrderList;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by 25596 on 2017/4/17.
 */

public class MainOrderFragment extends Fragment {
    private String pageType;
    private List<Order> mOrderDataList;
    public static final String BUNDLE_DATA_TYPE = "orderDataType";
    public static final String BUNDLE_DATA_LIST = "orderDataList";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            pageType = bundle.getString(BUNDLE_DATA_TYPE);
            Serializable orderDataSerializable = bundle.getSerializable(BUNDLE_DATA_LIST);
            mOrderDataList = ((SerializableOrderList) orderDataSerializable).getList();
        }

        View view = inflater.inflate(R.layout.main_order_fragment, null);
        SwipeRefreshLayout mainOrderSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.mainOrderSwipeRefresh);
        SwipeRefreshLayout noMainOrderDataLayout = (SwipeRefreshLayout) view.findViewById(R.id.noMainOrderDataLayout);
        mainOrderSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 发送刷新订单Handler
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MainActivity.MSG_REF_ORDER_LSIT;
                msg.setData(data);
                MainActivity.mHandler.sendMessage(msg);
            }
        });
        noMainOrderDataLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 发送刷新订单Handler
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MainActivity.MSG_REF_ORDER_LSIT;
                msg.setData(data);
                MainActivity.mHandler.sendMessage(msg);
            }
        });
        mainOrderSwipeRefresh.setTag("orderSwipeRefresh_" + pageType);
        noMainOrderDataLayout.setTag("noDataLayout_" + pageType);
        if (mOrderDataList != null && mOrderDataList.size() > 0) {
            noMainOrderDataLayout.setVisibility(View.GONE);
            mainOrderSwipeRefresh.setVisibility(View.VISIBLE);
            LinearLayout mainOrderDataLayout = (LinearLayout) view.findViewById(R.id.mainOrderDataLayout);
            mainOrderDataLayout.removeAllViews();
            // 生成订单数据
            createMainOrderView(getActivity(), mainOrderDataLayout, mOrderDataList);
        } else {
            mainOrderSwipeRefresh.setVisibility(View.GONE);
            noMainOrderDataLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    public static void createMainOrderView(Context context, LinearLayout mainOrderDataLayout, List<Order> mOrderDataList) {
        // 0学生  1商超   2外卖
        // 送货的  未接单 3 已接单 4
        // 商家    未接单 2 已接单 3
        // 学生未接 userType.equals("0") && orderData.getOrderState() == 3
        // 学生已接 userType.equals("0") && orderData.getOrderState() == 4
        // 商家未接 !userType.equals("0") && orderData.getOrderState() == 2
        // 商家已接 !userType.equals("0") && orderData.getOrderState() == 3
        final String userType = SharedPreferencesTool.getFromShared(context, "fyLoginUserInfo", "ifGive");
        DrawableManager drawableManager = new DrawableManager();
        for (final Order orderData : mOrderDataList) {
            LinearLayout receivedOrderLayout = new LinearLayout(context);
            LinearLayout.LayoutParams receivedOrderLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            receivedOrderLayoutLp.setMargins(DisplayUtil.dip2px(context, 10), 0, DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 5));
            receivedOrderLayout.setLayoutParams(receivedOrderLayoutLp);
            receivedOrderLayout.setBackgroundResource(R.drawable.order_card_bg);
            receivedOrderLayout.setPadding(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
            receivedOrderLayout.setOrientation(LinearLayout.VERTICAL);
            receivedOrderLayout.setTag("close");
            receivedOrderLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((userType.equals("0") && orderData.getOrderState() == 4) ||
                            (!userType.equals("0") && orderData.getOrderState() == 3) ||
                            (!userType.equals("0") && orderData.getOrderState() == 4) ||
                            (!userType.equals("0") && orderData.getOrderState() == 5)) {
                        if (v.getTag() == "close") {
                            v.setTag("open");
                            v.findViewWithTag("orderDetailLayout").setVisibility(View.VISIBLE);
                        } else {
                            v.setTag("close");
                            v.findViewWithTag("orderDetailLayout").setVisibility(View.GONE);
                        }
                    }
                }
            });

            // 添加姓名、手机号
            LinearLayout line1 = new LinearLayout(context);
            LinearLayout.LayoutParams line1Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            line1.setLayoutParams(line1Lp);
            line1.setPadding(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
            line1.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvUserName = new TextView(context);
            LinearLayout.LayoutParams tvUserNameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvUserName.setLayoutParams(tvUserNameLp);
            tvUserName.setGravity(Gravity.LEFT);
            tvUserName.setText("客户名称：" + orderData.getUserName());
            line1.addView(tvUserName);

            TextView tvUserPhone = new TextView(context);
            LinearLayout.LayoutParams tvUserPhoneLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            tvUserPhone.setLayoutParams(tvUserPhoneLp);
            tvUserPhone.setGravity(Gravity.RIGHT);
            tvUserPhone.setText("联系电话：" + orderData.getUserPhone());
            line1.addView(tvUserPhone);

            receivedOrderLayout.addView(line1);

            // 添加学校、详细地址
            LinearLayout line2 = new LinearLayout(context);
            LinearLayout.LayoutParams line2Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            line2.setLayoutParams(line2Lp);
            line2.setPadding(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
            line2.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvAddress = new TextView(context);
            LinearLayout.LayoutParams tvAddressLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvAddress.setLayoutParams(tvAddressLp);
            tvAddress.setGravity(Gravity.LEFT);
            tvAddress.setText("详细地址：" + orderData.getAddressDetail());
            line2.addView(tvAddress);

            receivedOrderLayout.addView(line2);

            // 添加支付方式、下单时间
            LinearLayout line3 = new LinearLayout(context);
            LinearLayout.LayoutParams line3Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            line3.setLayoutParams(line3Lp);
            line3.setPadding(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
            line3.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvPayWay = new TextView(context);
            LinearLayout.LayoutParams tvPayWayLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvPayWay.setLayoutParams(tvPayWayLp);
            tvPayWay.setGravity(Gravity.LEFT);
            if (orderData.getOrderType() == 1) {
                tvPayWay.setText("支付方式：微信");
            } else if (orderData.getOrderType() == 2) {
                tvPayWay.setText("支付方式：货到");
            } else {
                tvPayWay.setText("支付方式：未知");
            }
            line3.addView(tvPayWay);

            TextView tvOrderTime = new TextView(context);
            LinearLayout.LayoutParams tvOrderTimeLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            tvOrderTime.setLayoutParams(tvOrderTimeLp);
            tvOrderTime.setGravity(Gravity.RIGHT);
            tvOrderTime.setText("下单时间：" + DateFormatUtil.dateToStr(new Date(Long.parseLong(orderData.getOrderDate())), DateFormatUtil.MMDDHHMM));
            line3.addView(tvOrderTime);

            receivedOrderLayout.addView(line3);

            // 添加备注
            if (ComFun.strNull(orderData.getRemark()) && !orderData.getRemark().equals("null")) {
                LinearLayout line4 = new LinearLayout(context);
                LinearLayout.LayoutParams line4Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                line4.setLayoutParams(line4Lp);
                line4.setPadding(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
                line4.setOrientation(LinearLayout.HORIZONTAL);

                TextView tvRemark = new TextView(context);
                LinearLayout.LayoutParams tvRemarkLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                tvRemark.setLayoutParams(tvRemarkLp);
                tvRemark.setGravity(Gravity.LEFT);
                tvRemark.setText("给商家留言：" + orderData.getRemark());
                line4.addView(tvRemark);

                receivedOrderLayout.addView(line4);
            }

            LinearLayout orderDetailLayout = new LinearLayout(context);
            LinearLayout.LayoutParams orderDetailLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            orderDetailLayout.setLayoutParams(orderDetailLayoutLp);
            orderDetailLayout.setOrientation(LinearLayout.VERTICAL);
            orderDetailLayout.setTag("orderDetailLayout");

            // 添加购买详情信息部分
            if (orderData.getOrderDetail().size() > 0) {
                View line = new View(context);
                line.setBackgroundColor(Color.parseColor("#242424"));
                LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(context, 1));
                line.setLayoutParams(lineLp);
                orderDetailLayout.addView(line);
                for (Order.BuyContent buyContent : orderData.getOrderDetail()) {
                    LinearLayout line5 = new LinearLayout(context);
                    LinearLayout.LayoutParams line5Lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    line5.setLayoutParams(line5Lp);
                    line5.setPadding(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
                    line5.setOrientation(LinearLayout.HORIZONTAL);

                    ImageView imgShow = new ImageView(context);
                    LinearLayout.LayoutParams imgShowLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(context, 80), DisplayUtil.dip2px(context, 80));
                    imgShow.setLayoutParams(imgShowLp);
                    drawableManager.fetchDrawableOnThread(buyContent.getUrl(), imgShow);
                    line5.addView(imgShow);

                    LinearLayout detailInfoLayout = new LinearLayout(context);
                    LinearLayout.LayoutParams detailInfoLayoutLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                    detailInfoLayout.setLayoutParams(detailInfoLayoutLp);
                    detailInfoLayout.setOrientation(LinearLayout.VERTICAL);

                    // 商品名
                    TextView tvBugCeilName = new TextView(context);
                    LinearLayout.LayoutParams tvBugCeilNameLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    tvBugCeilNameLp.setMargins(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
                    tvBugCeilName.setLayoutParams(tvBugCeilNameLp);
                    tvBugCeilName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    tvBugCeilName.setText(buyContent.getName());
                    TextPaint tvBugCeilNameTp = tvBugCeilName.getPaint();
                    tvBugCeilNameTp.setFakeBoldText(true);
                    detailInfoLayout.addView(tvBugCeilName);
                    // 购买数量
                    TextView tvBugCeilNum = new TextView(context);
                    LinearLayout.LayoutParams tvBugCeilNumLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    tvBugCeilNumLp.setMargins(DisplayUtil.dip2px(context, 10), 0, DisplayUtil.dip2px(context, 10), 0);
                    tvBugCeilNum.setLayoutParams(tvBugCeilNumLp);
                    tvBugCeilNum.setText("购买数量：" + buyContent.getNum() + "     价格：" + buyContent.getPrice() + " 元");
                    detailInfoLayout.addView(tvBugCeilNum);

                    line5.addView(detailInfoLayout);

                    orderDetailLayout.addView(line5);
                }
            }
            // 添加优惠价
            if (orderData.getCouponPrice().equals("1")) {
                TextView tvBugCouponManey = new TextView(context);
                LinearLayout.LayoutParams tvBugCouponManeyLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tvBugCouponManeyLp.setMargins(DisplayUtil.dip2px(context, 10), 0, DisplayUtil.dip2px(context, 20), 0);
                tvBugCouponManey.setLayoutParams(tvBugCouponManeyLp);
                tvBugCouponManey.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tvBugCouponManey.setGravity(Gravity.RIGHT);
                tvBugCouponManey.setText("新用户：- 3 元");
                TextPaint tvBugTotalManeyTp = tvBugCouponManey.getPaint();
                tvBugTotalManeyTp.setFakeBoldText(true);
                orderDetailLayout.addView(tvBugCouponManey);
            }
            // 添加服务费
            if (!orderData.getServicePrice().equals("0")) {
                TextView tvBugServiceManey = new TextView(context);
                LinearLayout.LayoutParams tvBugServiceManeyLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tvBugServiceManeyLp.setMargins(DisplayUtil.dip2px(context, 10), 0, DisplayUtil.dip2px(context, 20), 0);
                tvBugServiceManey.setLayoutParams(tvBugServiceManeyLp);
                tvBugServiceManey.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tvBugServiceManey.setGravity(Gravity.RIGHT);
                tvBugServiceManey.setText("服务费：+ " + orderData.getServicePrice() + " 元");
                TextPaint tvBugTotalManeyTp = tvBugServiceManey.getPaint();
                tvBugTotalManeyTp.setFakeBoldText(true);
                orderDetailLayout.addView(tvBugServiceManey);
            }
            // 添加总价
            TextView tvBugTotalManey = new TextView(context);
            LinearLayout.LayoutParams tvBugTotalManeyLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvBugTotalManeyLp.setMargins(DisplayUtil.dip2px(context, 10), 0, DisplayUtil.dip2px(context, 20), 0);
            tvBugTotalManey.setLayoutParams(tvBugTotalManeyLp);
            tvBugTotalManey.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvBugTotalManey.setGravity(Gravity.RIGHT);
            tvBugTotalManey.setText("总价：" + orderData.getOrderPrice() + " 元");
            TextPaint tvBugTotalManeyTp = tvBugTotalManey.getPaint();
            tvBugTotalManeyTp.setFakeBoldText(true);
            orderDetailLayout.addView(tvBugTotalManey);

            orderDetailLayout.setVisibility(View.GONE);
            receivedOrderLayout.addView(orderDetailLayout);

            // 添加接单按钮
            LinearLayout receiveBtnLayout = new LinearLayout(context);
            LinearLayout.LayoutParams receiveBtnLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            receiveBtnLayoutLp.setMargins(DisplayUtil.dip2px(context, 8), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 8), DisplayUtil.dip2px(context, 10));
            receiveBtnLayout.setLayoutParams(receiveBtnLayoutLp);
            receiveBtnLayout.setBackgroundResource(R.drawable.menu_item_style_1);
            receiveBtnLayout.setClickable(true);
            receiveBtnLayout.setDescendantFocusability(LinearLayout.FOCUS_BLOCK_DESCENDANTS);
            receiveBtnLayout.setOrientation(LinearLayout.HORIZONTAL);
            receiveBtnLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((userType.equals("0") && orderData.getOrderState() == 4) ||
                            (userType.equals("0") && orderData.getOrderState() == 3) ||
                            (!userType.equals("0") && orderData.getOrderState() == 2)) {
                        // 发送接单Handler
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        msg.what = MainActivity.MSG_UPDATE_ORDER_STATE;
                        data.putString("orderId", orderData.getId());
                        data.putInt("orderState", orderData.getOrderState());
                        msg.setData(data);
                        MainActivity.mHandler.sendMessage(msg);
                    }
                }
            });

            TextView tvReceive = new TextView(context);
            LinearLayout.LayoutParams tvReceiveLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvReceive.setLayoutParams(tvReceiveLp);
            tvReceive.setPadding(DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10), DisplayUtil.dip2px(context, 10));
            tvReceive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvReceive.setGravity(Gravity.CENTER);
            if (userType.equals("0") && orderData.getOrderState() == 4) {
                tvReceive.setText("完 成 订 单");
            } else if (!userType.equals("0") && orderData.getOrderState() == 3) {
                tvReceive.setText("等待配送员接单");
            } else if (!userType.equals("0") && orderData.getOrderState() == 4) {
                tvReceive.setText("配送员已接单，正在配送中...");
            } else if (!userType.equals("0") && orderData.getOrderState() == 5) {
                tvReceive.setText("商品已送达，订单完成");
            } else if ((userType.equals("0") && orderData.getOrderState() == 3) ||
                    (!userType.equals("0") && orderData.getOrderState() == 2)) {
                tvReceive.setText("接    单");
            } else if (userType.equals("0") && orderData.getOrderState() == 5) {
                tvReceive.setText("订单已完成");
            } else {
                tvReceive.setText(userType + " -=- " + orderData.getOrderState());
            }
            TextPaint tvReceiveTp = tvReceive.getPaint();
            tvReceiveTp.setFakeBoldText(true);
            receiveBtnLayout.addView(tvReceive);

            receivedOrderLayout.addView(receiveBtnLayout);

            mainOrderDataLayout.addView(receivedOrderLayout);
        }
    }

    public static MainOrderFragment newInstance(String type, List<Order> orderData) {
        Bundle bundle = new Bundle();
        SerializableOrderList orderDataList = new SerializableOrderList();
        orderDataList.setList(orderData);
        bundle.putString(BUNDLE_DATA_TYPE, type);
        bundle.putSerializable(BUNDLE_DATA_LIST, orderDataList);

        MainOrderFragment fragment = new MainOrderFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
