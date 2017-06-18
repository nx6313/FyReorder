package com.fy.niu.fyreorder.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 18230 on 2017/6/19.
 */

public class Order implements Serializable {
    private String userName;
    private String userPhone;
    private String school;
    private String address;
    private String payWay;
    private String orderTime;
    private List<BuyContent> orderDetail;
    private String remark; // 给商家的留言
    private int state; // 已接1、未接2

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public List<BuyContent> getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(List<BuyContent> orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public static class BuyContent implements Serializable {
        private String imgPath;
        private String name;
        private String money;
        private int buyCount;

        public String getImgPath() {
            return imgPath;
        }

        public void setImgPath(String imgPath) {
            this.imgPath = imgPath;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public int getBuyCount() {
            return buyCount;
        }

        public void setBuyCount(int buyCount) {
            this.buyCount = buyCount;
        }
    }
}
