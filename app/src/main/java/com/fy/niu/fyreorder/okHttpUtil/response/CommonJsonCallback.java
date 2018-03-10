package com.fy.niu.fyreorder.okHttpUtil.response;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.fy.niu.fyreorder.okHttpUtil.commonhttp.ResponseEntityToModule;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeHandleCookieListener;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.Constants;
import com.fy.niu.fyreorder.util.L;
import com.fy.niu.fyreorder.util.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by 18230 on 2017/3/26.
 */

public class CommonJsonCallback implements Callback {
    protected final String RESULT_CODE = "responseCode";
    protected final String ERROR_MSG = "responseErrorMsg";
    protected final String EMPTY_MSG = "";
    protected final String COOKIE_STORE = "Set-Cookie";

    /**
     * 将其它线程的数据转发到UI线程
     */
    private Handler mDeliveryHandler;
    private DisposeDataListener mListener;
    private Class<?> mClass;

    public CommonJsonCallback(DisposeDataHandle handle) {
        this.mListener = handle.mListener;
        this.mClass = handle.mClass;
        this.mDeliveryHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onFailure(Call call, final IOException ioexception) {
        /**
         * 此时还在非UI线程，因此要转发
         */
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onFinish();
                if (ioexception instanceof SocketTimeoutException) {
                    L.d("请求超时");
                    // 返回数据超时
                    mListener.onFailure(new OkHttpException(Constants.HTTP_OUT_TIME_ERROR, "返回数据超时"));
                } else {
                    L.d("请求异常：" + ioexception.getMessage());
                    mListener.onFailure(new OkHttpException(Constants.HTTP_NETWORK_ERROR, ioexception));
                }
                // 发送获取数据失败广播
                Intent getDataFailIntent = new Intent();
                getDataFailIntent.setAction(Constants.MSG_GET_DATA_FAIL);
                MyApplication.getInstance().sendBroadcast(getDataFailIntent);
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        final String result = response.body().string();
        final ArrayList<String> cookieLists = handleCookie(response.headers());
        final String encodedPath = response.request().url().encodedPath();
        response.body().close();
        response.close();
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                handleResponse(result, encodedPath);
                /**
                 * handle the cookie
                 */
                if (mListener instanceof DisposeHandleCookieListener) {
                    ((DisposeHandleCookieListener) mListener).onCookie(cookieLists);
                }
            }
        });
    }

    private ArrayList<String> handleCookie(Headers headers) {
        ArrayList<String> tempList = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.name(i).equalsIgnoreCase(COOKIE_STORE)) {
                tempList.add(headers.value(i));
            }
        }
        return tempList;
    }

    private void handleResponse(Object responseObj, String url) {
        mListener.onFinish();
        if (responseObj == null) {
            L.d(Constants.OVERALL_TAG, "访问地址：" + url
                    + "，该HTTP访问服务器无返回JSON值，或者返回值异常");
            mListener.onFailure(new OkHttpException(Constants.HTTP_REQUEST_FAIL_ERROR, EMPTY_MSG));
            // 发送获取数据失败广播
            Intent getDataFailIntent = new Intent();
            getDataFailIntent.setAction(Constants.MSG_GET_DATA_FAIL);
            MyApplication.getInstance().sendBroadcast(getDataFailIntent);
            return;
        }

        try {
            JSONObject result = null;
            if (ComFun.getJSONType(responseObj.toString()).equals(ComFun.JSON_TYPE.JSON_TYPE_OBJECT)) {
                result = new JSONObject(responseObj.toString());
            } else if (ComFun.getJSONType(responseObj.toString()).equals(ComFun.JSON_TYPE.JSON_TYPE_ARRAY)) {
                result = new JSONObject();
                JSONArray dataJsonArr = new JSONArray(responseObj.toString());
                result.put("dataList", dataJsonArr);
            } else {
                new Exception("返回JSON数据格式错误，访问地址：" + url + "，返回值为：" + responseObj.toString()).printStackTrace();
            }
            if (!result.has(RESULT_CODE)) {
                // 如果返回JSON中没有 RESULT_CODE 值，则默认是返回成功状态，未返回JSON中添加上该参数值
                result.put(RESULT_CODE, Constants.HTTP_REQUEST_SUCCESS_CODE);
            }

            if (result.optString(RESULT_CODE).equals(Constants.HTTP_REQUEST_SUCCESS_CODE)) {
                // 返回数据成功
                if (!ComFun.strNull(mClass)) {
                    mListener.onSuccess(result);
                } else {
                    Object obj = ResponseEntityToModule.parseJsonObjectToModule(result, mClass);
                    if (ComFun.strNull(obj)) {
                        mListener.onSuccess(obj);
                    } else {
                        mListener.onFailure(new OkHttpException(Constants.HTTP_JSON_ERROR, EMPTY_MSG));
                    }
                }
            } else if (result.optString(RESULT_CODE).equals(Constants.HTTP_REQUEST_LOGIN_ERROR_CODE)) {
                // 返回用户登录失败（用户名或密码错误）
                mListener.onFailure(new OkHttpException(Constants.HTTP_LOGIN_ERROR_ERROR, ComFun.strNull(result.optString(ERROR_MSG)) ? result.optString(ERROR_MSG) : "用户登录失败"));
            } else if (result.optString(RESULT_CODE).equals(Constants.REQUEST_CODE_HAS)) {
                // 需要新添加或新保存的数据在数据库中已经存在
                mListener.onFailure(new OkHttpException(Constants.HTTP_HAS_ERROR, ComFun.strNull(result.optString(ERROR_MSG)) ? result.optString(ERROR_MSG) : "需要新添加或新保存的数据在数据库中已经存在"));
            } else if (result.optString(RESULT_CODE).equals(Constants.HTTP_LAST_VERSION_NULL)) {
                // 检查新版本，新版本对象为NULL，客户端默认该情况为 当前为最新版本
                mListener.onFailure(new OkHttpException(Constants.HTTP_LAST_VERSION_IS_NULL, ComFun.strNull(result.optString(ERROR_MSG)) ? result.optString(ERROR_MSG) : "检查新版本，新版本对象为NULL，客户端默认该情况为 当前为最新版本"));
            } else {
                // 未知错误
                mListener.onFailure(new OkHttpException(Constants.HTTP_REQUEST_FAIL_ERROR, ComFun.strNull(result.optString(ERROR_MSG)) ? result.optString(ERROR_MSG) : "发生未知错误"));
            }
            // 发送获取数据成功广播
            Intent getDataSuccessIntent = new Intent();
            getDataSuccessIntent.setAction(Constants.MSG_GET_DATA_SUCCESS);
            MyApplication.getInstance().sendBroadcast(getDataSuccessIntent);
        } catch (Exception e) {
            mListener.onFailure(new OkHttpException(Constants.HTTP_REQUEST_FAIL_ERROR, e.getMessage()));
            L.d("获取数据失败：" + e.getMessage());
            // 发送获取数据失败广播
            Intent getDataFailIntent = new Intent();
            getDataFailIntent.setAction(Constants.MSG_GET_DATA_FAIL);
            MyApplication.getInstance().sendBroadcast(getDataFailIntent);
        }
    }
}
