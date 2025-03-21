package com.example.pa.ui.photo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "image_items")
public class ImageItem {
    // 核心标识
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    // 资源定位
    @ColumnInfo(name = "url", index = true, collate = ColumnInfo.NOCASE)
    private String url;

    // 缓存管理
    @ColumnInfo(name = "local_path")
    private String localPath;

    // 元数据
    @ColumnInfo(name = "file_size", defaultValue = "-1")
    private Long fileSize;

    @ColumnInfo(name = "width", defaultValue = "0")
    private Integer width;

    @ColumnInfo(name = "height", defaultValue = "0")
    private Integer height;

    // 安全校验
    @ColumnInfo(name = "hash", collate = ColumnInfo.BINARY)
    private String hash;

    // 时间管理
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private Date createdAt;

    @ColumnInfo(name = "last_accessed", index = true)
    private Date lastAccessed;

    // 用户状态
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    private Boolean isFavorite;

    // 文件类型
    @ColumnInfo(name = "mime_type", defaultValue = "'image/jpeg'")
    private String mimeType;

    // Room必需的无参构造
    public ImageItem(String url) {
        this.url = url;
        this.createdAt = new Date();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalCachePath() {
        return localPath;
    }

    public void setLocalCachePath(String localPath) {
        this.localPath = localPath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}