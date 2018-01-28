package com.fy.niu.fyreorder.util;

import android.content.Context;

import com.fy.niu.fyreorder.okHttpUtil.CommonOkHttpClient;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.request.CommonRequest;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;

import okhttp3.Call;

public class ConnectorInventory {

    /**
     * 用户登录
     */
    public static void  userLogin(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "toAppLogin", params), dataHandle);
    }

    /**
     * 获取用户基本信息数据
     */
    public static void getUserInfo(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "getUserDetailMsg", params), dataHandle);
    }

    /**
     * 获取任务列表
     */
    public static void getTaskList(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "getTaSkList", params), dataHandle);
    }

    /**
     * 修改任务状态
     */
    public static void updateTaskState(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "updateTaskState", params), dataHandle);
    }

    /**
     * 获取接单数据(已接)
     */
    public static void getOrderListReceived(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "getOrderListReceived", params), dataHandle);
    }

    /**
     * 获取接单数据(未接)
     */
    public static void getOrderListMissed(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "getOrderListMissed", params), dataHandle);
    }

    /**
     * 获取已完成接单数据
     */
    public static void getCompleteOrderList(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "getOrderListFinish", params), dataHandle);
    }

    /**
     * 修改订单状态
     */
    public static void updateOrderState(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "updateOrderState", params), dataHandle);
    }

    /**
     * 获取订单金额数据
     */
    public static void getOrderNumChargeByday(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "getOrderNumChargeByday", params), dataHandle);
    }

    /**
     * 设置开启或关闭接单
     */
    public static void setUserIfOpen(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "toSetUserIfOpen", params), dataHandle);
    }

    /**
     * 修改密码
     */
    public static void updateUserPass(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "toUpdateUserPass", params), dataHandle);
    }

    /**
     * 获取最后版本数据
     */
    public static Call getNewAppVersion(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        return CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "getNewVersion", params), dataHandle);
    }

}
