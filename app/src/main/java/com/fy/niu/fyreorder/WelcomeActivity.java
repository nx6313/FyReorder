package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.fragment.GuideFragment;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private Handler mWelcomeHandler;
    private WelcomeTask mWelcomeTesk;

    private ViewPager welcomeViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<GuideFragment> mContents = new ArrayList<>();

    private TextView welcomeToAppMain;

    private List<Integer> welcomeImgIdList = new ArrayList<>(); // 图片资源的数组
    private String toPageType = "toLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComFun.addToActiveActivityList(WelcomeActivity.this);

        // 隐藏标题栏和状态栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉状态栏
        setContentView(R.layout.activity_welcome);

        welcomeViewPager = (ViewPager) findViewById(R.id.welcomeViewPager);
        welcomeViewPager.setVisibility(View.GONE);

        welcomeToAppMain = (TextView) findViewById(R.id.welcomeToAppMain);
        welcomeToAppMain.setVisibility(View.GONE);

        String loginUserId = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "fyLoginUserInfo", "userId");
        if(ComFun.strNull(loginUserId)){
            // 已打开过，直接进入程序
            boolean needLogin = SharedPreferencesTool.getBooleanFromShared(WelcomeActivity.this, "fyLoginUserInfo", "needLogin");
            if(needLogin){
                toPageType = "toLogin";
            }else{
                toPageType = "toMain";
                XGPushManager.registerPush(WelcomeActivity.this, new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object data, int i) {
                        SharedPreferencesTool.addOrUpdate(WelcomeActivity.this, "fyBaseData", "userToken", data.toString());
                        Log.d("TPush", "欢迎页，注册成功，设备token为：" + data);
                    }

                    @Override
                    public void onFail(Object data, int errCode, String msg) {
                        Log.d("TPush", "欢迎页，注册失败，错误码：" + errCode + ",错误信息：" + msg);
                    }
                });
            }
            mWelcomeHandler = new Handler();
            mWelcomeTesk = new WelcomeTask();
            mWelcomeHandler.postDelayed(mWelcomeTesk, 2000);
        }else{
            toPageType = "toWelcome";
            // 添加欢迎页图片
            welcomeImgIdList.add(R.drawable.guide_1);
            welcomeImgIdList.add(R.drawable.guide_2);
            welcomeImgIdList.add(R.drawable.guide_3);

            // 加载ViewPager
            initViewPager();

            mWelcomeHandler = new Handler();
            mWelcomeTesk = new WelcomeTask();
            mWelcomeHandler.postDelayed(mWelcomeTesk, 2000);
        }
    }

    /**
     * 加载引导页图片
     */
    private void initViewPager() {
        for(int i = 0; i < welcomeImgIdList.size(); i++){
            GuideFragment guideFragment = GuideFragment.newInstance(welcomeImgIdList.get(i));
            mContents.add(guideFragment);
        }

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mContents.get(position);
            }

            @Override
            public int getCount() {
                return mContents.size();
            }
        };

        // View集合初始化好后，设置Adapter
        welcomeViewPager.setAdapter(mAdapter);
        welcomeViewPager.setOffscreenPageLimit(welcomeImgIdList.size());
        // 设置滑动监听
        welcomeViewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //判断是否是最后一页，若是则显示按钮
        if (position == welcomeImgIdList.size() - 1){
            welcomeToAppMain.setVisibility(View.VISIBLE);
        }else {
            welcomeToAppMain.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 欢迎页定时跳转程序任务
     */
    class WelcomeTask implements Runnable {

        @Override
        public void run() {
            // 如果有欢迎页展示，并且用户是第一次使用软件，则显示欢迎页图片
            // 如果有欢迎页展示，但用户不是第一次使用软件，则直接进入登录页面
            // 如果没有欢迎页展示，则直接进入登录页面
            if(welcomeImgIdList.size() > 0 && toPageType.equals("toWelcome")){
                welcomeViewPager.setVisibility(View.VISIBLE);
                welcomeViewPager.setCurrentItem(0);

                if(welcomeImgIdList.size() == 1){
                    welcomeToAppMain.setVisibility(View.VISIBLE);
                }
            }else if(toPageType.equals("toLogin")){
                // 直接进入登录页面
                Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                WelcomeActivity.this.finish();
            }else if(toPageType.equals("toMain")){
                // 直接进入程序首页
                Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                mainIntent.putExtra("needSilentLogin", true);
                startActivity(mainIntent);
                WelcomeActivity.this.finish();
            }
        }
    }

    /**
     * 点击立即体验 跳转程序主页--登录
     * @param view
     */
    public void toAppMain(View view){
        Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        WelcomeActivity.this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(welcomeViewPager != null && welcomeViewPager.getVisibility() == View.VISIBLE){
                System.exit(0);
            }else{
                // 欢迎页面屏蔽后退键事件
            }
        }
        return true;
    }
}
