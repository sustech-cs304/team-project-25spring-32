package com.example.pa.data.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MusicItem {
    private final String name; // 用于显示在下拉菜单中的名称
    private final Uri uri;     // 音乐文件的 Uri (可以是资源 Uri, 文件 Uri, 或网络 Uri)

    public MusicItem(@NonNull String name, @Nullable Uri uri) {
        this.name = name;
        this.uri = uri;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public Uri getUri() {
        return uri;
    }

    // 重写 toString() 方便 ArrayAdapter 直接使用
    @NonNull
    @Override
    public String toString() {
        return name;
    }
}