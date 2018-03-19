package com.fy.niu.fyreorder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.broadcastReceiver.FyBroadcastReceiver;
import com.fy.niu.fyreorder.customView.ElasticScrollView;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.DisplayUtil;
import com.fy.niu.fyreorder.util.MyApplication;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrintDialogActivity extends Activity {
    public static Handler mHandler = null;
    private CheckBox cbPrint;
    private TextView tvPrintTip;
    private Button btnAgainConnectPrint;
    private Button btnReConnectPrint;
    private TextView tvPrintCode;
    private Button btnConnectPrint;
    private TextView tvPrintListening;
    private ElasticScrollView esvOrderWaiter;
    private LinearLayout printingOrderDataLayout;

    private PopupWindow mPopupWindow = null;

    private static final int OPEN_BLUETOOTH = 1; // activity result

    private static final int MSG_SHOW_LOADING = 1; // handle
    private static final int MSG_HIDE_LOADING = 2;
    public static final int MSG_CONNECTION_DIS = 3;

    private List<String> searchGetBluetoothAddressList = new ArrayList<>(); // 保存当前搜索到的蓝牙设备地址，避免重复
    private List<byte[]> writeDataList = new ArrayList<>(); // 要打印的数据字节数组集合

    private String bluetoothStatus;

    public PrintDialogActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_dialog);

        ComFun.addToActiveActivityList(PrintDialogActivity.this);

        initView();
    }

    private void initView() {
        // 设置弹框Activity页面大小
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.alpha = 1.0f; //设置本身透明度
        p.dimAmount = 0.6f; //设置黑暗度
        p.gravity = Gravity.CENTER;
        getWindow().setAttributes(p); //设置生效
        getWindow().setGravity(Gravity.CENTER);

        // 找到设备的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        // 搜索完成的广播
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter2);

        mHandler = new PrintDialogActivity.mHandler();

        if (MyApplication.mBluetoothAdapter == null) {
            MyApplication.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        cbPrint = (CheckBox) findViewById(R.id.cbPrint);
        tvPrintTip = (TextView) findViewById(R.id.tvPrintTip);
        btnAgainConnectPrint = (Button) findViewById(R.id.btnAgainConnectPrint);
        btnReConnectPrint = (Button) findViewById(R.id.btnReConnectPrint);
        tvPrintCode = (TextView) findViewById(R.id.tvPrintCode);
        btnConnectPrint = (Button) findViewById(R.id.btnConnectPrint);
        tvPrintListening = (TextView) findViewById(R.id.tvPrintListening);
        esvOrderWaiter = (ElasticScrollView) findViewById(R.id.esvOrderWaiter);
        printingOrderDataLayout = (LinearLayout) findViewById(R.id.printingOrderDataLayout);
        cbPrint.setChecked(false);
        tvPrintTip.setVisibility(View.GONE);
        btnAgainConnectPrint.setVisibility(View.GONE);
        btnReConnectPrint.setVisibility(View.GONE);
        tvPrintCode.setVisibility(View.GONE);
        btnConnectPrint.setVisibility(View.GONE);
        tvPrintListening.setVisibility(View.GONE);
        esvOrderWaiter.setVisibility(View.GONE);
        printingOrderDataLayout.setVisibility(View.GONE);

        cbPrint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesTool.addOrUpdate(PrintDialogActivity.this, "systemSet", "printIsOpen", isChecked);
                if (isChecked) {
                    if (MyApplication.mBluetoothAdapter != null) {
                        // 注册蓝牙监听
                        PrintDialogActivity.this.registerReceiver(mReceiver, makeFilter());
                        tvPrintTip.setVisibility(View.VISIBLE);
                        tvPrintCode.setVisibility(View.VISIBLE);
                        final String connectionDeviceCode = SharedPreferencesTool.getFromShared(PrintDialogActivity.this, "systemSet", "connectionDeviceCode", "");
                        if (ComFun.strNull(connectionDeviceCode)) {
                            if (MyApplication.mBluetoothAdapter.isEnabled()) {
                                BluetoothSocket curBluetoothSocket = MyApplication.mBluetoothSocketMap.get(connectionDeviceCode);
                                if (ComFun.strNull(curBluetoothSocket)) {
                                    if (curBluetoothSocket.isConnected()) {
                                        tvPrintCode.setText(connectionDeviceCode);
                                        btnAgainConnectPrint.setVisibility(View.GONE);
                                        btnReConnectPrint.setVisibility(View.VISIBLE);
                                        btnConnectPrint.setVisibility(View.GONE);
//                                        tvPrintListening.setVisibility(View.VISIBLE);
//                                        esvOrderWaiter.setVisibility(View.VISIBLE);
                                        printingOrderDataLayout.setVisibility(View.VISIBLE);
                                        printingOrderDataLayout.removeAllViews();
                                        return;
                                    }
                                }
                                tvPrintCode.setText("正在连接中...");

                                Message mainPageUpdateMenuMsg = new Message();
                                Bundle mainPageUpdateMenuData = new Bundle();
                                mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                                mainPageUpdateMenuData.putString("status", "正在连接中...");
                                mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                                MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ConnectRunnable connectRunnable = new ConnectRunnable(connectionDeviceCode);
                                        new Thread(connectRunnable).start();
                                    }
                                }, 200);
                            } else {
                                tvPrintCode.setText("蓝牙未启用，打票机无连接");
                                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(intent, OPEN_BLUETOOTH);

                                Message mainPageUpdateMenuMsg = new Message();
                                Bundle mainPageUpdateMenuData = new Bundle();
                                mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                                mainPageUpdateMenuData.putString("status", "蓝牙未启用");
                                mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                                MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                            }
                        } else {
                            tvPrintCode.setText("无打票机连接");
                            btnConnectPrint.setVisibility(View.VISIBLE);

                            Message mainPageUpdateMenuMsg = new Message();
                            Bundle mainPageUpdateMenuData = new Bundle();
                            mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                            mainPageUpdateMenuData.putString("status", "未设置");
                            mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                            MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                        }
                    } else {
                        cbPrint.setChecked(false);
                        SharedPreferencesTool.addOrUpdate(PrintDialogActivity.this, "systemSet", "printIsOpen", false);
                        ComFun.showToast(PrintDialogActivity.this, "对不起，您的设备不支持蓝牙", Toast.LENGTH_SHORT);

                        Message mainPageUpdateMenuMsg = new Message();
                        Bundle mainPageUpdateMenuData = new Bundle();
                        mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                        mainPageUpdateMenuData.putString("status", "不支持蓝牙");
                        mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                        MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                    }
                } else {
                    tvPrintTip.setVisibility(View.GONE);
                    btnAgainConnectPrint.setVisibility(View.GONE);
                    btnReConnectPrint.setVisibility(View.GONE);
                    tvPrintCode.setVisibility(View.GONE);
                    btnConnectPrint.setVisibility(View.GONE);
//                    tvPrintListening.setVisibility(View.GONE);
//                    esvOrderWaiter.setVisibility(View.GONE);
                    printingOrderDataLayout.setVisibility(View.GONE);
                    for (Map.Entry<String, BluetoothSocket> m : MyApplication.mBluetoothSocketMap.entrySet()) {
                        if (m.getValue().isConnected()) {
                            try {
                                m.getValue().close();
                            } catch (IOException e) {
                            }
                        }
                    }
                    Message mainPageUpdateMenuMsg = new Message();
                    Bundle mainPageUpdateMenuData = new Bundle();
                    mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                    mainPageUpdateMenuData.putString("status", "未开启");
                    mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                    MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                }
            }
        });

        Boolean printIsOpenInSave = SharedPreferencesTool.getBooleanFromShared(PrintDialogActivity.this, "systemSet", "printIsOpen");
        if (printIsOpenInSave) {
            if (MyApplication.mBluetoothAdapter != null) {
                cbPrint.setChecked(true);
            } else {
                ComFun.showToast(PrintDialogActivity.this, "对不起，您的设备不支持蓝牙", Toast.LENGTH_SHORT);

                Message mainPageUpdateMenuMsg = new Message();
                Bundle mainPageUpdateMenuData = new Bundle();
                mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                mainPageUpdateMenuData.putString("status", "不支持蓝牙");
                mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除蓝牙广播
        unregisterReceiver(mReceiver);
    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.e("TAG", "TURNING_ON");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            bluetoothStatus = "on"; // 蓝牙已打开
                            Log.e("TAG", "STATE_ON");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.e("TAG", "STATE_TURNING_OFF");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            bluetoothStatus = "off"; // 蓝牙已关闭
                            Log.e("TAG", "STATE_OFF");
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    // 发现设备
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // 判断是否配对过
                    final RadioGroup blueDevItemWrap = (RadioGroup) mPopupWindow.getContentView().findViewById(R.id.blueDevItemWrap);
                    View blueDevItemView = LayoutInflater.from(PrintDialogActivity.this).inflate(R.layout.blue_dev_item, null);
                    RadioButton rbBlueDev = (RadioButton) blueDevItemView.findViewWithTag("rbBlueDev");
                    rbBlueDev.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            btnConnectPrint.setVisibility(View.GONE);
                            final String curSelectedBlueDeviceAddress = buttonView.getTag(R.id.tag_bluetooth_device_address).toString();
                            SharedPreferencesTool.addOrUpdate(PrintDialogActivity.this, "systemSet", "connectionDeviceCode", curSelectedBlueDeviceAddress);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mPopupWindow.dismiss();
                                    // 断开当前已连接的蓝牙设备
                                    for (Map.Entry<String, BluetoothSocket> m : MyApplication.mBluetoothSocketMap.entrySet()) {
                                        if (m.getValue().isConnected()) {
                                            try {
                                                m.getValue().close();
                                            } catch (IOException e) {
                                            }
                                        }
                                    }
                                    ConnectRunnable connectRunnable = new ConnectRunnable(curSelectedBlueDeviceAddress);
                                    new Thread(connectRunnable).start();
                                }
                            }, 100);
                        }
                    });
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (!searchGetBluetoothAddressList.contains(device.getAddress())) {
                            searchGetBluetoothAddressList.add(device.getAddress());
                        } else {
                            return;
                        }
                        String rbTextVal = "";
                        if (ComFun.strNull(device.getName())) {
                            rbTextVal = "名称：" + device.getName() + "\t\t";
                        }
                        rbTextVal += "地址：" + device.getAddress();
                        rbBlueDev.setText(rbTextVal);
                        rbBlueDev.setTag(R.id.tag_bluetooth_device_address, device.getAddress());
                        blueDevItemWrap.addView(blueDevItemView);
                    } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        if (!searchGetBluetoothAddressList.contains(device.getAddress())) {
                            searchGetBluetoothAddressList.add(device.getAddress());
                        } else {
                            return;
                        }
                        // 已配对的设备
                        String rbTextVal = "";
                        if (ComFun.strNull(device.getName())) {
                            rbTextVal = "名称：" + device.getName() + "\t\t";
                        }
                        rbTextVal += "地址：" + device.getAddress();
                        rbBlueDev.setText(rbTextVal);
                        rbBlueDev.setTag(R.id.tag_bluetooth_device_address, device.getAddress());
                        blueDevItemWrap.addView(blueDevItemView);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    // 搜索完成
                    if (mPopupWindow != null) {
                        ProgressBar pbSearchTip = (ProgressBar) mPopupWindow.getContentView().findViewById(R.id.pbSearchTip);
                        TextView tvSearchTip = (TextView) mPopupWindow.getContentView().findViewById(R.id.tvSearchTip);
                        Button btnReSearch = (Button) mPopupWindow.getContentView().findViewById(R.id.btnReSearch);
                        pbSearchTip.setVisibility(View.GONE);
                        tvSearchTip.setVisibility(View.GONE);
                        btnReSearch.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    // 连接新的打票机
    public void reConnectionPrintDevice(View view) {
        connectionPrintDev();
    }

    // 第一次设置打票机连接
    public void connectionPrintDevice(View view) {
        connectionPrintDev();
    }

    // 重试连接
    public void againConnectionPrintDevice(View view) {
        final String connectionDeviceCode = SharedPreferencesTool.getFromShared(PrintDialogActivity.this, "systemSet", "connectionDeviceCode", "");
        if (ComFun.strNull(connectionDeviceCode)) {
            tvPrintCode.setText("正在连接中...");

            Message mainPageUpdateMenuMsg = new Message();
            Bundle mainPageUpdateMenuData = new Bundle();
            mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
            mainPageUpdateMenuData.putString("status", "正在连接中...");
            mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
            MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConnectRunnable connectRunnable = new ConnectRunnable(connectionDeviceCode);
                    new Thread(connectRunnable).start();
                }
            }, 100);
        }
    }

    // 所有蓝牙设备列表并提示用户选择连接打票机
    private void connectionPrintDev() {
        if (MyApplication.mBluetoothAdapter.isEnabled()) {
            // 开始搜索蓝牙设备
            if (MyApplication.mBluetoothAdapter.isDiscovering()) {
                MyApplication.mBluetoothAdapter.cancelDiscovery();
            }
            MyApplication.mBluetoothAdapter.startDiscovery();
            // 显示蓝牙设备列表popWindow弹层
            final View blueDevListPopView = LayoutInflater.from(PrintDialogActivity.this).inflate(R.layout.blue_dev_list_view, null);
            RadioGroup blueDevItemWrap = (RadioGroup) blueDevListPopView.findViewById(R.id.blueDevItemWrap);
            blueDevItemWrap.removeAllViews();
            searchGetBluetoothAddressList.clear();
            Button btnReSearch = (Button) blueDevListPopView.findViewById(R.id.btnReSearch);
            btnReSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.GONE);
                    ProgressBar pbSearchTip = (ProgressBar) blueDevListPopView.findViewById(R.id.pbSearchTip);
                    TextView tvSearchTip = (TextView) blueDevListPopView.findViewById(R.id.tvSearchTip);
                    pbSearchTip.setVisibility(View.VISIBLE);
                    tvSearchTip.setVisibility(View.VISIBLE);
                    RadioGroup blueDevItemWrap = (RadioGroup) blueDevListPopView.findViewById(R.id.blueDevItemWrap);
                    blueDevItemWrap.removeAllViews();
                    searchGetBluetoothAddressList.clear();
                    // 开始搜索蓝牙设备
                    if (MyApplication.mBluetoothAdapter.isDiscovering()) {
                        MyApplication.mBluetoothAdapter.cancelDiscovery();
                    }
                    MyApplication.mBluetoothAdapter.startDiscovery();
                }
            });

            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                return;
            }
            mPopupWindow = new PopupWindow(blueDevListPopView, LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(PrintDialogActivity.this, 400));
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable());
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (MyApplication.mBluetoothAdapter.isDiscovering()) {
                        MyApplication.mBluetoothAdapter.cancelDiscovery();
                    }
                }
            });
            mPopupWindow.update();
            mPopupWindow.setAnimationStyle(R.style.popWindow_animation);
            mPopupWindow.showAtLocation(findViewById(R.id.printDialogLayout), Gravity.BOTTOM, 0, 0);
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, OPEN_BLUETOOTH);
        }
    }

    public static class ConnectRunnable implements Runnable {
        private boolean needShowLoading = true;
        private String SerialPortServiceClass_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // 蓝牙串口号
        private String LANAccessUsingPPPServiceClass_UUID = "00001102-0000-1000-8000-00805F9B34FB"; //

        private String deviceAddress = null;
        //新建BluetoothSocket类
        private final BluetoothSocket mmSocket;
        //新建BluetoothDevice对象
        private final BluetoothDevice mmDevice;

        public ConnectRunnable(String blueDeviceAddress) {
            deviceAddress = blueDeviceAddress;
            BluetoothSocket tmp = null;
            //赋值给设备
            mmDevice = MyApplication.mBluetoothAdapter.getRemoteDevice(deviceAddress);
            try {
                //根据UUID创建并返回一个BluetoothSocket
                tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(SerialPortServiceClass_UUID));
            } catch (IOException e) {
            }
            //赋值给BluetoothSocket
            mmSocket = tmp;
        }

        public ConnectRunnable(String blueDeviceAddress, boolean showLoadFlag) {
            needShowLoading = showLoadFlag;
            deviceAddress = blueDeviceAddress;
            BluetoothSocket tmp = null;
            //赋值给设备
            mmDevice = MyApplication.mBluetoothAdapter.getRemoteDevice(deviceAddress);
            try {
                //根据UUID创建并返回一个BluetoothSocket
                tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(SerialPortServiceClass_UUID));
            } catch (IOException e) {
            }
            //赋值给BluetoothSocket
            mmSocket = tmp;
        }

        @Override
        public void run() {
            Message mainPageUpdateMenuMsg = new Message();
            Bundle mainPageUpdateMenuData = new Bundle();
            mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
            mainPageUpdateMenuData.putString("status", "正在连接中...");
            mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
            MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);

            if (needShowLoading && mHandler != null) {
                // 发送连接蓝牙 Handler
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = PrintDialogActivity.MSG_SHOW_LOADING;
                data.putString("loadingTip", "设备正在连接中...");
                msg.setData(data);
                mHandler.sendMessage(msg);
            }
            // 取消发现设备
            MyApplication.mBluetoothAdapter.cancelDiscovery();
            try {
                // 连接到设备
                mmSocket.connect();
            } catch (IOException connectException) {
                mainPageUpdateMenuMsg = new Message();
                mainPageUpdateMenuData = new Bundle();
                mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                mainPageUpdateMenuData.putString("status", "连接失败");
                mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                if (mHandler != null) {
                    // 发送连接蓝牙失败 Handler
                    Message failMsg = new Message();
                    Bundle failData = new Bundle();
                    failMsg.what = PrintDialogActivity.MSG_HIDE_LOADING;
                    failData.putString("toastTip", "连接失败，确保选择正确的设备后，请重试");
                    failData.putBoolean("connectionFlag", false);
                    failData.putString("deviceAddress", deviceAddress);
                    failMsg.setData(failData);
                    mHandler.sendMessage(failMsg);
                }
                // 无法连接，关闭Socket
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            // 管理连接
            manageConnectedSocket(deviceAddress, mmSocket);
            mainPageUpdateMenuMsg = new Message();
            mainPageUpdateMenuData = new Bundle();
            mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
            mainPageUpdateMenuData.putString("status", "听单中...");
            mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
            MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
            if (mHandler != null) {
                // 发送连接蓝牙成功 Handler
                Message successMsg = new Message();
                Bundle successData = new Bundle();
                successMsg.what = PrintDialogActivity.MSG_HIDE_LOADING;
                successData.putString("toastTip", "设备连接成功");
                successData.putBoolean("connectionFlag", true);
                successData.putString("deviceAddress", deviceAddress);
                successMsg.setData(successData);
                mHandler.sendMessage(successMsg);
            }

            // 连接成功测试打印
            Intent printDataIntent = new Intent();
            printDataIntent.putExtra("printDeviceAddress", deviceAddress);
            printDataIntent.putExtra("title", "赋渔接单测试页");
            printDataIntent.setAction(FyBroadcastReceiver.ACTION_WRITE_TO_BLUE_DEVICE);
            MyApplication.getInstance().getApplicationContext().sendBroadcast(printDataIntent);
        }

        //取消连接
        public void cancelConnect() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

        private void manageConnectedSocket(String deviceAddress, BluetoothSocket mmSocket) {
            MyApplication.mBluetoothSocketMap.put(deviceAddress, mmSocket);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_BLUETOOTH) {
            if (resultCode == -1) {
                final String connectionDeviceCode = SharedPreferencesTool.getFromShared(PrintDialogActivity.this, "systemSet", "connectionDeviceCode", "");
                if (ComFun.strNull(connectionDeviceCode)) {
                    ComFun.showToast(PrintDialogActivity.this, "蓝牙已打开", Toast.LENGTH_SHORT);
                    tvPrintCode.setText("正在连接中...");

                    Message mainPageUpdateMenuMsg = new Message();
                    Bundle mainPageUpdateMenuData = new Bundle();
                    mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                    mainPageUpdateMenuData.putString("status", "正在连接中...");
                    mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                    MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ConnectRunnable connectRunnable = new ConnectRunnable(connectionDeviceCode);
                            new Thread(connectRunnable).start();
                        }
                    }, 300);
                } else {
                    ComFun.showToast(PrintDialogActivity.this, "蓝牙已打开，2秒后将自动开始搜索蓝牙设备列表", Toast.LENGTH_SHORT);
                    tvPrintCode.setText("未设置相关打票机");

                    Message mainPageUpdateMenuMsg = new Message();
                    Bundle mainPageUpdateMenuData = new Bundle();
                    mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                    mainPageUpdateMenuData.putString("status", "未设置");
                    mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                    MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connectionPrintDev();
                        }
                    }, 2400);
                }
            } else {
                ComFun.showToast(PrintDialogActivity.this, "蓝牙未打开，请前往系统设置打开蓝牙功能", Toast.LENGTH_SHORT);
            }
        }
    }

    class mHandler extends Handler {
        public mHandler() {
        }

        public mHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            switch (msg.what) {
                case MSG_SHOW_LOADING:
                    String loadingTip = b.getString("loadingTip");
                    ComFun.showLoading(PrintDialogActivity.this, loadingTip);
                    tvPrintCode.setText("正在连接中...");
                    btnAgainConnectPrint.setVisibility(View.GONE);
                    btnReConnectPrint.setVisibility(View.VISIBLE);
                    btnConnectPrint.setVisibility(View.GONE);
//                    tvPrintListening.setVisibility(View.GONE);
//                    esvOrderWaiter.setVisibility(View.GONE);
                    printingOrderDataLayout.setVisibility(View.GONE);
                    break;
                case MSG_HIDE_LOADING:
                    String toastTip = b.getString("toastTip");
                    String deviceAddress = b.getString("deviceAddress");
                    boolean connectionFlag = b.getBoolean("connectionFlag");
                    ComFun.showToast(PrintDialogActivity.this, toastTip, Toast.LENGTH_LONG);
                    ComFun.hideLoading();
                    if (connectionFlag) {
                        tvPrintCode.setText(deviceAddress);
                        btnAgainConnectPrint.setVisibility(View.GONE);
                        btnReConnectPrint.setVisibility(View.VISIBLE);
                        btnConnectPrint.setVisibility(View.GONE);
//                        tvPrintListening.setVisibility(View.VISIBLE);
//                        esvOrderWaiter.setVisibility(View.VISIBLE);
                        printingOrderDataLayout.setVisibility(View.VISIBLE);
                        printingOrderDataLayout.removeAllViews();
                    } else {
                        // 之前保存到蓝牙地址失效，连接失败
                        tvPrintCode.setText("设备连接失败\n\n\t可能的失败原因：\n\t\t1、正在连接的设备并未开启蓝牙。\n\t\t2、正在连接的设备不在附近。\n\t\t3、正在连接的设备并没有完成配对。\n\n\n您可以：\n选择 再次重试连接 或者 连接新的打票机");
                        btnAgainConnectPrint.setVisibility(View.VISIBLE);
                        btnReConnectPrint.setVisibility(View.VISIBLE);
                        btnConnectPrint.setVisibility(View.GONE);
//                        tvPrintListening.setVisibility(View.GONE);
//                        esvOrderWaiter.setVisibility(View.GONE);
                        printingOrderDataLayout.setVisibility(View.GONE);
                    }
                    break;
                case MSG_CONNECTION_DIS:
                    ComFun.showToast(PrintDialogActivity.this, "打票机连接已断开", Toast.LENGTH_SHORT);
                    tvPrintCode.setText("连接已断开\n\n\t可能的断开原因：\n\t\t1、正在连接的设备关闭了蓝牙。\n\t\t2、正在连接的设备不在附近。\n\t\t3、正在连接的设备并没有完成配对。\n\n\n您可以：\n选择 再次重试连接 或者 连接新的打票机");
                    btnAgainConnectPrint.setVisibility(View.VISIBLE);
                    btnReConnectPrint.setVisibility(View.VISIBLE);
                    btnConnectPrint.setVisibility(View.GONE);
//                    tvPrintListening.setVisibility(View.GONE);
//                    esvOrderWaiter.setVisibility(View.GONE);
                    printingOrderDataLayout.setVisibility(View.GONE);
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
