package com.fy.niu.fyreorder.util;

import android.app.Application;
import android.app.Notification;
import android.media.RingtoneManager;
import android.util.Log;

import com.tencent.android.tpush.XGCustomPushNotificationBuilder;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

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

        XGPushConfig.enableDebug(getApplicationContext(), true);
        XGPushManager.registerPush(getApplicationContext(), new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int i) {
                SharedPreferencesTool.addOrUpdate(getApplicationContext(), "fyBaseData", "userToken", data.toString());
                Log.d("TPush", "注册成功，设备token为：" + data);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
//        XGCustomPushNotificationBuilder build = new XGCustomPushNotificationBuilder();
//        build.setSound(RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_ALARM)) // 设置声音
                // setSound(
                // Uri.parse("android.resource://" + getPackageName()
                // + "/" + R.raw.wind)) 设定Raw下指定声音文件
//                .setDefaults(Notification.DEFAULT_VIBRATE) // 震动
//                .setFlags(Notification.FLAG_NO_CLEAR); // 是否可清除
    }
}
