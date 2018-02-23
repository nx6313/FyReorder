package com.fy.niu.fyreorder;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etUserPhone;
    private EditText etUserSchool;
    private EditText etUserPerl; // 专业
    private EditText etUserDorm;
    private EditText etUserDormNo;

    private String userSchool = null;
    private String userPerl = null;

    private ListPopupWindow listPopupWindow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ComFun.addToActiveActivityList(RegisterActivity.this);

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

    private void initView() {
        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserPhone = (EditText) findViewById(R.id.etUserPhone);
        etUserSchool = (EditText) findViewById(R.id.etUserSchool);
        etUserPerl = (EditText) findViewById(R.id.etUserPerl);
        etUserDorm = (EditText) findViewById(R.id.etUserDorm);
        etUserDormNo = (EditText) findViewById(R.id.etUserDormNo);
    }

    private void initData() {
        RequestParams params = new RequestParams();
        ConnectorInventory.getSchoolData(RegisterActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
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

                    }
                } catch (JSONException e) {}
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.showToast(RegisterActivity.this, "获取院校信息失败，请稍后重试", Toast.LENGTH_LONG);
            }
        }));
        etUserSchool.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim() != "") {
                    // 获取近似项
                    listPopupWindow = new ListPopupWindow(RegisterActivity.this);
                    listPopupWindow.setAnchorView(etUserSchool);
                    listPopupWindow.setHeight(100);
                    listPopupWindow.setAdapter(new ListAdapter());
                    listPopupWindow.show();
                } else {
                    if(listPopupWindow != null && !listPopupWindow.isShowing()) {
                        listPopupWindow.dismiss();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etUserDorm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim() != "") {
                    // 获取近似项
                } else {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // 进行注册
    public void doRegister(View view) {

    }

    // 返回登录
    public void backToLogin(View view) {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        RegisterActivity.this.finish();
    }

    class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}
