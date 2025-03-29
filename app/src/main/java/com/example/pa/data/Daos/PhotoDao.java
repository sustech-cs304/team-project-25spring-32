package com.example.pa.data.Daos;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;

public class PhotoDao {
    // ===================== 表结构常量 =====================
    public static final String TABLE_NAME = "Photo";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_FILE_PATH = "file_path";
    public static final String COLUMN_FILE_URL = "file_url";
    public static final String COLUMN_UPLOADED_TIME = "uploaded_time";
    public static final String COLUMN_TAKEN_TIME = "taken_time";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_AI_OBJECTS = "ai_objects";

    // 建表SQL（包含外键约束）
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_TYPE + " TEXT NOT NULL DEFAULT 'photo' CHECK (" + COLUMN_TYPE + " IN ('photo', 'video')), " +
                    COLUMN_FILE_PATH + " TEXT, " +
                    COLUMN_FILE_URL + " TEXT, " +
                    COLUMN_UPLOADED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    COLUMN_TAKEN_TIME + " TEXT, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_AI_OBJECTS + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    UserDao.TABLE_NAME + "(" + UserDao.COLUMN_ID + "))";

    private final SQLiteDatabase db;
    private final Gson gson = new Gson();

    // ===================== 初始化 =====================
    public PhotoDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
        enableForeignKeys(); // 启用外键约束
    }

    // SQLite默认关闭外键约束，需手动启用
    private void enableForeignKeys() {
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    // ===================== 核心CRUD操作 =====================

    /**
     * 添加照片（基础信息）
     * @return 新照片ID，失败返回-1
     */
    public long addPhoto(int userId, String type, String filePath) {
        // 参数校验
        if (!isValidType(type)) {
            Log.e("PhotoDao", "类型必须为 photo 或 video");
            return -1;
        }

        if (!isUserExists(userId)) {
            Log.e("PhotoDao", "用户ID不存在");
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TYPE, type.toLowerCase());
        values.put(COLUMN_FILE_PATH, filePath);

        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("PhotoDao", "添加照片失败: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 添加完整照片信息（包括位置和AI数据）
     */
    public long addFullPhoto(Photo photo) {
        if (!isValidPhoto(photo)) return -1;

        ContentValues values = photoToContentValues(photo);
        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("PhotoDao", "添加完整照片失败", e);
            return -1;
        }
    }

    /**
     * 更新照片描述信息
     */
    public boolean updateDescription(int photoId, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, description);

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(photoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("PhotoDao", "更新描述失败", e);
            return false;
        }
    }

    /**
     * 更新AI识别结果
     */
    public boolean updateAIObjects(int photoId, List<String> aiObjects) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_AI_OBJECTS, gson.toJson(aiObjects));

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(photoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("PhotoDao", "更新AI数据失败", e);
            return false;
        }
    }

    /**
     * 删除照片（级联删除相关数据需在数据库定义）
     */
    public boolean deletePhoto(int photoId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(photoId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("PhotoDao", "删除照片失败", e);
            return false;
        }
    }

    // ===================== 查询方法 =====================

    /**
     * 根据ID获取照片详情
     */
    public Photo getPhotoById(int photoId) {
        try (Cursor cursor = db.query(TABLE_NAME,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(photoId)},
                null, null, null)) {

            return cursorToPhoto(cursor);
        } catch (Exception e) {
            Log.e("PhotoDao", "查询照片失败", e);
            return null;
        }
    }

    /**
     * 获取用户的所有照片
     */
    public Cursor getPhotosByUser(int userId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_UPLOADED_TIME + " DESC"); // 按上传时间倒序
    }

    /**
     * 获取最近上传的照片（分页）
     */
    public Cursor getRecentPhotos(int limit) {
        return db.query(TABLE_NAME,
                null,
                null, null,
                null, null,
                COLUMN_UPLOADED_TIME + " DESC",
                String.valueOf(limit));
    }

    // ===================== 工具方法 =====================

    // 数据转换：Cursor → Photo
    private Photo cursorToPhoto(Cursor cursor) {
        if (!cursor.moveToFirst()) return null;

        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
        @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
        @SuppressLint("Range") String filePath = cursor.getString(cursor.getColumnIndex(COLUMN_FILE_PATH));
        @SuppressLint("Range") String uploadedTime = cursor.getString(cursor.getColumnIndex(COLUMN_UPLOADED_TIME));
        @SuppressLint("Range") String takenTime = cursor.getString(cursor.getColumnIndex(COLUMN_TAKEN_TIME));
        @SuppressLint("Range") double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
        @SuppressLint("Range") double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
        @SuppressLint("Range") String location = cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION));
        @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        @SuppressLint("Range") String aiJson = cursor.getString(cursor.getColumnIndex(COLUMN_AI_OBJECTS));

        List<String> aiObjects = Arrays.asList(gson.fromJson(aiJson, String[].class));

        return new Photo(id, userId, type, filePath, uploadedTime, takenTime,
                longitude, latitude, location, description, aiObjects);
    }

    // 数据转换：Photo → ContentValues
    private ContentValues photoToContentValues(Photo photo) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, photo.userId);
        values.put(COLUMN_TYPE, photo.type);
        values.put(COLUMN_FILE_PATH, photo.filePath);
        values.put(COLUMN_FILE_URL, photo.fileUrl);
        values.put(COLUMN_TAKEN_TIME, photo.takenTime);
        values.put(COLUMN_LONGITUDE, photo.longitude);
        values.put(COLUMN_LATITUDE, photo.latitude);
        values.put(COLUMN_LOCATION, photo.location);
        values.put(COLUMN_DESCRIPTION, photo.description);
        values.put(COLUMN_AI_OBJECTS, gson.toJson(photo.aiObjects));
        return values;
    }

    // ===================== 校验方法 =====================

    private boolean isValidType(String type) {
        return "photo".equalsIgnoreCase(type) || "video".equalsIgnoreCase(type);
    }

    private boolean isUserExists(int userId) {
        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + UserDao.TABLE_NAME + " WHERE " + UserDao.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)})) {
            return cursor.getCount() > 0;
        }
    }

    private boolean isValidPhoto(Photo photo) {
        return photo != null &&
                isValidType(photo.type) &&
                isUserExists(photo.userId) &&
                photo.filePath != null;
    }

    // ===================== 数据模型 =====================

    public static class Photo {
        public final int id;
        public final int userId;
        public final String type;
        public final String filePath;
        public final String fileUrl;
        public final String uploadedTime;
        public final String takenTime;
        public final double longitude;
        public final double latitude;
        public final String location;
        public final String description;
        public final List<String> aiObjects;

        public Photo(int id, int userId, String type, String filePath,
                     String uploadedTime, String takenTime,
                     double longitude, double latitude,
                     String location, String description,
                     List<String> aiObjects) {
            this.id = id;
            this.userId = userId;
            this.type = type;
            this.filePath = filePath;
            this.fileUrl = null; // 未来删除
            this.uploadedTime = uploadedTime;
            this.takenTime = takenTime;
            this.longitude = longitude;
            this.latitude = latitude;
            this.location = location;
            this.description = description;
            this.aiObjects = aiObjects;
        }
    }

    // ===================== 测试辅助 =====================

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
}