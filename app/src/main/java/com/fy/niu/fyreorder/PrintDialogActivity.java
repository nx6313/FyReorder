package com.fy.niu.fyreorder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fy.niu.fyreorder.customView.ElasticScrollView;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import java.util.UUID;

public class PrintDialogActivity extends Activity {
    private CheckBox cbPrint;
    private TextView tvPrintTip;
    private Button btnReConnectPrint;
    private TextView tvPrintCode;
    private Button btnConnectPrint;
    private TextView tvPrintListening;
    private ElasticScrollView esvOrderWaiter;
    private LinearLayout printingOrderDataLayout;

    private String BluetoothSocketUUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothSocket mBluetoothSocket = null;
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        cbPrint = (CheckBox) findViewById(R.id.cbPrint);
        tvPrintTip = (TextView) findViewById(R.id.tvPrintTip);
        btnReConnectPrint = (Button) findViewById(R.id.btnReConnectPrint);
        tvPrintCode = (TextView) findViewById(R.id.tvPrintCode);
        btnConnectPrint = (Button) findViewById(R.id.btnConnectPrint);
        tvPrintListening = (TextView) findViewById(R.id.tvPrintListening);
        esvOrderWaiter = (ElasticScrollView) findViewById(R.id.esvOrderWaiter);
        printingOrderDataLayout = (LinearLayout) findViewById(R.id.printingOrderDataLayout);
        cbPrint.setChecked(false);
        tvPrintTip.setVisibility(View.GONE);
        btnReConnectPrint.setVisibility(View.GONE);
        tvPrintCode.setVisibility(View.GONE);
        btnConnectPrint.setVisibility(View.GONE);
        tvPrintListening.setVisibility(View.GONE);
        esvOrderWaiter.setVisibility(View.GONE);
        printingOrderDataLayout.setVisibility(View.GONE);

        Boolean printIsOpenInSave = SharedPreferencesTool.getBooleanFromShared(PrintDialogActivity.this, "systemSet", "printIsOpen");
        if (printIsOpenInSave) {
            cbPrint.setChecked(true);
            if (mBluetoothAdapter == null) {
                mBluetoothAdapter.enable();
            }
            // 注册蓝牙监听
            PrintDialogActivity.this.registerReceiver(mReceiver, makeFilter());
            tvPrintTip.setVisibility(View.VISIBLE);
            tvPrintCode.setVisibility(View.VISIBLE);
            String connectionDeviceCode = SharedPreferencesTool.getFromShared(PrintDialogActivity.this, "systemSet", "connectionDeviceCode", "");
            if (ComFun.strNull(connectionDeviceCode)) {
                try {
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(connectionDeviceCode);
                    mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(BluetoothSocketUUID));
                    mBluetoothSocket.connect();
                } catch (Exception e) {
                }
                tvPrintCode.setText(connectionDeviceCode);
                btnReConnectPrint.setVisibility(View.VISIBLE);
                tvPrintListening.setVisibility(View.VISIBLE);
                esvOrderWaiter.setVisibility(View.VISIBLE);
                printingOrderDataLayout.setVisibility(View.VISIBLE);
            } else {
                tvPrintCode.setText("无打票机连接");
                btnConnectPrint.setVisibility(View.VISIBLE);
            }
        }
        cbPrint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesTool.addOrUpdate(PrintDialogActivity.this, "systemSet", "printIsOpen", isChecked);
                if (isChecked) {
                    if (mBluetoothAdapter == null) {
                        mBluetoothAdapter.enable();
                    }
                    // 注册蓝牙监听
                    PrintDialogActivity.this.registerReceiver(mReceiver, makeFilter());
                    tvPrintTip.setVisibility(View.VISIBLE);
                    tvPrintCode.setVisibility(View.VISIBLE);
                    String connectionDeviceCode = SharedPreferencesTool.getFromShared(PrintDialogActivity.this, "systemSet", "connectionDeviceCode", "");
                    if (ComFun.strNull(connectionDeviceCode)) {
                        try {
                            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(connectionDeviceCode);
                            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(BluetoothSocketUUID));
                            mBluetoothSocket.connect();
                        } catch (Exception e) {
                        }
                        tvPrintCode.setText(connectionDeviceCode);
                        btnReConnectPrint.setVisibility(View.VISIBLE);
                        tvPrintListening.setVisibility(View.VISIBLE);
                        esvOrderWaiter.setVisibility(View.VISIBLE);
                        printingOrderDataLayout.setVisibility(View.VISIBLE);
                    } else {
                        tvPrintCode.setText("无打票机连接");
                        btnConnectPrint.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvPrintTip.setVisibility(View.GONE);
                    btnReConnectPrint.setVisibility(View.GONE);
                    tvPrintCode.setVisibility(View.GONE);
                    btnConnectPrint.setVisibility(View.GONE);
                    tvPrintListening.setVisibility(View.GONE);
                    esvOrderWaiter.setVisibility(View.GONE);
                    printingOrderDataLayout.setVisibility(View.GONE);
                    if (mBluetoothAdapter != null) {
                        // 解除蓝牙监听
                        PrintDialogActivity.this.unregisterReceiver(mReceiver);
                    }
                }
            }
        });
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
            }
        }
    };

    // 连接新的打票机
    public void reConnectionPrintDevice(View view) {
    }

    // 第一次设置打票机连接
    public void connectionPrintDevice(View view) {
    }
}
