package com.example.pa.data.model;
// Photo.java - 对应后端返回的图片数据

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
}
