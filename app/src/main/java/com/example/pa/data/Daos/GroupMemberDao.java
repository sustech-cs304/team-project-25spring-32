package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

public class GroupMemberDao {
    public static final String TABLE_NAME = "GroupMembers";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_JOIN_TIME = "join_time";
    public static final String COLUMN_ROLE = "role"; // 可以是 "admin" 或 "member"

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GROUP_ID + " INTEGER NOT NULL, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_JOIN_TIME + " TEXT DEFAULT (datetime('now')), " +
                    COLUMN_ROLE + " TEXT DEFAULT 'member', " +
                    "FOREIGN KEY (" + COLUMN_GROUP_ID + ") REFERENCES " + GroupDao.TABLE_NAME + "(" + GroupDao.COLUMN_ID + "), " +
                    "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + UserDao.TABLE_NAME + "(" + UserDao.COLUMN_ID + "), " +
                    "UNIQUE(" + COLUMN_GROUP_ID + ", " + COLUMN_USER_ID + "))";

    private final SQLiteDatabase db;

    public GroupMemberDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    /**
     * 添加群组成员
     */
    public long addMember(int groupId, int userId, String role) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_ID, groupId);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_ROLE, role);

        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("GroupMemberDao", "添加群组成员失败: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 移除群组成员
     */
    public boolean removeMember(int groupId, int userId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_GROUP_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(groupId), String.valueOf(userId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("GroupMemberDao", "移除群组成员失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新成员角色
     */
    public boolean updateMemberRole(int groupId, int userId, String role) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROLE, role);

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_GROUP_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(groupId), String.valueOf(userId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("GroupMemberDao", "更新成员角色失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取群组的所有成员
     */
    public Cursor getGroupMembers(int groupId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_GROUP_ID + " = ?",
                new String[]{String.valueOf(groupId)},
                null, null, COLUMN_JOIN_TIME + " ASC");
    }

    /**
     * 获取用户加入的所有群组
     */
    public Cursor getUserGroups(int userId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_JOIN_TIME + " DESC");
    }

    /**
     * 检查用户是否是群组成员
     */
    public boolean isMember(int groupId, int userId) {
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_GROUP_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(groupId), String.valueOf(userId)},
                null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    /**
     * 获取用户在群组中的角色
     */
    public String getMemberRole(int groupId, int userId) {
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ROLE},
                COLUMN_GROUP_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(groupId), String.valueOf(userId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            }
            return null;
        }
    }
} 