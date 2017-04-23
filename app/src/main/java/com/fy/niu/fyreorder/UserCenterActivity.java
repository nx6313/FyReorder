package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fy.niu.fyreorder.customView.CircularImage;
import com.fy.niu.fyreorder.util.ComFun;

public class UserCenterActivity extends AppCompatActivity {
    private CircularImage userHeadImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(UserCenterActivity.this);

        // 处理为标题居中
        ((TextView) toolbar.findViewById(R.id.toolbarTitleTv)).setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupActionBar();

        initView();
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
            UserCenterActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        userHeadImg = (CircularImage) findViewById(R.id.userHeadImg);
        userHeadImg.setImageResource(R.drawable.default_user_head_1);
    }

    /**
     * 跳转到个人资料
     * @param view
     */
    public void toUserData(View view){
        Intent userDataIntent = new Intent(UserCenterActivity.this, UserDataActivity.class);
        startActivity(userDataIntent);
    }

    /**
     * 跳转到已接订单
     * @param view
     */
    public void toHasReceiveOrder(View view){
        Intent hasReceiveOrderIntent = new Intent(UserCenterActivity.this, OrderActivity.class);
        startActivity(hasReceiveOrderIntent);
    }

    /**
     * 用户退出登录
     * @param view
     */
    public void toUserLoginOut(View view){
        // 清空后退栈
        ComFun.clearAllActiveActivity();
        Intent welcomeIntent = new Intent(UserCenterActivity.this, WelcomeActivity.class);
        startActivity(welcomeIntent);
    }

}
