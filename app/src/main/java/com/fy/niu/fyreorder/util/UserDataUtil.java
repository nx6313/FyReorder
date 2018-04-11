package com.fy.niu.fyreorder.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nx6313 on 2018/3/20.
 * <p>
 * 用户信息数据中，userId 是会在重新登录时被替换掉的，其他的数据以用户Id作为键值参数保存
 */

public class UserDataUtil {
    public static final String fyLoginUserInfo = "fyLoginUserInfo";
    public static final String fySet = "fySet";
    public static final String fyPrintData = "fyPrintData";
    /////////////////////////// fyLoginUserInfo
    public static final String key_userId = "userId";
    public static final String key_userLoginName = "userLoginName";
    public static final String key_userLoginPass = "userLoginPass";
    public static final String key_ifGive = "ifGive";
    public static final String key_ifOpen = "ifOpen";
    public static final String key_floor = "floor";
    public static final String key_floorName = "floorName";
    public static final String key_needLogin = "needLogin";

    /////////////////////////// fySet
    public static final String key_receiveSelfFloor = "receiveSelfFloor";
    public static final String key_printIsOpen = "printIsOpen";
    public static final String key_connectionDeviceCode = "connectionDeviceCode";

    /////////////////////////// fyPrintData
    public static final String key_printDataList = "printDataList";

    public static void setUserId(Context context, String userId) {
        SharedPreferencesTool.addOrUpdate(context, fyLoginUserInfo, key_userId, userId);
    }

    public static String getUserId(Context context) {
        String userId = SharedPreferencesTool.getFromShared(context, fyLoginUserInfo, key_userId, "");
        return userId;
    }

    private static String getKeyForUser(Context context, String preSharedName) {
        String userId = SharedPreferencesTool.getFromShared(context, fyLoginUserInfo, key_userId, "");
        if (ComFun.strNull(userId)) {
            return preSharedName + "_" + userId;
        }
        return preSharedName;
    }

    public static String getDataByKey(Context context, String preSharedName, String key) {
        String userId = SharedPreferencesTool.getFromShared(context, fyLoginUserInfo, key_userId, "");
        return SharedPreferencesTool.getFromShared(context, preSharedName + "_" + userId, key);
    }

    public static Boolean getBooleanDataByKey(Context context, String preSharedName, String key) {
        String userId = SharedPreferencesTool.getFromShared(context, fyLoginUserInfo, key_userId, "");
        return SharedPreferencesTool.getBooleanFromShared(context, preSharedName + "_" + userId, key);
    }

    public static <T> List<T> getListDataByKey(Context context, String preSharedName, String key, Type classType) {
        String userId = SharedPreferencesTool.getFromShared(context, fyLoginUserInfo, key_userId, "");
        List<T> datalist = new ArrayList<>();
        String dataListStr = SharedPreferencesTool.getFromShared(context, preSharedName + "_" + userId, key);
        if (!ComFun.strNull(dataListStr)) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(dataListStr, classType);
        return datalist;
    }

    public static void saveUserData(Context context, String preSharedName, String key, String val) {
        SharedPreferencesTool.addOrUpdate(context, getKeyForUser(context, preSharedName), key, val);
    }

    public static void saveUserData(Context context, String preSharedName, String key, Boolean val) {
        SharedPreferencesTool.addOrUpdate(context, getKeyForUser(context, preSharedName), key, val);
    }

    public static <T> void saveUserData(Context context, String preSharedName, String key, List<T> datalist) {
        if (null == datalist || datalist.size() <= 0)
            return;
        Gson gson = new Gson();
        String dataListStr = gson.toJson(datalist);
        SharedPreferencesTool.addOrUpdate(context, getKeyForUser(context, preSharedName), key, dataListStr);
    }
}
