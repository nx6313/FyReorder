package com.fy.niu.fyreorder.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.fy.niu.fyreorder.R;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by 18230 on 2017/4/17.
 */

public class ComFun {
    private static Toast mToast = null;
    public static List<Activity> activityActiveList = new ArrayList<>();

    public enum JSON_TYPE {
        /**
         * JSONObject
         */
        JSON_TYPE_OBJECT,
        /**
         * JSONArray
         */
        JSON_TYPE_ARRAY,
        /**
         * 不是JSON格式的字符串
         */
        JSON_TYPE_ERROR
    }

    /**
     * 向当前活动的ActivityList中添加项
     *
     * @param activity
     */
    public static void addToActiveActivityList(Activity activity) {
        if (!activityActiveList.contains(activity)) {
            activityActiveList.add(activity);
        }
    }

    /**
     * 清空所有当前活动的Activity
     */
    public static void clearAllActiveActivity() {
        for (Activity activity : activityActiveList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 显示Toast提示信息
     *
     * @param context
     * @param text
     * @param duration
     */
    public static void showToast(Context context, String text, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }
        mToast.show();
    }

    /**
     * 显示Toast提示信息(单例模式)
     *
     * @param context
     * @param text
     * @param duration
     */
    public static void showToastSingle(Context context, String text, int duration) {
        Toast mToastSingle = Toast.makeText(context, text, duration);
        mToastSingle.show();
    }

    /**
     * 判断对象不为空
     *
     * @param str
     * @param flags 为可选参数，如果需要进一步判断List/Map/Array中的数量是否为0，可传入true；否则可不传（参数一为List/
     *              Map/Array类型时有效）
     * @return
     */
    public static boolean strNull(Object str, boolean... flags) {
        if (str != null && str != "" && !str.equals("")) {
            if (flags != null && flags.length > 0 && flags[0]) {
                if (ArrayList.class.isInstance(str)) {
                    if ((((List<?>) str).size() == 0)) {
                        return false;
                    }
                } else if (HashMap.class.isInstance(str)) {
                    if ((((Map<?, ?>) str).size() == 0)) {
                        return false;
                    }
                } else if (str.getClass().isArray()) {
                    if (Arrays.asList(str).size() == 0) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打开输入法
     *
     * @param context
     */
    public static void openIME(Context context, EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 关闭输入法
     *
     * @param context
     */
    public static void closeIME(Context context, View view) {
        if (view.getWindowToken() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 获得屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) MyApplication.getInstance().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 显示加载框
     *
     * @param activity
     * @param loadingTipValue
     */
    public static AlertDialog loadingDialog = null;

    public static void showLoading(Activity activity, String loadingTipValue) {
        loadingDialog = new AlertDialog.Builder(activity, R.style.MyDialogStyle).setCancelable(false).create();
        loadingDialog.show();
        WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
        params.width = getScreenWidth() * 3 / 4;
        //params.height = 200;
        loadingDialog.getWindow().setAttributes(params);

        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog, null);
        loadingView.setBackgroundResource(R.drawable.loading_border);
        win.setContentView(loadingView);
        GifView loadingGif = (GifView) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setGifImage(R.drawable.loading_girl);
        loadingGif.setShowDimension(99, 184);
        loadingGif.setGifImageType(GifView.GifImageType.COVER);
        TextView loadingTip = (TextView) loadingView.findViewById(R.id.loadingTip);
        if (loadingTip != null) {
            if (strNull(loadingTipValue)) {
                loadingTip.setText(loadingTipValue);
            }
        }
    }

    /**
     * 显示加载框
     *
     * @param activity
     * @param loadingTipValue
     */
    public static AlertDialog showLoading(Activity activity, String loadingTipValue, boolean cancelable) {
        loadingDialog = new AlertDialog.Builder(activity, R.style.MyDialogStyle).setCancelable(cancelable).create();
        loadingDialog.show();
        WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
        params.width = getScreenWidth() * 3 / 4;
        //params.height = 200;
        loadingDialog.getWindow().setAttributes(params);

        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog, null);
        loadingView.setBackgroundResource(R.drawable.loading_border);
        win.setContentView(loadingView);
        GifView loadingGif = (GifView) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setGifImage(R.drawable.loading_girl);
        loadingGif.setShowDimension(99, 184);
        loadingGif.setGifImageType(GifView.GifImageType.COVER);
        TextView loadingTip = (TextView) loadingView.findViewById(R.id.loadingTip);
        if (loadingTip != null) {
            if (strNull(loadingTipValue)) {
                loadingTip.setText(loadingTipValue);
            } else {
                loadingTip.setVisibility(View.GONE);
            }
        }
        return loadingDialog;
    }

    /**
     * 显示加载框
     *
     * @param activity
     * @param loadingTipValue
     */
    public static AlertDialog showLoading(Activity activity, String loadingTipValue, final LoadingCallback callback) {
        loadingDialog = new AlertDialog.Builder(activity, R.style.MyDialogStyle).setCancelable(true).create();
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.cancel();
            }
        });
        loadingDialog.show();
        WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
        params.width = getScreenWidth() * 3 / 4;
        //params.height = 200;
        loadingDialog.getWindow().setAttributes(params);

        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog, null);
        loadingView.setBackgroundResource(R.drawable.loading_border);
        win.setContentView(loadingView);
        GifView loadingGif = (GifView) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setGifImage(R.drawable.loading_girl);
        loadingGif.setShowDimension(99, 184);
        loadingGif.setGifImageType(GifView.GifImageType.COVER);
        TextView loadingTip = (TextView) loadingView.findViewById(R.id.loadingTip);
        if (loadingTip != null) {
            if (strNull(loadingTipValue)) {
                loadingTip.setText(loadingTipValue);
            } else {
                loadingTip.setVisibility(View.GONE);
            }
        }
        return loadingDialog;
    }

    public interface LoadingCallback {
        void cancel();
    }

    /**
     * 显示加载框
     *
     * @param activity
     * @param loadingTipValue
     */
    public static AlertDialogWrap showLoading(Activity activity, String loadingTipValue, boolean cancelable, final boolean cancelCall) {
        final AlertDialogWrap alertDialogWrap = new AlertDialogWrap();
        loadingDialog = new AlertDialog.Builder(activity, R.style.MyDialogStyle).setCancelable(cancelable).create();
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (cancelCall && alertDialogWrap.getHttpCall() != null) {
                    Call getNewVersionCall = alertDialogWrap.getHttpCall();
                    getNewVersionCall.cancel();
                }
            }
        });
        loadingDialog.show();
        WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
        params.width = getScreenWidth() * 3 / 7;
        params.alpha = 0.8f;
        //params.height = 200;
        loadingDialog.getWindow().setAttributes(params);

        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog, null);
        loadingView.setBackgroundResource(R.drawable.loading_bg);
        win.setContentView(loadingView);
        GifView loadingGif = (GifView) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setGifImage(R.drawable.loading);
        loadingGif.setShowDimension(122, 115);
        loadingGif.setGifImageType(GifView.GifImageType.COVER);
        TextView loadingTip = (TextView) loadingView.findViewById(R.id.loadingTip);
        if (loadingTip != null) {
            if (strNull(loadingTipValue)) {
                loadingTip.setText(loadingTipValue);
            } else {
                loadingTip.setVisibility(View.GONE);
            }
        }
        alertDialogWrap.setLoadingDialog(loadingDialog);
        return alertDialogWrap;
    }

    public static class AlertDialogWrap {
        private AlertDialog loadingDialog = null;
        private Call httpCall = null;

        public AlertDialogWrap() {
        }

        public AlertDialog getLoadingDialog() {
            return loadingDialog;
        }

        public void setLoadingDialog(AlertDialog loadingDialog) {
            this.loadingDialog = loadingDialog;
        }

        public Call getHttpCall() {
            return httpCall;
        }

        public void setHttpCall(Call httpCall) {
            this.httpCall = httpCall;
        }
    }

    /**
     * 隐藏加载框
     */
    public static void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public static boolean strInArr(String[] strArr, String str) {
        if (strNull(strArr) && strArr.length > 0 && strNull(str)) {
            for (String s : strArr) {
                if (s.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double add(double d1, BigDecimal d2) {
        // 进行加法运算
        BigDecimal b1 = new BigDecimal(d1);
        return b1.add(d2).doubleValue();
    }

    public static double sub(double d1, double d2) {
        // 进行减法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.subtract(b2).doubleValue();
    }

    public static double mul(double d1, double d2) {
        // 进行乘法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }

    public static double div(double d1, double d2, int len) {
        // 进行除法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double round(double d, int len) {
        // 进行四舍五入操作
        BigDecimal b1 = new BigDecimal(d);
        BigDecimal b2 = new BigDecimal(1);
        // 任何一个数字除以1都是原数字
        // ROUND_HALF_UP是BigDecimal的一个常量，表示进行四舍五入的操作
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 并0操作
     *
     * @return
     */
    public static String addZero(String val) {
        if (ComFun.strNull(val)) {
            if (val.contains(".")) {
                int pointNum = new BigDecimal(val).scale();
                if (pointNum <= 2) {
                    for (int i = 0; i < 2 - pointNum; i++) {
                        val += "0";
                    }
                    return val;
                } else {
                    return addZero(round(Double.parseDouble(val), 2) + "");
                }
            } else {
                return val + ".00";
            }
        }
        return "0.00";
    }

    /**
     * 获取程序版本号
     *
     * @param mContext
     * @return
     * @throws Exception
     */
    public static int getVersionCode(Context mContext) {
        //获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return -1;
    }

    /**
     * 获取程序版本号显示值
     *
     * @param mContext
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context mContext) {
        //获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return "";
    }

    /**
     * 安装apk
     *
     * @param mContext
     * @param file
     */
    public static void installApk(Context mContext, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data = FileProvider.getUriForFile(mContext, "com.fy.niu.fyreorder.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(file);
        }
        intent.setDataAndType(data, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    /**
     * 为View设置字体
     *
     * @param context
     * @param view
     * @param fontName
     */
    public static void setFont(Context context, TextView view, String fontName) {
        if (!strNull(fontName)) {
            fontName = "nsjmmt.ttf";
        }
        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
        view.setTypeface(face);
    }

    /**
     * 获取JSON类型 (JSONObject 还是 JSONArray)
     *
     * @param str
     * @return
     */
    public static JSON_TYPE getJSONType(String str) {
        if (TextUtils.isEmpty(str)) {
            return JSON_TYPE.JSON_TYPE_ERROR;
        }

        final char[] strChar = str.substring(0, 1).toCharArray();
        final char firstChar = strChar[0];

        if (firstChar == '{') {
            return JSON_TYPE.JSON_TYPE_OBJECT;
        } else if (firstChar == '[') {
            return JSON_TYPE.JSON_TYPE_ARRAY;
        } else {
            return JSON_TYPE.JSON_TYPE_ERROR;
        }
    }

    /**
     * 处理字符串，每隔i个字符，加一个换行符
     *
     * @param proName
     * @param i
     * @return
     */
    public static String autoWordLine(String proName, int i) {
        String returnStr = "";
        if (strNull(proName)) {
            for (int s = 0; s < proName.length(); s++) {
                if ((s + 1) % i == 0) {
                    returnStr += proName.charAt(s) + "\n";
                } else {
                    returnStr += proName.charAt(s);
                }
            }
        }
        return returnStr;
    }
}
