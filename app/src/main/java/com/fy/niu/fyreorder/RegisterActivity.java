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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.customView.ElasticScrollView;
import com.fy.niu.fyreorder.model.Dorm;
import com.fy.niu.fyreorder.model.School;
import com.fy.niu.fyreorder.okHttpUtil.exception.OkHttpException;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataHandle;
import com.fy.niu.fyreorder.okHttpUtil.listener.DisposeDataListener;
import com.fy.niu.fyreorder.okHttpUtil.request.RequestParams;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.ConnectorInventory;
import com.fy.niu.fyreorder.util.Constants;
import com.fy.niu.fyreorder.util.DisplayUtil;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private RelativeLayout registerPageInputLayout;
    private RelativeLayout registerPagePayLayout;
    private LinearLayout registerPayBfLayout;
    private ElasticScrollView registerDoc;
    private RelativeLayout registerPayLayout;
    private Button btnRegisterCloseDoc;
    private CheckBox cbRegisterReadDoc;

    private EditText etUserName;
    private EditText etUserPhone;
    private EditText etUserSchool;
    private EditText etUserPerl; // 专业
    private EditText etUserDorm;
    private RadioGroup rgUserSex;

    private Map<String, School> schoolMap = new HashMap<>();

    private SchoolListAdapter schoolListAdapter = null;
    private RoomListAdapter roomListAdapter = null;
    private List<School> schoolAdapterList = new ArrayList<>();
    private List<Dorm> roomAdapterList = new ArrayList<>();

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
        registerPageInputLayout = (RelativeLayout) findViewById(R.id.registerPageInputLayout);
        registerPagePayLayout = (RelativeLayout) findViewById(R.id.registerPagePayLayout);
        registerPageInputLayout.setVisibility(View.VISIBLE);
        registerPagePayLayout.setVisibility(View.GONE);
        registerPayBfLayout = (LinearLayout) findViewById(R.id.registerPayBfLayout);
        registerDoc = (ElasticScrollView) findViewById(R.id.registerDoc);
        registerPayLayout = (RelativeLayout) findViewById(R.id.registerPayLayout);
        btnRegisterCloseDoc = (Button) findViewById(R.id.btnRegisterCloseDoc);
        cbRegisterReadDoc = (CheckBox) findViewById(R.id.cbRegisterReadDoc);

        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserPhone = (EditText) findViewById(R.id.etUserPhone);
        etUserSchool = (EditText) findViewById(R.id.etUserSchool);
        etUserPerl = (EditText) findViewById(R.id.etUserPerl);
        etUserDorm = (EditText) findViewById(R.id.etUserDorm);
        rgUserSex = (RadioGroup) findViewById(R.id.rgUserSex);
    }

    private void initData() {
        ComFun.showLoading(RegisterActivity.this, "正在获取学院信息，请稍后");
        ConnectorInventory.getSchoolData(RegisterActivity.this, null, new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading();
            }

            @Override
            public void onSuccess(Object responseObj) {
                etUserSchool.setFocusable(true);
                //etUserSchool.setEnabled(true);
                etUserSchool.setHint("请输入所在学院");
                etUserSchool.setOnClickListener(null);
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
                                            schoolListAdapter = new SchoolListAdapter();
                                            listPopupWindow.setAdapter(schoolListAdapter);
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
                                        schoolAdapterList = schools;
                                        schoolListAdapter.notifyDataSetChanged();
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
                        etUserDorm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ComFun.closeIME(RegisterActivity.this, etUserDorm);
                                if (userSchoolId == null) {
                                    ComFun.showToast(RegisterActivity.this, "请先输入所在院校", Toast.LENGTH_SHORT);
                                    etUserDorm.setText("");
                                    roomAdapterList.clear();
                                } else {
                                    if (listPopupWindow == null || (listPopupWindow != null && !listPopupWindow.isShowing())) {
                                        listPopupWindow = new ListPopupWindow(RegisterActivity.this);
                                        listPopupWindow.setAnchorView(etUserDorm);
                                        listPopupWindow.setHeight(400);
                                        roomListAdapter = new RoomListAdapter();
                                        listPopupWindow.setAdapter(roomListAdapter);
                                        listPopupWindow.show();
                                        listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                            @Override
                                            public void onDismiss() {
                                                String curStr = etUserDorm.getText().toString().trim();
                                                if (userDormId == null) {
                                                    etUserDorm.setText("");
                                                    roomAdapterList.clear();
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
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                etUserSchool.setFocusable(false);
                //etUserSchool.setEnabled(false);
                etUserSchool.setHint("点这里重新获取院校信息");
                etUserSchool.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initData();
                    }
                });
                etUserDorm.setFocusable(false);
                etUserDorm.setEnabled(false);
                if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(RegisterActivity.this, "获取院校信息超时，请稍后重试", Toast.LENGTH_LONG);
                } else {
                    ComFun.showToast(RegisterActivity.this, "获取院校信息失败，请稍后重试", Toast.LENGTH_LONG);
                }
            }
        }));
    }

    private List<School> getNearSchoolList(String searchStr) {
        List<School> searchSchools = new ArrayList<>();
        for (Map.Entry<String, School> m : schoolMap.entrySet()) {
            if (m.getKey().contains(searchStr)) {
                searchSchools.add(m.getValue());
            }
        }
        return searchSchools;
    }

    // 跳转到下一步
    public void toRegisterNext(View view) {
        if (!ComFun.strNull(etUserName.getText().toString())) {
            ComFun.showToast(RegisterActivity.this, "用户名不能为空", Toast.LENGTH_LONG);
            return;
        }
        if (!ComFun.strNull(etUserPhone.getText().toString())) {
            ComFun.showToast(RegisterActivity.this, "手机号不能为空", Toast.LENGTH_LONG);
            return;
        }
        if (!ComFun.strNull(userSchoolId)) {
            ComFun.showToast(RegisterActivity.this, "所在院校不能为空", Toast.LENGTH_LONG);
            return;
        }
        if (!ComFun.strNull(etUserPerl.getText().toString())) {
            ComFun.showToast(RegisterActivity.this, "专业不能为空", Toast.LENGTH_LONG);
            return;
        }
        if (!ComFun.strNull(userDormId)) {
            ComFun.showToast(RegisterActivity.this, "楼号不能为空", Toast.LENGTH_LONG);
            return;
        }
        registerPageInputLayout.setVisibility(View.GONE);
        registerPagePayLayout.setVisibility(View.VISIBLE);
    }

    // 显示用户协议
    public void showRegisterDoc(View view) {
        registerDoc.setVisibility(View.VISIBLE);
        registerPayLayout.setVisibility(View.GONE);
        btnRegisterCloseDoc.setVisibility(View.VISIBLE);
        registerPayBfLayout.setVisibility(View.GONE);
    }

    // 关闭用户协议
    public void closeRegisterDoc(View view) {
        registerDoc.setVisibility(View.GONE);
        registerPayLayout.setVisibility(View.VISIBLE);
        btnRegisterCloseDoc.setVisibility(View.GONE);
        registerPayBfLayout.setVisibility(View.VISIBLE);
    }

    // 进行注册
    public void doRegister(View view) {
        if (cbRegisterReadDoc.isChecked()) {
            ComFun.showToast(RegisterActivity.this, "微信支付正在审核中，敬请期待", Toast.LENGTH_LONG);
//            ComFun.showLoading(RegisterActivity.this, "正在注册，请稍后");
//            RequestParams params = new RequestParams();
//            params.put("name", etUserName.getText().toString().trim());
//            params.put("telephone", etUserPhone.getText().toString().trim());
//            params.put("orgId", userSchoolId);
//            params.put("floorId", userDormId);
//            params.put("major", etUserPerl.getText().toString().trim());
//            params.put("sex", rgUserSex.findViewById(rgUserSex.getCheckedRadioButtonId()).getTag().toString().trim());
//            ConnectorInventory.userRegister(RegisterActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
//                @Override
//                public void onFinish() {
//                    ComFun.hideLoading();
//                }
//
//                @Override
//                public void onSuccess(Object responseObj) {
//                    try {
//                        JSONObject resultJson = new JSONObject(responseObj.toString());
//                        Boolean success = resultJson.getBoolean("success");
//                        ComFun.showToast(RegisterActivity.this, resultJson.getString("msg"), Toast.LENGTH_LONG);
//                        if (success) {
//                            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
//                            startActivity(loginIntent);
//                            RegisterActivity.this.finish();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(OkHttpException okHttpE) {
//                    if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
//                        ComFun.showToast(RegisterActivity.this, "注册超时，请稍后重试", Toast.LENGTH_LONG);
//                    } else {
//                        ComFun.showToast(RegisterActivity.this, "注册失败，请稍后重试", Toast.LENGTH_LONG);
//                    }
//                }
//            }));
        } else {
            ComFun.showToast(RegisterActivity.this, "请您先阅读《速达接单用户协议》", Toast.LENGTH_LONG);
        }
    }

    // 返回登录
    public void backToLogin(View view) {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        RegisterActivity.this.finish();
    }

    class SchoolListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return schoolAdapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return schoolAdapterList.get(position);
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
            schollTv.setTag(schoolAdapterList.get(position).getOrgroleId());
            schollTv.setText(schoolAdapterList.get(position).getOrgName());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView schollClickTv = (TextView) v.findViewById(R.id.schoolInfoItem);
                    userSchoolId = schollClickTv.getTag().toString().trim();
                    userSchool = schollClickTv.getText().toString().trim();
                    etUserSchool.setText(userSchool);

                    ComFun.showLoading(RegisterActivity.this, "正在获取宿舍楼信息，请稍后");
                    RequestParams params = new RequestParams();
                    params.put("orgroleId", userSchoolId);
                    ConnectorInventory.getDromDataBySchool(RegisterActivity.this, params, new DisposeDataHandle(new DisposeDataListener() {
                        @Override
                        public void onFinish() {
                            ComFun.hideLoading();
                        }

                        @Override
                        public void onSuccess(Object responseObj) {
                            try {
                                JSONObject resultJson = new JSONObject(responseObj.toString());
                                Boolean success = resultJson.getBoolean("success");
                                if (success) {
                                    roomAdapterList.clear();
                                    JSONArray dromArr = resultJson.getJSONObject("body").getJSONArray("floorList");
                                    for (int i = 0; i < dromArr.length(); i++) {
                                        JSONObject dromDataObj = dromArr.getJSONObject(i);
                                        Dorm dorm = new Dorm();
                                        dorm.setOrgId(dromDataObj.getString("orgId"));
                                        dorm.setFloorId(dromDataObj.getString("floorId"));
                                        dorm.setFloor(dromDataObj.getString("floor"));
                                        roomAdapterList.add(dorm);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(OkHttpException okHttpE) {
                            if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                                ComFun.showToast(RegisterActivity.this, "获取宿舍楼信息超时，请稍后重试", Toast.LENGTH_LONG);
                            } else {
                                ComFun.showToast(RegisterActivity.this, "获取宿舍楼信息失败，请稍后重试", Toast.LENGTH_LONG);
                            }
                        }
                    }));
                    if (listPopupWindow != null && listPopupWindow.isShowing()) {
                        listPopupWindow.dismiss();
                    }
                }
            });
            return view;
        }
    }

    class RoomListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return roomAdapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return roomAdapterList.get(position);
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
            schollTv.setTag(roomAdapterList.get(position).getFloorId());
            schollTv.setText(roomAdapterList.get(position).getFloor());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView schollClickTv = (TextView) v.findViewById(R.id.schoolInfoItem);
                    userDormId = schollClickTv.getTag().toString().trim();
                    userDorm = schollClickTv.getText().toString().trim();
                    etUserDorm.setText(userDormId);
                    if (listPopupWindow != null && listPopupWindow.isShowing()) {
                        listPopupWindow.dismiss();
                    }
                }
            });
            return view;
        }
    }
}
