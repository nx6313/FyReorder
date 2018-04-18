package com.fy.niu.fyreorder;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.DisplayUtil;
import com.fy.niu.fyreorder.util.UserDataUtil;

public class SystemSetActivity extends AppCompatActivity {
    private TextView tvOrderSoundItemTip;
    private TextView tvOrderSoundSetData;
    private LinearLayout orderSoundDoPaneLayout;

    private MediaPlayer mMediaMusic = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_set);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(SystemSetActivity.this);

        // 处理为标题居中
        ((TextView) toolbar.findViewById(R.id.toolbarTitleTv)).setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupActionBar();

        initView();

        initData();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            SystemSetActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        tvOrderSoundItemTip = (TextView) findViewById(R.id.tvOrderSoundItemTip);
        tvOrderSoundSetData = (TextView) findViewById(R.id.tvOrderSoundSetData);
        orderSoundDoPaneLayout = (LinearLayout) findViewById(R.id.orderSoundDoPaneLayout);
    }

    public void initData() {
        String orderSoundUri = UserDataUtil.getDataByKey(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundUri);
        String orderSoundName = UserDataUtil.getDataByKey(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundName);
        if (!ComFun.strNull(orderSoundUri)) {
            UserDataUtil.saveUserData(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundUri, "default");
        }
        if (!ComFun.strNull(orderSoundName)) {
            UserDataUtil.saveUserData(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundName, "默认提示音效");
            tvOrderSoundSetData.setText("默认提示音效");
        } else {
            tvOrderSoundSetData.setText(orderSoundName);
        }
    }

    // 播放当前的订单提示音效
    public void playCurrentOrderSound(final View view) {
        if (!view.getTag().toString().equals("playing")) {
            view.setTag("playing");
            Drawable sound_playing = SystemSetActivity.this.getResources().getDrawable(R.drawable.sound_playing);
            sound_playing.setBounds(0, 0, DisplayUtil.dip2px(SystemSetActivity.this, 16), DisplayUtil.dip2px(SystemSetActivity.this, 16));
            ((TextView) view).setCompoundDrawables(sound_playing, null, null, null);
            ((TextView) view).setTextColor(Color.parseColor("#E13455"));
            ((RelativeLayout) view.getParent()).setBackgroundColor(Color.parseColor("#F9E3EC"));

            String orderSoundUri = UserDataUtil.getDataByKey(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundUri);
            if (orderSoundUri.equals("default")) {
                mMediaMusic = MediaPlayer.create(this, R.raw.order_default_sound);
            } else {
                mMediaMusic = MediaPlayer.create(this, Uri.parse(orderSoundUri));
            }
            mMediaMusic.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    view.setTag("no_play");
                    Drawable sound = SystemSetActivity.this.getResources().getDrawable(R.drawable.sound);
                    sound.setBounds(0, 0, DisplayUtil.dip2px(SystemSetActivity.this, 16), DisplayUtil.dip2px(SystemSetActivity.this, 16));
                    ((TextView) view).setCompoundDrawables(sound, null, null, null);
                    ((TextView) view).setTextColor(Color.parseColor("#262626"));
                    ((RelativeLayout) view.getParent()).setBackgroundColor(Color.parseColor("#FAFAFA"));
                }
            });
            if (!mMediaMusic.isPlaying()) {
                mMediaMusic.start();
            }
        } else {
            view.setTag("no_play");
            Drawable sound = SystemSetActivity.this.getResources().getDrawable(R.drawable.sound);
            sound.setBounds(0, 0, DisplayUtil.dip2px(SystemSetActivity.this, 16), DisplayUtil.dip2px(SystemSetActivity.this, 16));
            ((TextView) view).setCompoundDrawables(sound, null, null, null);
            ((TextView) view).setTextColor(Color.parseColor("#262626"));
            ((RelativeLayout) view.getParent()).setBackgroundColor(Color.parseColor("#FAFAFA"));

            if (mMediaMusic != null && mMediaMusic.isPlaying()) {
                mMediaMusic.stop();
            }
        }
    }

    // 设置订单提示音效
    public void setOrderSound(View view) {
        orderSoundDoPaneLayout.setVisibility(View.VISIBLE);
    }

    // 点击试听音效按钮
    public void tryPlayOrderSound(View view) {
        orderSoundDoPaneLayout.setVisibility(View.GONE);
        playCurrentOrderSound(tvOrderSoundItemTip);
    }

    // 点击设置订单音效为默认按钮
    public void setOrderSoundToDefault(View view) {
        orderSoundDoPaneLayout.setVisibility(View.GONE);
        tvOrderSoundSetData.setText("默认提示音效");
        UserDataUtil.saveUserData(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundUri, "default");
        UserDataUtil.saveUserData(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundName, "默认提示音效");
    }

    // 点击从手机设置订单音效按钮
    public void setOrderSoundFromPhone(View view) {
        orderSoundDoPaneLayout.setVisibility(View.GONE);
        String orderSoundUri = UserDataUtil.getDataByKey(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundUri);
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置订单提示音效");
        if (ComFun.strNull(orderSoundUri) && !orderSoundUri.equals("default")) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(orderSoundUri)); //将已经勾选过的铃声传递给系统铃声界面进行显示
        }
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            try {
                Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI); //获取用户选择的铃声数据
                String path = getRealFilePath(SystemSetActivity.this, pickedUri);
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                UserDataUtil.saveUserData(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundUri, pickedUri.toString());
                UserDataUtil.saveUserData(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundName, fileName);
                tvOrderSoundSetData.setText(fileName);
            } catch (Exception e) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
