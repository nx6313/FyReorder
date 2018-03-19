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
import com.fy.niu.fyreorder.util.PrintUtil;
import com.fy.niu.fyreorder.util.SharedPreferencesTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
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
                if (ComFun.strNull(orderPrintData)) {
                    if (ComFun.getJSONType(orderPrintData).equals(ComFun.JSON_TYPE.JSON_TYPE_OBJECT)) {
                        try {
                            JSONObject result = new JSONObject(orderPrintData);
                            if (result.has("printData")) {
                                JSONObject orderData = new JSONObject(result.getString("printData"));
                                String shopName = orderData.getString("shopName");
                                String orderNumber = orderData.getString("orderNumber");
                                String orderDate = orderData.getString("orderDate");
                                String payType = orderData.getString("payType");
                                String shopTel = orderData.getString("shopTel");
                                String children = orderData.getString("children");
                                String orderPrice = orderData.getString("orderPrice");
                                String name = orderData.getString("name");
                                String phone = orderData.getString("phone");
                                String detail = orderData.getString("detail");

                                PrintOrderData printOrderData = new PrintOrderData();
                                printOrderData.setShopName(shopName);
                                printOrderData.setOrderNumber(orderNumber);
                                printOrderData.setOrderDate(orderDate);
                                printOrderData.setPayType(payType);
                                printOrderData.setShopTel(shopTel);
                                JSONArray childrenArr = new JSONArray(children);
                                List<PrintOrderData.PrintOrderChildData> orderChild = new ArrayList<>();
                                for (int c = 0; c < childrenArr.length(); c++) {
                                    JSONObject cData = new JSONObject(childrenArr.getString(c));

                                    PrintOrderData.PrintOrderChildData cd = new PrintOrderData.PrintOrderChildData();
                                    cd.setProName(cData.getString("proName"));
                                    cd.setProNum(cData.getString("proNum"));
                                    cd.setProPrice(cData.getString("proPrice"));
                                    orderChild.add(cd);
                                }
                                printOrderData.setChildrens(orderChild);
                                printOrderData.setOrderPrice(orderPrice);
                                printOrderData.setName(name);
                                printOrderData.setPhone(phone);
                                printOrderData.setDetail(detail);

                                Intent messagePrintDataIntent = new Intent();
                                messagePrintDataIntent.putExtra("title", "赋渔专送");
                                messagePrintDataIntent.putExtra("printOrderData", printOrderData);
                                messagePrintDataIntent.setAction(FyBroadcastReceiver.ACTION_WRITE_TO_BLUE_DEVICE);
                                context.sendBroadcast(messagePrintDataIntent);
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
                break;
            case JPushInterface.ACTION_NOTIFICATION_RECEIVED:
                break;
            case ACTION_WRITE_TO_BLUE_DEVICE:
                String printDeviceAddress = SharedPreferencesTool.getFromShared(context, "systemSet", "connectionDeviceCode", "");
                String print_title = bundle.getString("title");
                PrintOrderData printOrderData = (PrintOrderData) bundle.getSerializable("printOrderData");
                List<byte[]> dataByteList = anayWriteDataList(print_title, printOrderData);
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
    private List<byte[]> anayWriteDataList(String title, PrintOrderData printOrderData) {
        List<byte[]> dataList = new ArrayList<>();
        try {
            if (ComFun.strNull(printOrderData)) {
                dataList.add(PrintUtil.toCenter);
                dataList.add((title + "\n").getBytes("GBK"));
                dataList.add(PrintUtil.cut2);
                dataList.add((printOrderData.getShopName() + "\n").getBytes("GBK"));
                dataList.add(PrintUtil.toLeft);
                dataList.add(("订单编号：" + printOrderData.getOrderNumber() + "\n").getBytes("GBK"));
                dataList.add(("订单时间：" + printOrderData.getOrderDate() + "\n").getBytes("GBK"));
                dataList.add(("支付方式：" + printOrderData.getPayType() + "\n").getBytes("GBK"));
                dataList.add(("商家联系方式：" + printOrderData.getShopTel() + "\n").getBytes("GBK"));
                dataList.add(PrintUtil.toCenter);
                dataList.add(("------------------------" + "\n").getBytes("GBK"));
                dataList.add(PrintUtil.cut2);
                dataList.add(PrintUtil.toLeft);
                dataList.add(("名称").getBytes("GBK"));
                dataList.add(new byte[]{0x09});
                dataList.add(new byte[]{0x09});
                dataList.add(("数量").getBytes("GBK"));
                dataList.add(new byte[]{0x09});
                dataList.add(("价格" + "\n").getBytes("GBK"));
                for (PrintOrderData.PrintOrderChildData poc : printOrderData.getChildrens()) {
                    dataList.add((ComFun.autoWordLine(poc.getProName(), 6)).getBytes("GBK"));
                    dataList.add(new byte[]{0x09});
                    dataList.add(new byte[]{0x09});
                    dataList.add(("x" + poc.getProNum()).getBytes("GBK"));
                    dataList.add(new byte[]{0x09});
                    dataList.add((poc.getProPrice() + "\n").getBytes("GBK"));
                }
                dataList.add(PrintUtil.toCenter);
                dataList.add(("------------------------" + "\n").getBytes("GBK"));
                dataList.add(PrintUtil.toRight);
                dataList.add(("总价：" + printOrderData.getOrderPrice() + "\n").getBytes("GBK"));
                dataList.add(PrintUtil.cut2);
                dataList.add(PrintUtil.toCenter);
                dataList.add(("------------------------" + "\n").getBytes("GBK"));
                dataList.add(PrintUtil.toLeft);
                dataList.add(("收货人：" + printOrderData.getName() + "\n").getBytes("GBK"));
                dataList.add(("手机号：" + printOrderData.getPhone() + "\n").getBytes("GBK"));
                dataList.add(("地点：" + printOrderData.getDetail() + "\n").getBytes("GBK"));
            } else {
                dataList.add(PrintUtil.toCenter);
                dataList.add((title + "\n").getBytes("GBK"));
            }

            dataList.add(PrintUtil.cut);
        } catch (IOException e) {
        }
        return dataList;
    }

    static class PrintOrderData implements Serializable {
        private String shopName;
        private String orderNumber;
        private String orderDate;
        private String payType;
        private String shopTel;
        private List<PrintOrderChildData> childrens = new ArrayList<>();
        private String orderPrice;
        private String name;
        private String phone;
        private String detail;

        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }

        public String getPayType() {
            return payType;
        }

        public void setPayType(String payType) {
            this.payType = payType;
        }

        public String getShopTel() {
            return shopTel;
        }

        public void setShopTel(String shopTel) {
            this.shopTel = shopTel;
        }

        public List<PrintOrderChildData> getChildrens() {
            return childrens;
        }

        public void setChildrens(List<PrintOrderChildData> childrens) {
            this.childrens = childrens;
        }

        public String getOrderPrice() {
            return orderPrice;
        }

        public void setOrderPrice(String orderPrice) {
            this.orderPrice = orderPrice;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public static class PrintOrderChildData implements Serializable {
            private String proName;
            private String proNum;
            private String proPrice;

            public String getProName() {
                return proName;
            }

            public void setProName(String proName) {
                this.proName = proName;
            }

            public String getProNum() {
                return proNum;
            }

            public void setProNum(String proNum) {
                this.proNum = proNum;
            }

            public String getProPrice() {
                return proPrice;
            }

            public void setProPrice(String proPrice) {
                this.proPrice = proPrice;
            }
        }
    }

}
