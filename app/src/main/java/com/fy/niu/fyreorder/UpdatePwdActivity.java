package com.fy.niu.fyreorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdatePwdActivity extends AppCompatActivity {
    private EditText etPwdOld;
    private EditText etPwdNew;
    private EditText etPwdReNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pwd);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(UpdatePwdActivity.this);

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
            UpdatePwdActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        etPwdOld = (EditText) findViewById(R.id.etPwdOld);
        etPwdNew = (EditText) findViewById(R.id.etPwdNew);
        etPwdReNew = (EditText) findViewById(R.id.etPwdReNew);
    }

    public void doUpdatePwd(View view) {
        String oldPwd = etPwdOld.getText().toString();
        String newPwd = etPwdNew.getText().toString();
        String reNewPwd = etPwdReNew.getText().toString();
        if(ComFun.strNull(oldPwd.trim()) && ComFun.strNull(newPwd.trim()) && ComFun.strNull(reNewPwd.trim())){
            if(newPwd.trim().equals(reNewPwd.trim())){
                ComFun.showLoading(UpdatePwdActivity.this, "正在处理，请稍后");
                String userId = SharedPreferencesTool.getFromShared(UpdatePwdActivity.this, "fyLoginUserInfo", "userId");
                RequestParams params = new RequestParams();
                params.put("userId", userId);
                params.put("passOld", oldPwd.trim());
                params.put("passNew", newPwd.trim());
                ConnectorInventory.updateUserPass(UpdatePwdActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
                    @Override
                    public void onFinish() {
                        ComFun.hideLoading();
                    }

                    @Override
                    public void onSuccess(Object responseObj) {
                        try {
                            JSONObject resultJson = new JSONObject(responseObj.toString());
                            String code = resultJson.getString("code");
                            if(code.equals("ajaxSuccess")){
                                ComFun.showToast(UpdatePwdActivity.this, "修改密码成功，请重新登录", Toast.LENGTH_SHORT);
                                // 跳转到登录页面
                                SharedPreferencesTool.addOrUpdate(UpdatePwdActivity.this, "fyLoginUserInfo", "needLogin", true);
                                // 清空后退栈
                                ComFun.clearAllActiveActivity();
                                Intent loginIntent = new Intent(UpdatePwdActivity.this, LoginActivity.class);
                                UpdatePwdActivity.this.startActivity(loginIntent);
                            }else{
                                ComFun.showToast(UpdatePwdActivity.this, "修改密码失败，有可能旧密码不正确", Toast.LENGTH_LONG);
                            }
                        } catch (JSONException e) {}
                    }

                    @Override
                    public void onFailure(OkHttpException okHttpE) {
                        ComFun.showToast(UpdatePwdActivity.this, "修改密码失败，请稍后重试", Toast.LENGTH_LONG);
                    }
                }));
            }else{
                ComFun.showToast(UpdatePwdActivity.this, "两次输入的密码不一致，请重新输入", Toast.LENGTH_LONG);
            }
        }else{
            if(!ComFun.strNull(oldPwd.trim())){
                ComFun.showToast(UpdatePwdActivity.this, "请输入旧密码", Toast.LENGTH_SHORT);
            }else if(!ComFun.strNull(newPwd.trim())){
                ComFun.showToast(UpdatePwdActivity.this, "请输入新密码", Toast.LENGTH_SHORT);
            }else if(!ComFun.strNull(reNewPwd.trim())){
                ComFun.showToast(UpdatePwdActivity.this, "请再次输入新密码", Toast.LENGTH_SHORT);
            }
        }
    }

}
