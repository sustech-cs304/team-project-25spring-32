package com.example.pa.util;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.pa.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class UriToPathHelper {

    private static final String TAG = "UriToPathHelper";

    public static String getPathFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        // 尝试直接从Uri获取路径 (对 file:// URIs 有效)
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        // 对于 content:// URIs, 复制到应用缓存目录
        File cacheDir = context.getCacheDir();
        File tempFile = null;
        String fileName = getFileName(uri); // 尝试获取原始文件名

        try {
            tempFile = new File(cacheDir, "temp_" + System.currentTimeMillis() + "_" + fileName);
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {
                if (inputStream == null) {
                    Log.e(TAG, "InputStream is null for URI: " + uri.toString());
                    return null;
                }
                byte[] buffer = new byte[4096]; // 4KB buffer
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                Log.d(TAG, "File copied to: " + tempFile.getAbsolutePath());
                return tempFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying URI to cache file", e);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete(); // 清理失败的临时文件
            }
            return null; // 或者抛出自定义异常
        }
    }

    public static String getRealPathFromUri(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (SecurityException e) {
            Log.e("Photo", "Error getting real path: " + e.getMessage());
        }
        return null;
    }

    // 简单的文件名提取 (可能不总是准确，取决于URI提供者)
    private static String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = android.provider.MediaStore.Images.Media.query(
                    MyApplication.getAppContext().getContentResolver(), // 请确保你有可用的 Context
                    uri,
                    new String[]{android.provider.MediaStore.Images.ImageColumns.DISPLAY_NAME},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.ImageColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to get display name for content URI: " + uri, e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        // 如果文件名包含非法字符，进行清理或替换
        if (result != null) {
            result = result.replaceAll("[^a-zA-Z0-9._-]", "_");
        } else {
            result = UUID.randomUUID().toString(); // 备用文件名
        }
        return result;
    }
}
