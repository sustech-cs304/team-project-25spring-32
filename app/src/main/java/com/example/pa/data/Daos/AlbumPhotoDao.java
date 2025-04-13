package com.example.pa.data.Daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;

public class AlbumPhotoDao {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: I directly copy the code from its response and modify the logic of some method, add some
     * methods we need but it did not generate, and add some logs.
     */
    public static final String TABLE_NAME = "AlbumPhoto";
    public static final String COLUMN_ALBUM_ID = "album_id";
    public static final String COLUMN_PHOTO_ID = "photo_id";
    public static final String COLUMN_ADDED_TIME = "added_time";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ALBUM_ID + " INTEGER, " +
                    COLUMN_PHOTO_ID + " INTEGER, " +
                    COLUMN_ADDED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    "PRIMARY KEY (" + COLUMN_ALBUM_ID + ", " + COLUMN_PHOTO_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_ALBUM_ID + ") REFERENCES " +
                    AlbumDao.TABLE_NAME + "(" + AlbumDao.COLUMN_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_PHOTO_ID + ") REFERENCES " +
                    PhotoDao.TABLE_NAME + "(" + PhotoDao.COLUMN_ID + "))";

    private final SQLiteDatabase db;

    public AlbumPhotoDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public boolean addPhotoToAlbum(int albumId, int photoId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_ID, albumId);
        values.put(COLUMN_PHOTO_ID, photoId);

        try {
            long result = db.insert(TABLE_NAME, null, values);
            return result != -1;
        } catch (SQLException e) {
            Log.e("AlbumPhotoDao", "添加照片到相册失败: " + e.getMessage());
            return false;
        }
    }

    public Cursor getPhotosInAlbum(int albumId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_ALBUM_ID + " = ?",
                new String[]{String.valueOf(albumId)},
                null, null, COLUMN_ADDED_TIME + " DESC");
    }

    public boolean removePhotoFromAlbum(int albumId, int photoId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_ALBUM_ID + " = ? AND " + COLUMN_PHOTO_ID + " = ?",
                    new String[]{String.valueOf(albumId), String.valueOf(photoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("AlbumPhotoDao", "从相册移除照片失败", e);
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