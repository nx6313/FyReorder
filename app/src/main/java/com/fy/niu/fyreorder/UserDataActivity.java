package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fy.niu.fyreorder.customView.CircularImage;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.DBOpenHelper;
import com.fy.niu.fyreorder.util.DBUtil;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONException;
import org.json.JSONObject;

public class UserDataActivity extends AppCompatActivity {
    private CircularImage userDataHeadImg;
    private TextView tvUserInfoName;
    private TextView tvUserInfoSchool;
    private TextView tvUserInfoPhone;
    private LinearLayout userInfoFloorName;
    private TextView tvUserInfoFloor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(UserDataActivity.this);

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
            UserDataActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        userDataHeadImg = (CircularImage) findViewById(R.id.userDataHeadImg);
        userDataHeadImg.setImageResource(R.drawable.default_user_head);

        tvUserInfoName = (TextView) findViewById(R.id.tvUserInfoName);
        tvUserInfoSchool = (TextView) findViewById(R.id.tvUserInfoSchool);
        tvUserInfoPhone = (TextView) findViewById(R.id.tvUserInfoPhone);

        userInfoFloorName = (LinearLayout) findViewById(R.id.userInfoFloorName);
        tvUserInfoFloor = (TextView) findViewById(R.id.tvUserInfoFloor);
        String ifGive = SharedPreferencesTool.getFromShared(UserDataActivity.this, "fyLoginUserInfo", "ifGive");
        if(ifGive.equals("0")){
            userInfoFloorName.setVisibility(View.VISIBLE);
        }else{
            userInfoFloorName.setVisibility(View.GONE);
        }

        JSONObject userInfo = DBUtil.find(new DBOpenHelper(UserDataActivity.this), "userInfo", new String[] { "userName", "floorName", "telephone", "orgName" },
                null, null, null, null, null, null);
        try {
            tvUserInfoName.setText(userInfo.getString("userName"));
            tvUserInfoSchool.setText(userInfo.getString("orgName"));
            tvUserInfoPhone.setText(userInfo.getString("telephone"));
            if(ifGive.equals("0")){
                tvUserInfoFloor.setText(userInfo.getString("floorName"));
            }
        } catch (JSONException e) {}
    }

    public void updateUserPassword(View view) {
        Intent updatePwdIntent = new Intent(UserDataActivity.this, UpdatePwdActivity.class);
        startActivity(updatePwdIntent);
    }

}
