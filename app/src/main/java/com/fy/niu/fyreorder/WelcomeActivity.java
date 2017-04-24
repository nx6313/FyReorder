package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.fragment.GuideFragment;
import com.fy.niu.fyreorder.util.ComFun;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private Handler mWelcomeHandler;
    private WelcomeTask mWelcomeTesk;

    private ViewPager welcomeViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<GuideFragment> mContents = new ArrayList<>();

    private LinearLayout guide_ll_point; // 放置圆点
    private TextView welcomeToAppMain;

    private List<Integer> welcomeImgIdList = new ArrayList<>(); // 图片资源的数组
    // 实例化原点View
    private ImageView iv_point;
    private ImageView[] ivPointArray;

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

        guide_ll_point = (LinearLayout) findViewById(R.id.guide_ll_point);
        guide_ll_point.setVisibility(View.GONE);

        welcomeToAppMain = (TextView) findViewById(R.id.welcomeToAppMain);
        welcomeToAppMain.setVisibility(View.GONE);

        // 添加欢迎页图片
        welcomeImgIdList.add(R.drawable.guide_1);
        welcomeImgIdList.add(R.drawable.guide_2);

        // 加载ViewPager
        initViewPager();

        // 加载底部圆点
        if(welcomeImgIdList.size() > 1){
            initPoint();
        }

        mWelcomeHandler = new Handler();
        mWelcomeTesk = new WelcomeTask();
        mWelcomeHandler.postDelayed(mWelcomeTesk, 2000);
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

    /**
     * 加载底部圆点
     */
    private void initPoint() {
        guide_ll_point.removeAllViews();
        // 根据ViewPager的item数量实例化数组
        ivPointArray = new ImageView[welcomeImgIdList.size()];

        // 循环新建底部圆点ImageView，将生成的ImageView保存到数组中
        int size = welcomeImgIdList.size();
        for (int i = 0;i<size;i++){
            iv_point = new ImageView(this);
            //iv_point.setLayoutParams(new ViewGroup.LayoutParams(40, 40));
            iv_point.setPadding(30, 0, 30, 0);//left,top,right,bottom
            ivPointArray[i] = iv_point;
            // 第一个页面需要设置为选中状态，这里采用两张不同的图片
            if (i == 0){
                iv_point.setBackgroundResource(R.drawable.pointer_full);
            }else{
                iv_point.setBackgroundResource(R.drawable.pointer_empty);
            }
            // 将数组中的ImageView加入到ViewGroup
            guide_ll_point.addView(ivPointArray[i]);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // 循环设置当前页的标记图
        int length = welcomeImgIdList.size();
        for (int i = 0;i<length;i++){
            ivPointArray[position].setBackgroundResource(R.drawable.pointer_full);
            if (position != i){
                ivPointArray[i].setBackgroundResource(R.drawable.pointer_empty);
            }
        }
        //判断是否是最后一页，若是则显示按钮
        if (position == welcomeImgIdList.size() - 1){
            welcomeToAppMain.setVisibility(View.VISIBLE);
            guide_ll_point.setVisibility(View.GONE);
        }else {
            welcomeToAppMain.setVisibility(View.GONE);
            guide_ll_point.setVisibility(View.VISIBLE);
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
            if(welcomeImgIdList.size() > 0 && true){
                welcomeViewPager.setVisibility(View.VISIBLE);
                guide_ll_point.setVisibility(View.VISIBLE);
                welcomeViewPager.setCurrentItem(0);

                if(welcomeImgIdList.size() == 1){
                    welcomeToAppMain.setVisibility(View.VISIBLE);
                }
            }else{
                // 直接进入登录页面
                Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(loginIntent);
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
