package com.fy.niu.fyreorder.util;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 18230 on 2016/11/26.
 */

public class SerializableObjectList implements Serializable {
    private List<Object> list = null;

    public List<Object> getList(){
        return list;
    }

    public void setList(List<Object> list){
        this.list = list;
    }
}
