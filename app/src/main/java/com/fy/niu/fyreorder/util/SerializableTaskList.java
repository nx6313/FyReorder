package com.fy.niu.fyreorder.util;

import com.fy.niu.fyreorder.model.Task;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 18230 on 2016/11/26.
 */

public class SerializableTaskList implements Serializable {
    private List<Task> list = null;

    public List<Task> getList(){
        return list;
    }

    public void setList(List<Task> list){
        this.list = list;
    }
}
