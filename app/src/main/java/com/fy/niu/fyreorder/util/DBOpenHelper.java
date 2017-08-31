package com.fy.niu.fyreorder.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 18230 on 2017/8/20.
 */

public class DBOpenHelper extends SQLiteOpenHelper {

    public DBOpenHelper(Context context) {
        super(context, "fyGo.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notification (id integer primary key autoincrement,msg_id varchar(64),title varchar(128),activity varchar(256),notificationActionType varchar(512),content text,update_time varchar(16))");
        db.execSQL("CREATE TABLE userInfo (userId varchar(64),floor varchar(64),floorName varchar(200),zhi text,wei text,grade varchar(20),userName varchar(200),roleName varchar(200),type varchar(20),orgName varchar(20),telephone varchar(30))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
