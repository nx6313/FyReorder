package com.fy.niu.fyreorder.util;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by 25596 on 2017/4/18.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 初始化极光推送SDK
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}
