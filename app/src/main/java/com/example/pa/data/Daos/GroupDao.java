package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupDao {
    public static final String TABLE_NAME = "Groups";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CREATED_TIME = "created_time";
    public static final String COLUMN_CREATOR_ID = "creator_id";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_CREATED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    COLUMN_CREATOR_ID + " INTEGER NOT NULL)";

    private final SQLiteDatabase db;
    private final Context context;

    public GroupDao(Context context) {
        this.context = context;
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    /**
     * 创建新群组
     */
    public long createGroup(String name, String description, int creatorId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CREATOR_ID, creatorId);

        try {
            long groupId = db.insert(TABLE_NAME, null, values);
            if (groupId != -1) {
                // 将创建者添加为群组管理员
                GroupMemberDao groupMemberDao = new GroupMemberDao(context);
                groupMemberDao.addMember((int) groupId, creatorId, "admin");
            }
            return groupId;
        } catch (SQLException e) {
            Log.e("GroupDao", "创建群组失败: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 获取群组信息
     */
    public Cursor getGroup(int groupId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(groupId)},
                null, null, null);
    }

    /**
     * 更新群组信息
     */
    public boolean updateGroup(int groupId, String name, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(groupId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("GroupDao", "更新群组失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除群组
     */
    public boolean deleteGroup(int groupId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(groupId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("GroupDao", "删除群组失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户创建的所有群组
     */
    public Cursor getGroupsByCreator(int creatorId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_CREATOR_ID + " = ?",
                new String[]{String.valueOf(creatorId)},
                null, null, COLUMN_CREATED_TIME + " DESC");
    }

    /**
     * 获取用户所在的所有群组
     */
    public Cursor getUserGroups(int userId) {
        GroupMemberDao groupMemberDao = new GroupMemberDao(context);
        Cursor memberCursor = groupMemberDao.getUserGroups(userId);
        
        if (memberCursor != null && memberCursor.moveToFirst()) {
            List<Integer> groupIds = new ArrayList<>();
            do {
                int groupId = memberCursor.getInt(memberCursor.getColumnIndexOrThrow(GroupMemberDao.COLUMN_GROUP_ID));
                groupIds.add(groupId);
            } while (memberCursor.moveToNext());
            memberCursor.close();

            if (!groupIds.isEmpty()) {
                String placeholders = String.join(",", Collections.nCopies(groupIds.size(), "?"));
                String[] args = groupIds.stream().map(String::valueOf).toArray(String[]::new);
                
                return db.query(TABLE_NAME,
                        null,
                        COLUMN_ID + " IN (" + placeholders + ")",
                        args,
                        null, null,
                        COLUMN_CREATED_TIME + " DESC");
            }
        }
        return null;
    }

    /**
     * 检查群组是否存在
     */
    public boolean isGroupExists(int groupId) {
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(groupId)},
                null, null, null)) {
            return cursor.getCount() > 0;
        }
    }
} 