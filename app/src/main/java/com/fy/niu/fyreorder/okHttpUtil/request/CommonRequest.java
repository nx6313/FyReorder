package com.fy.niu.fyreorder.okHttpUtil.request;

import android.content.Context;

import java.io.File;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 负责创建各种类型的请求对象，包括get, post, 文件上传类型, 文件下载类型
 */
public class CommonRequest {
    public static String PROJECT_NAME = "FyreorderServer";

    public static Request createPostRequest(Context context, String url, RequestParams params) {

        FormBody.Builder mFormBodyBuild = new FormBody.Builder();

        // 默认加上当前登录用户Id
//        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
        if(params == null){
            params = new RequestParams();
        }
//        params.put("userId", userId);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                mFormBodyBuild.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody mFormBody = mFormBodyBuild.build();
        return new Request.Builder().url(url).post(mFormBody).build();
    }

    public static Request createGetRequest(Context context, String url, RequestParams params) {
        StringBuilder urlBuilder = new StringBuilder(url).append("?");

        // 默认加上当前登录用户Id
//        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
        if(params == null){
            params = new RequestParams();
        }
//        params.put("userId", userId);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1)).get().build();
    }

    public static Request createGetRequest(String url) {
        StringBuilder urlBuilder = new StringBuilder(url);

        return new Request.Builder().url(urlBuilder.toString()).get().build();
    }

    private static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream");

    /**
     * 文件上传请求
     * @param url
     * @param params
     * @return
     */
    public static Request createMultiPostRequest(Context context, String url, RequestParams params) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);

        // 默认加上当前登录用户Id
//        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
        if(params == null){
            params = new RequestParams();
        }
//        params.put("userId", userId);

        if (params != null) {

            for (Map.Entry<String, Object> entry : params.fileParams.entrySet()) {
                if (entry.getValue() instanceof File) {
                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(FILE_TYPE, (File) entry.getValue()));
                } else if (entry.getValue() instanceof String) {

                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(null, (String) entry.getValue()));
                }
            }
        }
        return new Request.Builder().url(url).post(requestBody.build()).build();
    }
}
