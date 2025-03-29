package com.example.pa.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.Date;

public class UserDao {
    // 表结构常量
    public static final String TABLE_NAME = "User";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_CREATED_TIME = "created_time";
    public static final String COLUMN_AVATAR_PATH = "avatar_path";
    public static final String COLUMN_AVATAR_URL = "avatar_url";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL UNIQUE CHECK(length(" + COLUMN_USERNAME + ") <= 20), " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                    COLUMN_CREATED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    COLUMN_AVATAR_PATH + " TEXT, " +
                    COLUMN_AVATAR_URL + " TEXT)";

    private final SQLiteDatabase db;

    public UserDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    /**
     * 添加用户（带参数校验）
     * @return 新用户的ID，失败返回-1
     */
    public long addUser(String username, String email, String passwordHash) {
        // 参数校验
        if (username == null || username.isEmpty() || username.length() > 20) {
            Log.e("UserDao", "用户名不合法");
            return -1;
        }

        if (!isValidEmail(email)) {
            Log.e("UserDao", "邮箱格式不正确");
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD_HASH, passwordHash);

        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("UserDao", "添加用户失败: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 更新用户头像信息
     */
    public boolean updateUserAvatar(int userId, String localPath, String remoteUrl) {
        ContentValues values = new ContentValues();
        if (localPath != null) values.put(COLUMN_AVATAR_PATH, localPath);
        if (remoteUrl != null) values.put(COLUMN_AVATAR_URL, remoteUrl);

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("UserDao", "更新头像失败", e);
            return false;
        }
    }

    /**
     * 根据用户名获取用户信息
     */
    public User getUserByUsername(String username) {
        try (Cursor cursor = db.query(TABLE_NAME,
                null,
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null)) {

            if (cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } catch (Exception e) {
            Log.e("UserDao", "查询用户失败", e);
            return null;
        }
    }

    /**
     * 验证用户凭据
     */
    public boolean validateUser(String username, String passwordHash) {
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD_HASH + " = ?",
                new String[]{username, passwordHash},
                null, null, null)) {

            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("UserDao", "验证用户失败", e);
            return false;
        }
    }

    // 清空表（测试用）
    public void clearTable() {
        db.beginTransaction();
        try {
            db.delete(TABLE_NAME, null, null);
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_NAME + "'");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // 辅助方法：Cursor转User对象
    private User cursorToUser(Cursor cursor) {
        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
        @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
        @SuppressLint("Range") String createdTime = cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_TIME));
        @SuppressLint("Range") String avatarPath = cursor.getString(cursor.getColumnIndex(COLUMN_AVATAR_PATH));
        @SuppressLint("Range") String avatarUrl = cursor.getString(cursor.getColumnIndex(COLUMN_AVATAR_URL));

        return new User(id, username, email, createdTime, avatarPath, avatarUrl);
    }

    // 邮箱格式校验（应用层实现）
    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // 用户数据模型（内部类）
    public static class User {
        public final int id;
        public final String username;
        public final String email;
        public final String createdTime;
        public final String avatarPath;
        public final String avatarUrl;

        public User(int id, String username, String email,
                    String createdTime, String avatarPath, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.createdTime = createdTime;
            this.avatarPath = avatarPath;
            this.avatarUrl = avatarUrl;
        }
    }
}