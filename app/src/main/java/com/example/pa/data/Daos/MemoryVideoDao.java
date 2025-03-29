package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

public class MemoryVideoDao {
    public static final String TABLE_NAME = "MemoryVideo";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_CREATED_TIME = "created_time";
    public static final String COLUMN_MUSIC_URL = "music_url";
    public static final String COLUMN_VIDEO_URL = "video_url";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_THEME + " TEXT, " +
                    COLUMN_CREATED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    COLUMN_MUSIC_URL + " TEXT, " +
                    COLUMN_VIDEO_URL + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    UserDao.TABLE_NAME + "(" + UserDao.COLUMN_ID + "))";

    private final SQLiteDatabase db;

    public MemoryVideoDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public long addMemoryVideo(int userId, String name, String theme, String musicUrl) {
        if (name == null || name.isEmpty()) {
            Log.e("MemoryVideoDao", "视频名称不合法");
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_THEME, theme);
        values.put(COLUMN_MUSIC_URL, musicUrl);

        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("MemoryVideoDao", "添加记忆视频失败: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateVideoUrl(int videoId, String videoUrl) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_URL, videoUrl);

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(videoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("MemoryVideoDao", "更新视频URL失败", e);
            return false;
        }
    }

    public Cursor getMemoryVideosByUser(int userId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                COLUMN_CREATED_TIME + " DESC");
    }

    public boolean deleteMemoryVideo(int videoId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(videoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("MemoryVideoDao", "删除记忆视频失败", e);
            return false;
        }
    }

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

    public static class MemoryVideo {
        public final int id;
        public final int userId;
        public final String name;
        public final String theme;
        public final String createdTime;
        public final String musicUrl;
        public final String videoUrl;

        public MemoryVideo(int id, int userId, String name, String theme,
                           String createdTime, String musicUrl, String videoUrl) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.theme = theme;
            this.createdTime = createdTime;
            this.musicUrl = musicUrl;
            this.videoUrl = videoUrl;
        }
    }
}