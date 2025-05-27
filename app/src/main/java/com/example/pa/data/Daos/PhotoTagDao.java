package com.example.pa.data.Daos;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PhotoTagDao {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: I directly copy the code from its response and modify the logic of some method, add some
     * methods we need but it did not generate, and add some logs.
     */
    public static final String TABLE_NAME = "PhotoTag";
    public static final String COLUMN_PHOTO_ID = "photo_id";
    public static final String COLUMN_TAG_ID = "tag_id";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_PHOTO_ID + " INTEGER, " +
                    COLUMN_TAG_ID + " INTEGER, " +
                    "PRIMARY KEY (" + COLUMN_PHOTO_ID + ", " + COLUMN_TAG_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_PHOTO_ID + ") REFERENCES " +
                    PhotoDao.TABLE_NAME + "(" + PhotoDao.COLUMN_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_TAG_ID + ") REFERENCES " +
                    TagDao.TABLE_NAME + "(" + TagDao.COLUMN_ID + "))";

    private final SQLiteDatabase db;

    public PhotoTagDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public boolean addTagToPhoto(int photoId, int tagId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO_ID, photoId);
        values.put(COLUMN_TAG_ID, tagId);

        try {
            long result = db.insert(TABLE_NAME, null, values);
            return result != -1;
        } catch (SQLException e) {
            Log.e("PhotoTagDao", "添加标签到照片失败: " + e.getMessage());
            return false;
        }
    }

    public Cursor getTagsForPhoto(int photoId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_PHOTO_ID + " = ?",
                new String[]{String.valueOf(photoId)},
                null, null, null);
    }

    @SuppressLint("Range")
    public List<Integer> getPhotoIdsByTag(int tagId) {
        List<Integer> photoIds = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_PHOTO_ID},
                COLUMN_TAG_ID + " = ?",
                new String[]{String.valueOf(tagId)},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                photoIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_PHOTO_ID)));
            }
            cursor.close();
        }
        return photoIds;
    }

    public boolean removeTagFromPhoto(int photoId, int tagId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_PHOTO_ID + " = ? AND " + COLUMN_TAG_ID + " = ?",
                    new String[]{String.valueOf(photoId), String.valueOf(tagId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("PhotoTagDao", "从照片移除标签失败", e);
            return false;
        }
    }

    public boolean removeTagFromPhotoByPhoto(int photoId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_PHOTO_ID + " = ?",
                    new String[]{String.valueOf(photoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("PhotoTagDao", "从照片移除标签失败", e);
            return false;
        }
    }

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