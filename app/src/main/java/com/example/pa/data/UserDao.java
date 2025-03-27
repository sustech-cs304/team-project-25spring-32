package com.example.pa.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class UserDao {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // 添加用户
    public long addUser(String name, String email) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_EMAIL, email);

        return database.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    // 获取所有用户
    public Cursor getAllUsers() {
        String[] columns = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_EMAIL
        };

        return database.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                null, null, null, null, null
        );
    }

    // 更新用户
    public int updateUser(long id, String name, String email) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_EMAIL, email);

        return database.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // 删除用户
    public int deleteUser(long id) {
        return database.delete(
                DatabaseHelper.TABLE_USERS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }
}