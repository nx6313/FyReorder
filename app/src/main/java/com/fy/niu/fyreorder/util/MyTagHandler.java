package com.fy.niu.fyreorder.util;

import android.content.Context;
import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

/**
 * Created by 18230 on 2017/9/7.
 */

public class MyTagHandler implements Html.TagHandler {
    private final Context mConrext;

    public MyTagHandler(Context context){
        mConrext = context;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

    }
}