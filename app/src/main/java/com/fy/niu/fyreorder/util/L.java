package com.fy.niu.fyreorder.util;

import android.content.Context;
import android.util.Log;

/**
 * Created by 18230 on 2017/3/20.
 */

public class L {
    private static final String TAG = "fyreorder_debug";
    private static final boolean debugFlag = true;

    public static void e(String  msg){
        if(debugFlag)
            Log.e(TAG, msg);
    }

    public static void d(String  msg){
        if(debugFlag)
            Log.d(TAG, msg);
    }

    public static void d(String tag, String  msg){
        if(debugFlag)
            Log.d(tag, msg);
    }

    public static void toast(Context context, String text, int duration){
        if(debugFlag)
            ComFun.showToast(context, text, duration);
    }
}
