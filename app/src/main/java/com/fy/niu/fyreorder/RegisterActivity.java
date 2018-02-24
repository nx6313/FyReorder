package com.fy.niu.fyreorder;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.model.School;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etUserPhone;
    private EditText etUserSchool;
    private EditText etUserPerl; // 专业
    private EditText etUserDorm;
    private EditText etUserDormNo;

    private Map<String, School> schoolMap = new HashMap<>();

    private String userSchoolId = null;
    private String userSchool = null;
    private String userDormId = null;
    private String userDorm = null;

    private ListPopupWindow listPopupWindow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏和状态栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉状态栏
        setContentView(R.layout.activity_register);

        initView();
        initData();
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
        ConnectorInventory.getSchoolData(RegisterActivity.this, null, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject resultJson = new JSONObject(responseObj.toString());
                    Boolean success = resultJson.getBoolean("success");
                    if (success) {
                        JSONArray schoolArr = resultJson.getJSONObject("body").getJSONArray("universityList");
                        for (int i = 0; i < schoolArr.length(); i++) {
                            JSONObject schoolDataObj = schoolArr.getJSONObject(i);
                            School school = new School();
                            school.setModel(schoolDataObj.getInt("model"));
                            school.setGrade(schoolDataObj.getInt("grade"));
                            school.setOrgroleId(schoolDataObj.getString("orgroleId"));
                            school.setPid(schoolDataObj.getString("pid"));
                            school.setOrgName(schoolDataObj.getString("orgName"));
                            schoolMap.put(schoolDataObj.getString("orgName"), school);
                        }
                        etUserSchool.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (!s.toString().trim().equals("")) {
                                    // 获取近似项
                                    List<School> schools = getNearSchoolList(s.toString().trim());
                                    if (schools.size() > 0) {
                                        if (listPopupWindow == null || (listPopupWindow != null && !listPopupWindow.isShowing())) {
                                            listPopupWindow = new ListPopupWindow(RegisterActivity.this);
                                            listPopupWindow.setAnchorView(etUserSchool);
                                            listPopupWindow.setHeight(400);
                                            listPopupWindow.show();
                                            listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                                @Override
                                                public void onDismiss() {
                                                    String curStr = etUserSchool.getText().toString().trim();
                                                    if (userSchoolId == null) {
                                                        etUserSchool.setText("");
                                                    } else {
                                                        if (!curStr.equals("") && !curStr.equals(userSchool)) {
                                                            etUserSchool.setText(userSchool);
                                                            listPopupWindow.dismiss();
                                                        } else if (curStr.equals("")) {
                                                            userSchoolId = null;
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        listPopupWindow.setAdapter(new SchoolListAdapter(schools));
                                    }
                                } else {
                                    if (listPopupWindow != null && listPopupWindow.isShowing()) {
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
                                if (!s.toString().trim().equals("")) {
                                    if (userSchoolId == null) {
                                        ComFun.showToast(RegisterActivity.this, "请先输入所在院校", Toast.LENGTH_SHORT);
                                        etUserDorm.setText("");
                                    } else {
                                        // 获取近似项
                                        RequestParams params = new RequestParams();
                                        params.put("", "");
                                        ConnectorInventory.getDromDataBySchool(RegisterActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
                                            @Override
                                            public void onFinish() {
                                            }

                                            @Override
                                            public void onSuccess(Object responseObj) {

                                            }

                                            @Override
                                            public void onFailure(OkHttpException okHttpE) {
                                                ComFun.showToast(RegisterActivity.this, "获取楼号信息失败，请稍后重试", Toast.LENGTH_LONG);
                                            }
                                        }));

                                        if (listPopupWindow == null || (listPopupWindow != null && !listPopupWindow.isShowing())) {
                                            listPopupWindow = new ListPopupWindow(RegisterActivity.this);
                                            listPopupWindow.setAnchorView(etUserDorm);
                                            listPopupWindow.setHeight(400);
                                            listPopupWindow.show();
                                            listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                                @Override
                                                public void onDismiss() {
                                                    String curStr = etUserDorm.getText().toString().trim();
                                                    if (userDormId == null) {
                                                        etUserDorm.setText("");
                                                    } else {
                                                        if (!curStr.equals("") && !curStr.equals(userDorm)) {
                                                            etUserDorm.setText(userDorm);
                                                            listPopupWindow.dismiss();
                                                        } else if (curStr.equals("")) {
                                                            userDormId = null;
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        //listPopupWindow.setAdapter(new ListAdapter());
                                    }
                                } else {
                                    if (listPopupWindow != null && listPopupWindow.isShowing()) {
                                        listPopupWindow.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                ComFun.showToast(RegisterActivity.this, "获取院校信息失败，请稍后重试", Toast.LENGTH_LONG);
            }
        }));
    }

    private List<School> getNearSchoolList(String searchStr) {
        List<School> searchSchools = new ArrayList<>();
        searchStr = "山西";
        for (Map.Entry<String, School> m : schoolMap.entrySet()) {
            if (m.getKey().contains(searchStr)) {
                searchSchools.add(m.getValue());
            }
        }
        return searchSchools;
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

    class SchoolListAdapter extends BaseAdapter {
        List<School> schools = new ArrayList<>();

        public SchoolListAdapter(List<School> schoolList) {
            schools = schoolList;
        }

        @Override
        public int getCount() {
            return schools.size();
        }

        @Override
        public Object getItem(int position) {
            return schools.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
            View view = inflater.inflate(R.layout.school_info_item, null);
            TextView schollTv = (TextView) view.findViewById(R.id.schoolInfoItem);
            schollTv.setTag(schools.get(position).getOrgroleId());
            schollTv.setText(schools.get(position).getOrgName());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView schollClickTv = (TextView) v.findViewById(R.id.schoolInfoItem);
                    userSchoolId = schollClickTv.getTag().toString().trim();
                    userSchool = schollClickTv.getText().toString().trim();
                    etUserSchool.setText(userSchool);
                    if (listPopupWindow != null && listPopupWindow.isShowing()) {
                        listPopupWindow.dismiss();
                    }
                }
            });
            return view;
        }
    }
}
