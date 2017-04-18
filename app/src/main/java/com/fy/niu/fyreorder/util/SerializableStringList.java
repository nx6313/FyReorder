package com.fy.niu.fyreorder.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/26.
 */

public class SerializableStringList implements Serializable {
    private List<String> list = null;

    public List<String> getList(){
        return list;
    }

    public void setList(List<String> list){
        this.list = list;
    }
}
