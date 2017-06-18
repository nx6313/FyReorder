package com.fy.niu.fyreorder.util;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.fy.niu.fyreorder.okHttpUtil.CommonOkHttpClient;
import com.fy.niu.fyreorder.okHttpUtil.request.CommonRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 18230 on 2017/6/19.
 */

public class DrawableManager {
    private final Map<String, Drawable> drawableMap;
    public DrawableManager() {
        drawableMap = new HashMap<>();
    }

    public Drawable fetchDrawable(String urlString) {
        if (drawableMap.containsKey(urlString)) {
            return drawableMap.get(urlString);
        }
        Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
        try {
            InputStream is = fetch(urlString);
            if(is != null){
                Drawable drawable = Drawable.createFromStream(is, "src");
                if (drawable != null) {
                    drawableMap.put(urlString, drawable);
                    Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
                            + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                            + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
                } else {
                    Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
                }

                return drawable;
            }else{
                Log.e(this.getClass().getSimpleName(), "fetchDrawable failed");
                return null;
            }
        } catch (MalformedURLException e) {
            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        }
    }

    public void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
        if (drawableMap.containsKey(urlString)) {
            imageView.setImageDrawable(drawableMap.get(urlString));
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                imageView.setImageDrawable((Drawable) message.obj);
            }
        };

        Thread thread = new Thread() {//多线程异步处理
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                Drawable drawable = fetchDrawable(urlString);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    //获取远程资源
    private InputStream fetch(String urlString) throws IOException {
        CommonOkHttpClient httpClient = new CommonOkHttpClient();
        Response response = httpClient.execute(urlString);
        if(response != null){
            return response.body().byteStream();
        }
        return null;
    }
}
