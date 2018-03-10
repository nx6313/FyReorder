package com.fy.niu.fyreorder.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ant.liao.GifView;
import com.fy.niu.fyreorder.CompleteOrderActivity;
import com.fy.niu.fyreorder.MainActivity;
import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.model.Order;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.Constants;
import com.fy.niu.fyreorder.util.DateFormatUtil;
import com.fy.niu.fyreorder.util.SerializableOrderList;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;
import com.squareup.picasso.Picasso;

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
    private static int currentPageIndex_Complete = 1; // 已完成订单当前页码数，初始化时，有页面单独设置
    private static int currentPageIndex_WeiJie = 1; // 未接订单当前页码数，初始化时，有页面单独设置
    private static int currentPageIndex_YiJie = 1; // 已接订单当前页码数，初始化时，有页面单独设置

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
                msg.what = MainActivity.MSG_REF_ORDER_LSIT;
                MainActivity.mHandler.sendMessage(msg);
            }
        });
        noMainOrderDataLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 发送刷新订单Handler
                Message msg = new Message();
                msg.what = MainActivity.MSG_REF_ORDER_LSIT;
                MainActivity.mHandler.sendMessage(msg);
            }
        });
        mainOrderSwipeRefresh.setTag("orderSwipeRefresh_" + pageType);
        noMainOrderDataLayout.setTag("noDataLayout_" + pageType);
        if (mOrderDataList != null && mOrderDataList.size() > 0) {
            noMainOrderDataLayout.setVisibility(View.GONE);
            mainOrderSwipeRefresh.setVisibility(View.VISIBLE);
            LinearLayout mainOrderDataLayout = (LinearLayout) view.findViewById(R.id.mainOrderDataLayout);
            // 生成订单数据
            createMainOrderView(getActivity(), pageType, mainOrderDataLayout, mOrderDataList);
        } else {
            mainOrderSwipeRefresh.setVisibility(View.GONE);
            noMainOrderDataLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    public static void createMainOrderView(final Context context, String pageType, LinearLayout mainOrderDataLayout, List<Order> mOrderDataList) {
        mainOrderDataLayout.removeAllViews();
        addOrderItemView(context, pageType, mainOrderDataLayout, mOrderDataList);
    }

    private static void addOrderItemView(final Context context, final String pageType, LinearLayout mainOrderDataLayout, List<Order> mOrderDataList) {
        // 0学生  1商超   2外卖
        // 送货的  未接单 3 已接单 4
        // 商家    未接单 2 已接单 3
        // 学生未接 userType.equals("0") && orderData.getOrderState() == 3
        // 学生已接 userType.equals("0") && orderData.getOrderState() == 4
        // 商家未接 !userType.equals("0") && orderData.getOrderState() == 2
        // 商家已接 !userType.equals("0") && orderData.getOrderState() == 3
        final String userType = SharedPreferencesTool.getFromShared(context, "fyLoginUserInfo", "ifGive");
        LayoutInflater inflater = LayoutInflater.from(context);
        for (final Order orderData : mOrderDataList) {
            View orderItemView = inflater.inflate(R.layout.order_fragment_item, null);

            LinearLayout orderItemMainWrap = (LinearLayout) orderItemView.findViewWithTag("order_item_main_wrap");
            TextView orderType = (TextView) orderItemView.findViewWithTag("order_item_type");
            TextView userName = (TextView) orderItemView.findViewWithTag("order_item_user_name");
            TextView userAddress = (TextView) orderItemView.findViewWithTag("order_item_user_address");
            TextView userPayWay = (TextView) orderItemView.findViewWithTag("order_item_pay_way");
            TextView userPhone = (TextView) orderItemView.findViewWithTag("order_item_user_phone");
            TextView orderNum = (TextView) orderItemView.findViewWithTag("order_item_order_num");
            TextView orderTime = (TextView) orderItemView.findViewWithTag("order_item_order_time");
            TextView orderIdenCode = (TextView) orderItemView.findViewWithTag("order_item_iden_code");
            TextView orderRemark = (TextView) orderItemView.findViewWithTag("order_item_order_remark");
            RelativeLayout orderReceiveUserInfoLayout = (RelativeLayout) orderItemView.findViewWithTag("order_item_receive_user_info_layout");
            TextView receiveUserName = (TextView) orderItemView.findViewWithTag("order_item_receive_user_name");
            TextView receiveUserPhone = (TextView) orderItemView.findViewWithTag("order_item_receive_user_phone");
            ImageView receiveOrderCall = (ImageView) orderItemView.findViewWithTag("order_item_receive_order_call");
            final LinearLayout itemDetailLayout = (LinearLayout) orderItemView.findViewWithTag("order_item_detail");
            LinearLayout itemDetailMoneyWrapLayout = (LinearLayout) orderItemView.findViewWithTag("order_item_detail_money_wrap");
            LinearLayout itemDetailWrapLayout = (LinearLayout) orderItemView.findViewWithTag("order_item_detail_wrap");
            TextView itemCouponMoney = (TextView) orderItemView.findViewWithTag("order_item_coupon");
            TextView itemServiceMoney = (TextView) orderItemView.findViewWithTag("order_item_service");
            TextView itemAllMoney = (TextView) orderItemView.findViewWithTag("order_item_all_money");
            LinearLayout orderItemDoLayout = (LinearLayout) orderItemView.findViewWithTag("order_item_do");
            TextView orderItemDo1 = (TextView) orderItemView.findViewWithTag("order_item_do_1");
            ImageView orderCall = (ImageView) orderItemView.findViewWithTag("order_item_order_call");
            if (pageType.equals("complete")) {
                receiveOrderCall.setVisibility(View.GONE);
                orderCall.setVisibility(View.GONE);
                orderItemDoLayout.setVisibility(View.GONE);
                userPhone.setVisibility(View.VISIBLE);
            }

            orderItemView.setTag("close");
            itemDetailLayout.setVisibility(View.GONE);
            orderItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((userType.equals("0") && orderData.getOrderState() == 4) ||
                            (userType.equals("0") && orderData.getOrderState() == 5) ||
                            (!userType.equals("0") && orderData.getOrderState() == 3) ||
                            (!userType.equals("0") && orderData.getOrderState() == 4) ||
                            (!userType.equals("0") && orderData.getOrderState() == 5)) {
                        if (v.getTag() == "close") {
                            v.setTag("open");
                            itemDetailLayout.setVisibility(View.VISIBLE);
                        } else {
                            v.setTag("close");
                            itemDetailLayout.setVisibility(View.GONE);
                        }
                    }
                }
            });

            if (pageType.equals("weiJie")) {
                orderItemMainWrap.setBackgroundResource(R.drawable.order_card_bg_w);
            }
            userName.setText("客户名称：" + orderData.getUserName() + "〔 "+ orderData.getShopName() +" 〕");
            if (orderData.getOrderType() == 1) {
                orderType.setText("订单类型：零食订单");
            } else if (orderData.getOrderType() == 2) {
                orderType.setText("订单类型：外卖订单");
            }
            userAddress.setText("详细地址：" + orderData.getAddressDetail());
            if (orderData.getPayType() == 1) {
                userPayWay.setText("支付方式：微信支付");
                if (pageType.equals("weiJie")) {
                    orderItemMainWrap.setBackgroundResource(R.drawable.order_card_bg_w_wx);
                } else {
                    orderItemMainWrap.setBackgroundResource(R.drawable.order_card_bg_wx);
                }
            } else if (orderData.getPayType() == 2) {
                userPayWay.setText("支付方式：货到付款");
            } else {
                userPayWay.setText("支付方式：未知");
            }
            if(ComFun.strNull(orderData.getUserPhone())) {
                userPhone.setText("客户电话：" + orderData.getUserPhone().substring(0, 3) + " **** " + orderData.getUserPhone().substring(orderData.getUserPhone().length() - 4, orderData.getUserPhone().length()));
            } else {
                userPhone.setText("客户电话：---");
            }
            orderNum.setText("编号：" + orderData.getOrderNumber());
            orderTime.setText("时间：" + DateFormatUtil.dateToStr(new Date(Long.parseLong(orderData.getOrderDate())), DateFormatUtil.MDHHMM));
            if(pageType.equals("yiJie")) {
                orderIdenCode.setVisibility(View.GONE);
                orderIdenCode.setText("完成码：" + orderData.getIdenCode());
            }
            if (ComFun.strNull(orderData.getRemark()) && !orderData.getRemark().equals("null")) {
                orderRemark.setText("给商家的留言：" + orderData.getRemark());
            } else {
                orderRemark.setVisibility(View.GONE);
            }
            if (pageType.equals("complete") || (!userType.equals("0") && orderData.getOrderState() == 4)) {
                orderReceiveUserInfoLayout.setVisibility(View.VISIBLE);
                if (ComFun.strNull(orderData.getPersonName())) {
                    receiveUserName.setText("配送员名称：" + orderData.getPersonName());
                } else {
                    receiveUserName.setText("配送员名称：名称为空");
                }
                if (ComFun.strNull(orderData.getPersonPhone())) {
                    receiveUserPhone.setText("电话：" + orderData.getPersonPhone().substring(0, 3) + " **** " + orderData.getPersonPhone().substring(orderData.getPersonPhone().length() - 4, orderData.getPersonPhone().length()));
                    if (!pageType.equals("complete")) {
                        receiveUserPhone.setText(receiveUserPhone.getText() + "（点击右侧图标拨打）");
                        receiveOrderCall.setVisibility(View.VISIBLE);
                    }
                } else {
                    receiveUserPhone.setText("电话：---");
                    receiveOrderCall.setVisibility(View.GONE);
                }
            } else {
                orderReceiveUserInfoLayout.setVisibility(View.GONE);
            }
            if (orderData.getOrderDetail().size() > 0) {
                for (Order.BuyContent buyContent : orderData.getOrderDetail()) {
                    View orderDetailItemView = inflater.inflate(R.layout.order_fragment_detail_item, null);
                    ImageView orderDetailItemImg = (ImageView) orderDetailItemView.findViewWithTag("order_detail_item_img");
                    TextView orderDetailItemName = (TextView) orderDetailItemView.findViewWithTag("order_detail_item_name");
                    TextView orderDetailItemCountPrice = (TextView) orderDetailItemView.findViewWithTag("order_detail_item_count_price");

                    Picasso.with(context).load(Constants.HTTP_URL_BASE_NEW + buyContent.getUrl()).resize(100, 100).centerCrop().placeholder(R.drawable.food_default).error(R.drawable.food_default).into(orderDetailItemImg);
                    orderDetailItemName.setText("商品名称：" + buyContent.getName());
                    orderDetailItemCountPrice.setText("购买数量：" + buyContent.getNum() + "           价格：" + buyContent.getPrice() + " 元");

                    itemDetailWrapLayout.addView(orderDetailItemView);
                }
            } else {
                itemDetailMoneyWrapLayout.setVisibility(View.GONE);
            }
            if (orderData.getCouponPrice().equals("1")) {
                itemCouponMoney.setText("新用户：- 3 元");
            } else {
                itemCouponMoney.setVisibility(View.GONE);
            }
            if (!orderData.getServicePrice().equals("0")) {
                itemServiceMoney.setText("服务费：+ " + orderData.getServicePrice() + " 元");
            } else {
                itemServiceMoney.setVisibility(View.GONE);
            }
            itemAllMoney.setText("总价：" + orderData.getOrderPrice() + " 元");
            orderItemDoLayout.setOnClickListener(new View.OnClickListener() {
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
                        data.putSerializable("orderData", orderData);
                        msg.setData(data);
                        MainActivity.mHandler.sendMessage(msg);
                    }
                }
            });
            if (userType.equals("0") && orderData.getOrderState() == 4) {
                orderItemDo1.setText("点 击 完 成 订 单");
            } else if (!userType.equals("0") && orderData.getOrderState() == 3) {
                orderItemDo1.setText("等待配送员接单");
            } else if (!userType.equals("0") && orderData.getOrderState() == 4) {
                orderItemDo1.setText("配送员已接单，正在配送中...");
            } else if (!userType.equals("0") && orderData.getOrderState() == 5) {
                orderItemDo1.setText("商品已送达，订单完成");
            } else if ((userType.equals("0") && orderData.getOrderState() == 3) ||
                    (!userType.equals("0") && orderData.getOrderState() == 2)) {
                orderItemDo1.setText("接    单");
            } else if (userType.equals("0") && orderData.getOrderState() == 5) {
                orderItemDo1.setText("订单已完成");
            } else {
                orderItemDo1.setText(userType + " -=- " + orderData.getOrderState());
            }
            receiveOrderCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    msg.what = MainActivity.MSG_CALL_USER_PHONE;
                    data.putString("userPhone", orderData.getPersonPhone());
                    msg.setData(data);
                    MainActivity.mHandler.sendMessage(msg);
                }
            });
            orderCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    msg.what = MainActivity.MSG_CALL_USER_PHONE;
                    data.putString("userPhone", orderData.getUserPhone());
                    msg.setData(data);
                    MainActivity.mHandler.sendMessage(msg);
                }
            });

            mainOrderDataLayout.addView(orderItemView);
        }
        // 添加是否需要上拉刷新的布局
        if ((pageType.equals("weiJie") || pageType.equals("yiJie") || pageType.equals("complete")) && mOrderDataList.size() > 0) {
            final View topPullRefView = inflater.inflate(R.layout.pull_ref, null);
            GifView topPullRefIngGif = (GifView) topPullRefView.findViewById(R.id.topPullRefIngGif);
            //topPullRefIngGif.setGifImage(R.drawable.refing);
            topPullRefIngGif.setShowDimension(40, 40);
            topPullRefIngGif.setGifImageType(GifView.GifImageType.COVER);
            LinearLayout topPullRefLayout = (LinearLayout) topPullRefView.findViewById(R.id.topPullRefLayout);
            topPullRefLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag().toString().equals("pullToRef")) {
                        v.setTag("refIng");
                        // 更新布局为正在刷新
                        topPullRefView.findViewById(R.id.topPullRefTipTv).setVisibility(View.GONE);
                        topPullRefView.findViewById(R.id.topPullRefIngLayout).setVisibility(View.VISIBLE);
                        // 发送下一页数据请求广播
                        Intent refPageDataIntent = new Intent();
                        refPageDataIntent.setAction(CompleteOrderActivity.MSG_GET_NEW_PAGE_DATA);
                        refPageDataIntent.putExtra("pageType", pageType);
                        if(pageType.equals("complete")){
                            refPageDataIntent.putExtra("currentPageIndex", currentPageIndex_Complete + 1);
                        } else if(pageType.equals("weiJie")){
                            refPageDataIntent.putExtra("currentPageIndex", currentPageIndex_WeiJie + 1);
                        } else if(pageType.equals("yiJie")){
                            refPageDataIntent.putExtra("currentPageIndex", currentPageIndex_YiJie + 1);
                        }
                        context.sendBroadcast(refPageDataIntent);
                    }
                }
            });
            mainOrderDataLayout.addView(topPullRefView);
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

    public static void setCurrentPageIndex(int pageIndex, String pageType) {
        if(pageType.equals("complete")){
            currentPageIndex_Complete = pageIndex;
        } else if(pageType.equals("weiJie")){
            currentPageIndex_WeiJie = pageIndex;
        } else if(pageType.equals("yiJie")){
            currentPageIndex_YiJie = pageIndex;
        }
    }

    public static void completeRef(Context context, String pageType, LinearLayout mainOrderDataLayout, List<Order> pageOrderList, boolean finished) {
        if (mainOrderDataLayout != null) {
            if (!finished && pageOrderList.size() > 0) {
                mainOrderDataLayout.removeView(mainOrderDataLayout.findViewById(R.id.topPullRefLayout));
                addOrderItemView(context, pageType, mainOrderDataLayout, pageOrderList);
            }
            LinearLayout topPullRefLayout = (LinearLayout) mainOrderDataLayout.findViewById(R.id.topPullRefLayout);
            if (topPullRefLayout != null) {
                if (!finished) {
                    topPullRefLayout.setTag("pullToRef");
                } else {
                    topPullRefLayout.setTag("pullFinished");
                    TextView topPullRefTipTv = (TextView) mainOrderDataLayout.findViewById(R.id.topPullRefTipTv);
                    topPullRefTipTv.setText("没更多数据了");
                }
                // 更新布局为完成刷新
                mainOrderDataLayout.findViewById(R.id.topPullRefTipTv).setVisibility(View.VISIBLE);
                mainOrderDataLayout.findViewById(R.id.topPullRefIngLayout).setVisibility(View.GONE);
            }
        }
    }

}
