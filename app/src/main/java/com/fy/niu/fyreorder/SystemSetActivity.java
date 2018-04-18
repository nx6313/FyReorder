package com.fy.niu.fyreorder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        if (!ComFun.strNull(orderSoundUri)) {
            UserDataUtil.saveUserData(SystemSetActivity.this, UserDataUtil.fySet, UserDataUtil.key_orderSoundUri, "default");
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
    }

    // 点击从手机设置订单音效按钮
    public void setOrderSoundFromPhone(View view) {
        orderSoundDoPaneLayout.setVisibility(View.GONE);
    }
}
