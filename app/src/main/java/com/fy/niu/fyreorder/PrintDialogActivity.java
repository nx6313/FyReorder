package com.fy.niu.fyreorder;

import android.app.Activity;
import android.os.Bundle;

import com.fy.niu.fyreorder.util.ComFun;

public class PrintDialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_dialog);

        ComFun.addToActiveActivityList(PrintDialogActivity.this);
    }
}
