package com.fy.niu.fyreorder.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fy.niu.fyreorder.MainActivity;
import com.fy.niu.fyreorder.R;
import com.fy.niu.fyreorder.customView.HorizontalProgressbarWithProgress;
import com.fy.niu.fyreorder.model.Version;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * Created by 25596 on 2017/9/7.
 */

public class VersionUtil {
    public static AlertDialog versionDialog;
    public static HorizontalProgressbarWithProgress downloadProgressBar;
    private static int length;
    private static File file;
    private static Handler downloadHandler;
    private static List<HashMap<String, Integer>> threadList;
    private static int downloadTotal = 0;
    private static boolean downloading = false;

    public static void checkNewVersion(final Activity activity) {
        threadList = new ArrayList<>();

        ComFun.AlertDialogWrap alertDialogWrap = ComFun.showLoading(activity, null, true, true);
        // 创建网络请求，进行版本更新数据拿取
        Call call = ConnectorInventory.getNewAppVersion(activity, null, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                Log.d(" ==== 最后一条版本数据 === ", " ===> " + responseObj.toString());
                try {
                    JSONObject newVersionJson = new JSONObject(responseObj.toString());
                    if (newVersionJson.has("versionName") && newVersionJson.has("versionCode")) {
                        int versionCode = newVersionJson.getInt("versionCode");
                        int currentVersionCode = ComFun.getVersionCode(activity);
                        versionCode = 2;
                        if (versionCode > currentVersionCode) {
                            // 需要更新
                            Version version = new Version();
                            version.setId(newVersionJson.getString("id"));
                            version.setVersionCode(newVersionJson.getInt("versionCode"));
                            version.setVersionName(newVersionJson.getString("versionName"));
                            version.setUpdateDate(newVersionJson.getString("date"));
                            version.setContent(newVersionJson.getString("content"));
                            version.setAppUrl(newVersionJson.getString("url"));
                            String currentVersionName = ComFun.getVersionName(activity);
                            showNewVersionInfoDialog(activity, currentVersionName, version);
                        } else {
                            // 不需要更新
                            ComFun.showToast(activity, "当前已经是最新版本啦", Toast.LENGTH_SHORT);
                        }
                    } else {
                        ComFun.showToast(activity, "检查更新异常，请稍后重试", Toast.LENGTH_LONG);
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.showToast(activity, "检查更新失败，请稍后重试", Toast.LENGTH_LONG);
            }
        }));
        alertDialogWrap.setHttpCall(call);
    }

    private static void showNewVersionInfoDialog(final Activity activity, String oldVersionName, final Version version) {
        // 弹框显示新版本详细内容
        new android.support.v7.app.AlertDialog.Builder(activity).setTitle("发现新版本").setMessage(
                "当前版本：" + oldVersionName + "   最新版本：" + version.getVersionName() + "\n\n更新内容：" + Html.fromHtml(version.getContent(), null, new MyTagHandler(activity)) + "\n\n确定下载更新吗？")
                .setPositiveButton("下载更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startToDownloadNewVersion(activity, version.getAppUrl());
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    private static void startToDownloadNewVersion(final Activity activity, String apkUrl) {
        downloading = true;
        // 发送接单Handler
        Message msg = new Message();
        Bundle data = new Bundle();
        msg.what = MainActivity.MSG_START_DOWN_NEW_VERSION;
        data.putString("appUrl", apkUrl);
        msg.setData(data);
        MainActivity.mHandler.sendMessage(msg);
    }

    public static void addDownLoadHandler(final Activity activity) {
        downloadHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 0) {
                    downloadProgressBar.setProgress(msg.arg1);
                    if (msg.arg1 >= length) {
                        downloadTotal = 0;
                        downloading = false;
                        threadList.clear();
                        if (versionDialog != null && versionDialog.isShowing()) {
                            versionDialog.dismiss();
                        }
                        ComFun.installApk(activity, file);
                    }
                }
                return false;
            }
        });
    }

    public static void beginDownload(final Activity activity, final String apkUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Constants.HTTP_URL_BASE + apkUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                    length = conn.getContentLength();

                    downloadProgressBar.setMax(length);// 按照百分比现实进度
                    downloadProgressBar.setProgress(0);

                    if (length < 0) {
                        ComFun.showToast(activity, "文件不存在", Toast.LENGTH_SHORT);
                        return;
                    }
                    File downloadDir = new File(activity.getExternalCacheDir(), "download");
                    if (!downloadDir.exists()) {
                        downloadDir.mkdir();
                    }
                    file = new File(downloadDir, "fyReorder.apk");
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                    randomAccessFile.setLength(length);

                    int blockSize = length / 3;
                    for (int i = 0; i < 3; i++) {
                        int begin = i * blockSize;
                        int end = (i + 1) * blockSize;
                        if (i == 2) {
                            end = length;
                        }
                        HashMap<String, Integer> map = new HashMap<>();
                        map.put("begin", begin);
                        map.put("end", end);
                        map.put("finished", 0);
                        threadList.add(map);
                        // 创建新的线程，下载文件
                        Thread t = new Thread(new DownloadRunnable(i, begin, end, file, url));
                        t.start();
                    }

                } catch (MalformedURLException e) {
                    ComFun.showToast(activity, "URL 不正确，版本下载失败", Toast.LENGTH_SHORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    static class DownloadRunnable implements Runnable {
        private int begin;
        private int end;
        private File file;
        private URL url;
        private int id;

        public DownloadRunnable(int id, int begin, int end, File file, URL url) {
            this.begin = begin;
            this.end = end;
            this.file = file;
            this.url = url;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                if (begin > end) {
                    return;
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                conn.setRequestProperty("Range", "bytes=" + begin + "-" + end);

                InputStream is = conn.getInputStream();
                byte[] buf = new byte[1024 * 1024];
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(begin);
                int len;
                HashMap<String, Integer> map = threadList.get(id);
                while ((len = is.read(buf)) != -1 && downloading) {
                    randomAccessFile.write(buf, 0, len);
                    updateProgress(len);
                    map.put("finished", map.get("finished") + len);
                }
                is.close();
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized private static void updateProgress(int add) {
        downloadTotal += add;
        downloadHandler.obtainMessage(0, downloadTotal, 0).sendToTarget();
    }

}
