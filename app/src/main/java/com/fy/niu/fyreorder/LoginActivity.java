package com.fy.niu.fyreorder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fy.niu.fyreorder.customView.CircularImage;
import com.fy.niu.fyreorder.customView.ClearEditText;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.Constants;
import com.fy.niu.fyreorder.util.UserDataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

public class LoginActivity extends AppCompatActivity {
    private RelativeLayout login_layout;
    private CircularImage login_logo;
    private Handler mLoginHandler;
    private LoginTask mLoginTesk;

    private ClearEditText tvLoginName;
    private ClearEditText tvLoginPwd;
    private Button btnLogin;
    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComFun.addToActiveActivityList(LoginActivity.this);

        // 隐藏标题栏和状态栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉状态栏
        setContentView(R.layout.activity_login);

        login_layout = (RelativeLayout) findViewById(R.id.login_layout);
        //login_layout.setVisibility(View.GONE);

        //login_logo = (CircularImage) findViewById(R.id.login_logo);
        //login_logo.setImageResource(R.drawable.jiang_pai);

        tvLoginName = (ClearEditText) findViewById(R.id.tvLoginName);
        tvLoginPwd = (ClearEditText) findViewById(R.id.tvLoginPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
        aa.setInterpolator(new AccelerateInterpolator());
        aa.setDuration(800);
        login_layout.startAnimation(aa);

        // JPush请求权限
        JPushInterface.requestPermission(LoginActivity.this);

        // 登录按钮事件
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ComFun.strNull(tvLoginName.getText().toString()) && ComFun.strNull(tvLoginPwd.getText().toString())){
                    btnLogin.requestFocus();

                    ComFun.showLoading(LoginActivity.this, "登陆中，请稍后...", false);
                    // 执行登录任务
                    RequestParams params = new RequestParams();
                    params.put("loginName", tvLoginName.getText().toString().trim());
                    params.put("passWord", tvLoginPwd.getText().toString().trim());
                    ConnectorInventory.userLogin(LoginActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
                        @Override
                        public void onFinish() {
                            ComFun.hideLoading();
                        }

                        @Override
                        public void onSuccess(Object responseObj) {
                            try {
                                Log.d("登录成功，用户信息", responseObj.toString());
                                JSONObject data = new JSONObject(responseObj.toString());
                                if(data.get("result").equals("success")){
                                    JPushInterface.setAlias(LoginActivity.this, Constants.JPUSH_SEQUENCE, data.getString("userId"));
                                    // 保存登录用户信息
                                    UserDataUtil.setUserId(LoginActivity.this, data.getString(UserDataUtil.key_userId));
                                    UserDataUtil.saveUserData(LoginActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_userLoginName, tvLoginName.getText().toString().trim());
                                    UserDataUtil.saveUserData(LoginActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_userLoginPass, tvLoginPwd.getText().toString().trim());
                                    UserDataUtil.saveUserData(LoginActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_ifGive, data.getString("ifGive"));
                                    UserDataUtil.saveUserData(LoginActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_ifOpen, data.getString("ifOpen"));
                                    UserDataUtil.saveUserData(LoginActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_floor, data.getString("floor"));
                                    UserDataUtil.saveUserData(LoginActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_floorName, data.getString("floorName"));
                                    UserDataUtil.saveUserData(LoginActivity.this, UserDataUtil.fyLoginUserInfo, UserDataUtil.key_needLogin, false);
                                    ComFun.showToast(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT);
                                    mLoginHandler = new Handler();
                                    mLoginTesk = new LoginTask();
                                    mLoginHandler.postDelayed(mLoginTesk, 1000);
                                }else{
                                    ComFun.showToast(LoginActivity.this, "账号或密码错误", Toast.LENGTH_LONG);
                                }
                            } catch (JSONException e) {}
                        }

                        @Override
                        public void onFailure(OkHttpException okHttpE) {
                            ComFun.showToast(LoginActivity.this, "登录异常，请稍后重试", Toast.LENGTH_LONG);
                        }
                    }));
                }else{
                    if(!ComFun.strNull(tvLoginName.getText().toString())){
                        ComFun.showToast(LoginActivity.this, "请输入登录账号", Toast.LENGTH_SHORT);
                        tvLoginName.requestFocus();
                        tvLoginName.setHintTextColor(Color.parseColor("#E14D49"));
                    }else if(!ComFun.strNull(tvLoginPwd.getText().toString())){
                        ComFun.showToast(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT);
                        tvLoginPwd.requestFocus();
                        tvLoginPwd.setHintTextColor(Color.parseColor("#E14D49"));
                    }
                }
            }
        });
    }

    // 用户注册事件触发
    public void userRegister(View view) {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        LoginActivity.this.finish();
    }

    /**
     * 登录页定时跳转程序任务
     */
    class LoginTask implements Runnable {

        @Override
        public void run() {
            ComFun.hideLoading();
            full(false);
            Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainActivityIntent);
            LoginActivity.this.finish();
        }
    }

    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                v.clearFocus();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                ComFun.showToast(this, "再按一次离开", 2000);
                exitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
        }
        return true;
    }
}
