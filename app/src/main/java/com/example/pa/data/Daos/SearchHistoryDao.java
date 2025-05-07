package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

public class SearchHistoryDao {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: I directly copy the code from its response and modify the logic of some method, add some
     * methods we need but it did not generate, and add some logs.
     */
    public static final String TABLE_NAME = "SearchHistory";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_QUERY = "query";
    public static final String COLUMN_CREATED_TIME = "created_time";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_QUERY + " TEXT NOT NULL, " +
                    COLUMN_CREATED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    UserDao.TABLE_NAME + "(" + UserDao.COLUMN_ID + "))";

    private final SQLiteDatabase db;

    public SearchHistoryDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public long addSearchHistory(int userId, String query) {
        if (query == null || query.isEmpty()) {
            Log.e("SearchHistoryDao", "搜索内容为空");
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_QUERY, query);

        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("SearchHistoryDao", "添加搜索记录失败: " + e.getMessage());
            return -1;
        }
    }
    public boolean hasSearchHistory(int userId) {
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
        boolean hasHistory = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return hasHistory;
    }

    public Cursor getSearchHistoryByUser(int userId, int limit) {
        return db.rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COLUMN_USER_ID + " = ? " +
                        " GROUP BY " + COLUMN_QUERY +
                        " ORDER BY MAX(" + COLUMN_CREATED_TIME + ") DESC " +
                        " LIMIT ?",
                new String[]{String.valueOf(userId), String.valueOf(limit)}
        );
    }

    public boolean clearUserHistory(int userId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("SearchHistoryDao", "清除搜索历史失败", e);
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

    public static class SearchHistory {
        public final int id;
        public final int userId;
        public final String query;
        public final String createdTime;

        public SearchHistory(int id, int userId, String query, String createdTime) {
            this.id = id;
            this.userId = userId;
            this.query = query;
            this.createdTime = createdTime;
        }
    }
}