package com.fy.niu.fyreorder.util;

import com.fy.niu.fyreorder.model.Order;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 18230 on 2016/11/26.
 */

public class SerializableOrderList implements Serializable {
    private List<Order> list = null;

    public List<Order> getList(){
        return list;
    }

    public void setList(List<Order> list){
        this.list = list;
    }
}
