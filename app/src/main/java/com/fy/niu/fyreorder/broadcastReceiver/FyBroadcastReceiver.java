package com.fy.niu.fyreorder.broadcastReceiver;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.fy.niu.fyreorder.MainActivity;
import com.fy.niu.fyreorder.PrintDialogActivity;
import com.fy.niu.fyreorder.util.ComFun;
import com.fy.niu.fyreorder.util.MyApplication;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class FyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "FyBReceiver";
    public static final String ACTION_WRITE_TO_BLUE_DEVICE = "fyOrder:action_write_to_blue_device";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String keyValueStr = "";
        for (String key : bundle.keySet()) {
            keyValueStr += key + ": " + bundle.get(key) + " | ";
        }
        Log.d(TAG + " 收到广播", intent.getAction() + " -> " + keyValueStr);
        switch (intent.getAction()) {
            case JPushInterface.ACTION_MESSAGE_RECEIVED:
                String orderPrintData = bundle.getString(JPushInterface.EXTRA_EXTRA);

                Intent messagePrintDataIntent = new Intent();
                messagePrintDataIntent.putExtra("title", "赋渔接单测试页");
                messagePrintDataIntent.setAction(FyBroadcastReceiver.ACTION_WRITE_TO_BLUE_DEVICE);
                context.sendBroadcast(messagePrintDataIntent);
                break;
            case JPushInterface.ACTION_NOTIFICATION_RECEIVED:
                Intent notificationPrintDataIntent = new Intent();
                notificationPrintDataIntent.putExtra("title", "赋渔接单测试页");
                notificationPrintDataIntent.setAction(FyBroadcastReceiver.ACTION_WRITE_TO_BLUE_DEVICE);
                context.sendBroadcast(notificationPrintDataIntent);
                break;
            case ACTION_WRITE_TO_BLUE_DEVICE:
                String printDeviceAddress = SharedPreferencesTool.getFromShared(context, "systemSet", "connectionDeviceCode", "");
                String print_title = bundle.getString("title");
                List<byte[]> dataByteList = anayWriteDataList(print_title);
                BluetoothSocket printDeviceBluetoothSocket = MyApplication.mBluetoothSocketMap.get(printDeviceAddress);
                if (ComFun.strNull(printDeviceBluetoothSocket) && printDeviceBluetoothSocket.isConnected()) {
                    try {
                        for (byte[] dataByte : dataByteList) {
                            printDeviceBluetoothSocket.getOutputStream().write(dataByte);
                        }
                        printDeviceBluetoothSocket.getOutputStream().flush();
                    } catch (IOException e) {
                    }
                } else {
                    if (PrintDialogActivity.mHandler != null) {
                        Message printMsg = new Message();
                        printMsg.what = PrintDialogActivity.MSG_CONNECTION_DIS;
                        PrintDialogActivity.mHandler.sendMessage(printMsg);
                    }

                    if (MainActivity.mHandler != null) {
                        Message mainPageUpdateMenuMsg = new Message();
                        Bundle mainPageUpdateMenuData = new Bundle();
                        mainPageUpdateMenuMsg.what = MainActivity.MSG_UPDATE_PRINT_MENU_STATE;
                        mainPageUpdateMenuData.putString("status", "连接断开");
                        mainPageUpdateMenuMsg.setData(mainPageUpdateMenuData);
                        MainActivity.mHandler.sendMessage(mainPageUpdateMenuMsg);
                    }
                }
                break;
        }
    }

    // 解析要打印的数据为字节数组集合
    private List<byte[]> anayWriteDataList(String title) {
        List<byte[]> dataList = new ArrayList<>();
        try {
//            // 选择倍宽
//            dataList.add(new byte[]{0x1C, 0x21, 0x04});
//            // 选择倍高
//            dataList.add(new byte[]{0x1C, 0x21, 0x08});
            // 选择中间对齐
            dataList.add(new byte[]{0x1B, 0x61, 0x01});
            dataList.add((title + "\n").getBytes("GBK"));
            // 取消中间对齐
            dataList.add(new byte[]{0x1B, 0x61, 0x00});
//            // 取消倍宽
//            dataList.add(new byte[]{0x1C, 0x21, 0x00});
//            // 取消倍高
//            dataList.add(new byte[]{0x1C, 0x21, 0x00});
//            dataList.add(new byte[]{0x0a, 0x0a, 0x1d, 0x56, 0x01});
            dataList.add(new byte[]{0x0a, 0x0a, 0x1d, 0x56, 0x01});
            dataList.add(new byte[]{0x0a, 0x0a, 0x1d, 0x56, 0x01});
        } catch (IOException e) {
        }
        return dataList;
    }
}
