package com.fy.niu.fyreorder.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class FyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "XG";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

    }
}
