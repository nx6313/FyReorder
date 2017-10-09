package com.fy.niu.fyreorder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.fy.niu.fyreorder.util.SharedPreferencesTool;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

        login_logo = (CircularImage) findViewById(R.id.login_logo);
        login_logo.setImageResource(R.drawable.jiang_pai);

        tvLoginName = (ClearEditText) findViewById(R.id.tvLoginName);
        tvLoginPwd = (ClearEditText) findViewById(R.id.tvLoginPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
        aa.setInterpolator(new AccelerateInterpolator());
        aa.setDuration(800);
        login_layout.startAnimation(aa);

        // 登录按钮事件
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ComFun.strNull(tvLoginName.getText().toString()) && ComFun.strNull(tvLoginPwd.getText().toString())){
                    btnLogin.requestFocus();
                    XGPushManager.registerPush(LoginActivity.this, new XGIOperateCallback() {
                        @Override
                        public void onSuccess(Object data, int i) {
                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyBaseData", "userToken", data.toString());
                            Log.d("TPush", "登录时，注册成功，设备token为：" + data);
                            ComFun.showLoading(LoginActivity.this, "登陆中，请稍后...", false);
                            // 执行登录任务
                            String devToken = SharedPreferencesTool.getFromShared(LoginActivity.this, "fyBaseData", "userToken");
                            RequestParams params = new RequestParams();
                            Log.d("登陆中 ====== ", "设备 Token：" + devToken);
                            params.put("token", devToken);
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
                                            // 保存登录用户信息
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "userId", data.getString("userId"));
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "userLoginName", tvLoginName.getText().toString().trim());
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "userLoginPass", tvLoginPwd.getText().toString().trim());
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "ifGive", data.getString("ifGive"));
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "ifOpen", data.getString("ifOpen"));
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "floor", data.getString("floor"));
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "floorName", data.getString("floorName"));
                                            SharedPreferencesTool.addOrUpdate(LoginActivity.this, "fyLoginUserInfo", "needLogin", false);
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
                        }

                        @Override
                        public void onFail(Object data, int errCode, String msg) {
                            Log.d("TPush", "登录时，注册失败，错误码：" + errCode + ",错误信息：" + msg);
                            ComFun.showToast(LoginActivity.this, "登录推送注册异常，请稍后重试", Toast.LENGTH_LONG);
                        }
                    });
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
