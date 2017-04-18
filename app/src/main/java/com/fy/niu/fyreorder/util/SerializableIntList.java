package com.fy.niu.fyreorder.util;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 18230 on 2016/11/26.
 */

public class SerializableIntList implements Serializable {
    private List<Integer> list = null;

    public List<Integer> getList(){
        return list;
    }

    public void setList(List<Integer> list){
        this.list = list;
    }
}
