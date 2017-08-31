package com.fy.niu.fyreorder.model;

import java.io.Serializable;

public class ReceivedOrderData implements Serializable {
    private int dayNum;
    private String dayCharge;
    private int weekNum;
    private String weekCharge;
    private int monthNum;
    private String monthCharge;
    private int sumNum;
    private String sumCharge;

    public int getDayNum() {
        return dayNum;
    }

    public void setDayNum(int dayNum) {
        this.dayNum = dayNum;
    }

    public String getDayCharge() {
        return dayCharge;
    }

    public void setDayCharge(String dayCharge) {
        this.dayCharge = dayCharge;
    }

    public int getWeekNum() {
        return weekNum;
    }

    public void setWeekNum(int weekNum) {
        this.weekNum = weekNum;
    }

    public String getWeekCharge() {
        return weekCharge;
    }

    public void setWeekCharge(String weekCharge) {
        this.weekCharge = weekCharge;
    }

    public int getMonthNum() {
        return monthNum;
    }

    public void setMonthNum(int monthNum) {
        this.monthNum = monthNum;
    }

    public String getMonthCharge() {
        return monthCharge;
    }

    public void setMonthCharge(String monthCharge) {
        this.monthCharge = monthCharge;
    }

    public int getSumNum() {
        return sumNum;
    }

    public void setSumNum(int sumNum) {
        this.sumNum = sumNum;
    }

    public String getSumCharge() {
        return sumCharge;
    }

    public void setSumCharge(String sumCharge) {
        this.sumCharge = sumCharge;
    }
}
