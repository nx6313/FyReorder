package com.fy.niu.fyreorder.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class DBUtil {

    public static void save(DBOpenHelper dbOpenHelper, String tableName, ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.insert(tableName, null, values);
    }

    public static void delete(DBOpenHelper dbOpenHelper, String tableName, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete(tableName, whereClause, whereArgs);
    }

    public static void deleteAll(DBOpenHelper dbOpenHelper, String tableName) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete(tableName, "", null);
    }

    public static void update(DBOpenHelper dbOpenHelper, String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.update(tableName, values, whereClause, whereArgs);
    }

    public static JSONObject find(DBOpenHelper dbOpenHelper, String tableName, String[] columns, String selection,
                           String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        try {
            if (cursor.moveToFirst()) {
                JSONObject dbFindResult = new JSONObject();
                String[] searchColumns = cursor.getColumnNames();
                if(ComFun.strNull(searchColumns)){
                    for(String column : searchColumns){
                        dbFindResult.put(column, cursor.getString(cursor.getColumnIndex(column)));
                    }
                }
                return dbFindResult;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return null;
    }

    public static int getCount(DBOpenHelper dbOpenHelper, String tableName) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from " + tableName, null);
        try {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }

}
