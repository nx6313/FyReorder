package com.fy.niu.fyreorder.util;

import android.content.Context;

import com.fy.niu.fyreorder.okHttpUtil.CommonOkHttpClient;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.request.CommonRequest;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;

public class ConnectorInventory {

    /**
     * 用户登录
     */
    public static void userLogin(Context context, RequestParams params, DisposeDataHandle dataHandle) {
        CommonOkHttpClient.post(CommonRequest.createGetRequest(context, Constants.HTTP_URL_BASE + "toAppLogin", params), dataHandle);
    }

}
