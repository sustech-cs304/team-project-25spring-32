package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;
import com.example.pa.data.Daos.GroupDao;
import com.example.pa.data.Daos.PostDao;

import java.util.ArrayList;
import java.util.List;

public class GroupPostDao {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: I directly copy the code from its response and modify the logic of some method, add some
     * methods we need but it did not generate, and add some logs.
     */
    public static final String TABLE_NAME = "GroupPost";
    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_ADDED_TIME = "added_time";
    public static final String COLUMN_VISIBILITY = "visibility";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_GROUP_ID + " INTEGER, " +
                    COLUMN_POST_ID + " INTEGER, " +
                    COLUMN_ADDED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    COLUMN_VISIBILITY + " TEXT DEFAULT 'visible' CHECK(" + COLUMN_VISIBILITY + " IN ('visible', 'hidden')), " +
                    "PRIMARY KEY (" + COLUMN_GROUP_ID + ", " + COLUMN_POST_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_GROUP_ID + ") REFERENCES " +
                    GroupDao.TABLE_NAME + "(" + GroupDao.COLUMN_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COLUMN_POST_ID + ") REFERENCES " +
                    PostDao.TABLE_NAME + "(" + PostDao.COLUMN_ID + ") ON DELETE CASCADE)";

    private final SQLiteDatabase db;

    public GroupPostDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
        enableForeignKeys();
    }

    private void enableForeignKeys() {
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    /**
     * 将帖子添加到群组
     */
    public boolean addPostToGroup(int groupId, int postId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_ID, groupId);
        values.put(COLUMN_POST_ID, postId);

        try {
            Log.d("GroupPostDao", "添加帖子 " + postId + " 到群组: " + groupId);
            long result = db.insert(TABLE_NAME, null, values);
            return result != -1;
        } catch (SQLException e) {
            Log.e("GroupPostDao", "添加帖子到群组失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从群组中移除帖子
     */
    public boolean removePostFromGroup(int groupId, int postId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_GROUP_ID + " = ? AND " + COLUMN_POST_ID + " = ?",
                    new String[]{String.valueOf(groupId), String.valueOf(postId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("GroupPostDao", "从群组移除帖子失败", e);
            return false;
        }
    }

    /**
     * 获取群组中的所有帖子
     */
    public Cursor getPostsInGroup(int groupId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_GROUP_ID + " = ?",
                new String[]{String.valueOf(groupId)},
                null, null, COLUMN_ADDED_TIME + " DESC");
    }

    /**
     * 获取帖子所属的所有群组
     */
    public Cursor getGroupsForPost(int postId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_POST_ID + " = ?",
                new String[]{String.valueOf(postId)},
                null, null, null);
    }

    /**
     * 更新帖子在群组中的可见性
     */
    public boolean updatePostVisibility(int groupId, int postId, String visibility) {
        if (!isValidVisibility(visibility)) {
            Log.e("GroupPostDao", "可见性设置不合法");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_VISIBILITY, visibility);

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_GROUP_ID + " = ? AND " + COLUMN_POST_ID + " = ?",
                    new String[]{String.valueOf(groupId), String.valueOf(postId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("GroupPostDao", "更新帖子可见性失败", e);
            return false;
        }
    }

    /**
     * 获取群组中可见的帖子
     */
    public Cursor getVisiblePostsInGroup(int groupId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_GROUP_ID + " = ? AND " + COLUMN_VISIBILITY + " = 'visible'",
                new String[]{String.valueOf(groupId)},
                null, null, COLUMN_ADDED_TIME + " DESC");
    }

    /**
     * 检查帖子是否在群组中
     */
    public boolean isPostInGroup(int groupId, int postId) {
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_POST_ID},
                COLUMN_GROUP_ID + " = ? AND " + COLUMN_POST_ID + " = ?",
                new String[]{String.valueOf(groupId), String.valueOf(postId)},
                null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    /**
     * 获取群组中的帖子数量
     */
    public int getPostCountInGroup(int groupId) {
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{"COUNT(*)"},
                COLUMN_GROUP_ID + " = ?",
                new String[]{String.valueOf(groupId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }
    }

    // 校验可见性设置
    private boolean isValidVisibility(String visibility) {
        return visibility != null && 
               (visibility.equals("visible") || visibility.equals("hidden"));
    }

    // 清空表（测试用）
    public void clearTable() {
        db.beginTransaction();
        try {
            db.delete(TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
} 