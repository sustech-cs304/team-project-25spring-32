package com.example.pa.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * AI-generated-content
 * tool: DeepSeek
 * version: R1
 * usage: I asked how to create a local folder, and
 * directly copy the code from its response.
 */
public class FileRepository {
    private final Context context;

    public FileRepository(Context context) {
        this.context = context;
    }

    public boolean createAlbum(String albumName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return createAlbumWithMediaStore(albumName);
        } else {
            // Android 9 及以下逻辑（按需保留）
            return false;
        }
    }

    public boolean deleteAlbum(String albumName) {
        // 类似 createAlbum 的实现，反向操作删除文件夹
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return deleteAlbumWithMediaStore(albumName);
        } else {
            // Android 9 及以下逻辑（按需保留）
            return false;
        }
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to create a local folder, and
     * directly copy the code from its response.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean createAlbumWithMediaStore(String albumName) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        // 通过插入一个空文件来隐式创建文件夹
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "placeholder_" + System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + albumName);

        try {
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                // 删除占位文件（可选）
                resolver.delete(uri, null, null);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to delete a local folder, and
     * directly copy the code from its response.
     */
    // Android 10+ 删除逻辑
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean deleteAlbumWithMediaStore(String albumName) {
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        // 1. 查询相册内的所有文件
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{Environment.DIRECTORY_DCIM + "/" + albumName + "/%"};

        try (Cursor cursor = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // 2. 逐个删除文件
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri uri = ContentUris.withAppendedId(collection, id);
                    resolver.delete(uri, null, null);
                }
            }
            // 3. 尝试删除空文件夹（部分系统可能不支持）
            return deleteEmptyFolder(albumName, resolver);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to delete a local folder, and
     * directly copy the code from its response.
     */
    // 删除空文件夹（可选）
    //TODO: test whether the methods can only delete files in the empty folder or doesn't work.
    private boolean deleteEmptyFolder(String albumName, ContentResolver resolver) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + albumName);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);
        try {
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                resolver.delete(uri, null, null);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

