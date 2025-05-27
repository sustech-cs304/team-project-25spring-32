package com.example.pa.data.model;
// Photo.java - 对应后端返回的图片数据

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.util.List;

public class Photo {
    public final int id;
    public final int userId;
    public final String type;
    public String filePath;
    public final String fileUrl;
    public final String uploadedTime;
    public final String takenTime;
    public final double longitude;
    public final double latitude;
    public final String location;
    public final String description;
    public final List<String> aiObjects;
    public String filename; // 添加这个字段以保持兼容性

    public Photo(int id, int userId, String type, String filePath, String fileUrl,
                 String uploadedTime, String takenTime,
                 double longitude, double latitude,
                 String location, String description,
                 List<String> aiObjects) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.filePath = filePath;
        this.fileUrl = fileUrl; // 未来删除
        this.uploadedTime = uploadedTime;
        this.takenTime = takenTime;
        this.longitude = longitude;
        this.latitude = latitude;
        this.location = location;
        this.description = description;
        this.aiObjects = aiObjects;
        // 从文件路径提取文件名
        if (filePath != null) {
            int lastSlash = filePath.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < filePath.length() - 1) {
                this.filename = filePath.substring(lastSlash + 1);
            }
        }
    }

    // 从 URI 获取相册名（通过 MediaStore）
    public String extractAlbumName(Context context) {
        String[] projection = {MediaStore.Images.Media.RELATIVE_PATH};
        try (Cursor cursor = context.getContentResolver().query(
                Uri.parse(filePath),
                projection,
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(0);
                return parseAlbumNameFromRelativePath(path);
            }
        } catch (SecurityException e) {
            Log.e("Photo", "权限不足无法查询URI: " + filePath);
        }
        return null;
    }

    // 解析相对路径获取相册名（如 "DCIM/Vacation" → "Vacation"）
    private String parseAlbumNameFromRelativePath(String relativePath) {
        if (relativePath == null) return null;
        String[] segments = relativePath.split("/");
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].equals(Environment.DIRECTORY_DCIM) && i + 1 < segments.length) {
                return segments[i + 1];
            }
        }
        return null;
    }
}