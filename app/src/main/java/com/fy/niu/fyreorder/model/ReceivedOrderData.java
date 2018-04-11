package com.fy.niu.fyreorder.model;

import java.io.Serializable;

public class ReceivedOrderData implements Serializable {
    private int weiPayCount;
    private String weiPayMoney;
    private int huoDaoCount;
    private String huoDaoMoney;
    private int allCount;
    private String allMoney;

    public int getWeiPayCount() {
        return weiPayCount;
    }

    public void setWeiPayCount(int weiPayCount) {
        this.weiPayCount = weiPayCount;
    }

    public String getWeiPayMoney() {
        return weiPayMoney;
    }

    public void setWeiPayMoney(String weiPayMoney) {
        this.weiPayMoney = weiPayMoney;
    }

    public int getHuoDaoCount() {
        return huoDaoCount;
    }

    public void setHuoDaoCount(int huoDaoCount) {
        this.huoDaoCount = huoDaoCount;
    }

    public String getHuoDaoMoney() {
        return huoDaoMoney;
    }

    public void setHuoDaoMoney(String huoDaoMoney) {
        this.huoDaoMoney = huoDaoMoney;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public String getAllMoney() {
        return allMoney;
    }

    public void setAllMoney(String allMoney) {
        this.allMoney = allMoney;
    }
}
