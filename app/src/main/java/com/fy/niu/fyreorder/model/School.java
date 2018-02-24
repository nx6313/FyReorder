package com.fy.niu.fyreorder.model;

import java.io.Serializable;

/**
 * Created by nx6313 on 2018/2/24.
 */

public class School implements Serializable {
    private int model;
    private int grade;
    private String orgroleId;
    private String pid;
    private String orgName;

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getOrgroleId() {
        return orgroleId;
    }

    public void setOrgroleId(String orgroleId) {
        this.orgroleId = orgroleId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}
