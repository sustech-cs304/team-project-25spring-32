package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

public class TagDao {
    public static final String TABLE_NAME = "Tag";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IS_AI_RECOGNIZED = "is_ai_recognized";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_IS_AI_RECOGNIZED + " INTEGER DEFAULT 0)";

    private final SQLiteDatabase db;

    public TagDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public long addTag(String name, boolean isAiRecognized) {
        if (name == null || name.isEmpty()) {
            Log.e("TagDao", "标签名称不合法");
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_IS_AI_RECOGNIZED, isAiRecognized ? 1 : 0);

        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("TagDao", "添加标签失败: " + e.getMessage());
            return -1;
        }
    }

    public Cursor getAllTags() {
        return db.query(TABLE_NAME,
                null,
                null, null,
                null, null,
                COLUMN_NAME + " ASC");
    }

    public Cursor getAiRecognizedTags() {
        return db.query(TABLE_NAME,
                null,
                COLUMN_IS_AI_RECOGNIZED + " = 1",
                null,
                null, null,
                COLUMN_NAME + " ASC");
    }

    public boolean deleteTag(int tagId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(tagId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("TagDao", "删除标签失败", e);
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

    public static class Tag {
        public final int id;
        public final String name;
        public final boolean isAiRecognized;

        public Tag(int id, String name, boolean isAiRecognized) {
            this.id = id;
            this.name = name;
            this.isAiRecognized = isAiRecognized;
        }
    }
}