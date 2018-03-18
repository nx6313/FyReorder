package com.fy.niu.fyreorder.util;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by 25596 on 2017/4/18.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    public static BluetoothAdapter mBluetoothAdapter = null;
    public static Map<String, BluetoothSocket> mBluetoothSocketMap = new HashMap<>(); // 管理当前连接到蓝牙设备

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        JPushInterface.setDebugMode(true); // 开启JPush调试
    }
}
