package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

public class MemoryVideoPhotoDao {
    public static final String TABLE_NAME = "MemoryVideoPhoto";
    public static final String COLUMN_MEMORY_VIDEO_ID = "memory_video_id";
    public static final String COLUMN_PHOTO_ID = "photo_id";
    public static final String COLUMN_SEQUENCE = "sequence";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_MEMORY_VIDEO_ID + " INTEGER, " +
                    COLUMN_PHOTO_ID + " INTEGER, " +
                    COLUMN_SEQUENCE + " INTEGER, " +
                    "PRIMARY KEY (" + COLUMN_MEMORY_VIDEO_ID + ", " + COLUMN_PHOTO_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_MEMORY_VIDEO_ID + ") REFERENCES " +
                    MemoryVideoDao.TABLE_NAME + "(" + MemoryVideoDao.COLUMN_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_PHOTO_ID + ") REFERENCES " +
                    PhotoDao.TABLE_NAME + "(" + PhotoDao.COLUMN_ID + "))";

    private final SQLiteDatabase db;

    public MemoryVideoPhotoDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public boolean addPhotoToMemoryVideo(int memoryVideoId, int photoId, int sequence) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEMORY_VIDEO_ID, memoryVideoId);
        values.put(COLUMN_PHOTO_ID, photoId);
        values.put(COLUMN_SEQUENCE, sequence);

        try {
            long result = db.insert(TABLE_NAME, null, values);
            return result != -1;
        } catch (SQLException e) {
            Log.e("MemoryVideoPhotoDao", "添加照片到记忆视频失败: " + e.getMessage());
            return false;
        }
    }

    public Cursor getPhotosInMemoryVideo(int memoryVideoId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_MEMORY_VIDEO_ID + " = ?",
                new String[]{String.valueOf(memoryVideoId)},
                null, null,
                COLUMN_SEQUENCE + " ASC");
    }

    public boolean updatePhotoSequence(int memoryVideoId, int photoId, int newSequence) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SEQUENCE, newSequence);

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_MEMORY_VIDEO_ID + " = ? AND " + COLUMN_PHOTO_ID + " = ?",
                    new String[]{String.valueOf(memoryVideoId), String.valueOf(photoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("MemoryVideoPhotoDao", "更新照片顺序失败", e);
            return false;
        }
    }

    public boolean removePhotoFromMemoryVideo(int memoryVideoId, int photoId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_MEMORY_VIDEO_ID + " = ? AND " + COLUMN_PHOTO_ID + " = ?",
                    new String[]{String.valueOf(memoryVideoId), String.valueOf(photoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("MemoryVideoPhotoDao", "从记忆视频移除照片失败", e);
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