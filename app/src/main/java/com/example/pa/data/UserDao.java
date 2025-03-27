package com.example.pa.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserDao {
    // 表结构常量
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT)";

    private SQLiteDatabase db;

    public UserDao(Context context) {
        // 使用单例DatabaseHelper获取数据库实例
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    // 添加用户
    public long addUser(String name, String email) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);

        try {
            return db.insert(TABLE_USERS, null, values);
        } catch (SQLException e) {
            Log.e("UserDao", "添加用户失败", e);
            return -1;
        }
    }

    // 获取所有用户
    public Cursor getAllUsers() {
        String[] columns = {COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL};
        return db.query(TABLE_USERS, columns, null, null, null, null, null);
    }

    // 更新用户
    public int updateUser(long id, String name, String email) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);

        try {
            return db.update(TABLE_USERS, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e("UserDao", "更新用户失败", e);
            return 0;
        }
    }

    // 删除用户
    public int deleteUser(long id) {
        try {
            return db.delete(TABLE_USERS,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e("UserDao", "删除用户失败", e);
            return 0;
        }
    }

    // 清空表并重置自增ID
    public void clearTable() {
        db.beginTransaction();
        try {
            // 清空数据
            db.delete(TABLE_USERS, null, null);
            // 重置自增ID
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_USERS + "'");
            db.setTransactionSuccessful();
            Log.d("UserDao", "用户表已清空");
        } catch (SQLException e) {
            Log.e("UserDao", "清空表失败", e);
        } finally {
            db.endTransaction();
        }
    }
}