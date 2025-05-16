package com.example.pa.data;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * AI-generated-content
 * tool: DeepSeek
 * version: R1
 * usage: I asked how to create a local folder, and
 * directly copy the code from its response.
 */
public class FileRepository {
    private static final int DELETE_REQUEST_CODE = 1002;
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
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private boolean deleteAlbumWithMediaStore(String albumName) {
//        ContentResolver resolver = context.getContentResolver();
//        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
//
//        // 关键：修正查询条件，确保包含子文件夹
//        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ? ESCAPE '!'";
//        String escapedAlbumName = albumName.replace("_", "!_"); // 处理特殊字符
//        String[] selectionArgs = new String[]{Environment.DIRECTORY_DCIM + "/" + escapedAlbumName + "/%"};
//
//        try (Cursor cursor = resolver.query(collection, null, selection, selectionArgs, null)) {
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//                    Uri uri = ContentUris.withAppendedId(collection, id);
//                    int deleted = resolver.delete(uri, null, null);
//                    Log.d("Delete", "删除文件: " + uri + " 结果: " + (deleted > 0));
//                }
//            }
//            // 删除空文件夹（部分系统支持）
//            return deleteEmptyFolder(albumName, resolver);
//        } catch (Exception e) {
//            Log.e("Delete", "删除失败", e);
//            return false;
//        }
//    }


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
    @RequiresApi(api = Build.VERSION_CODES.Q) // minSdk 29
    private boolean deleteAlbumWithMediaStore(String albumName) {
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{Environment.DIRECTORY_DCIM + "/" + albumName + "/%"};

        List<Uri> toDelete = new ArrayList<>();

        try (Cursor cursor = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri uri = ContentUris.withAppendedId(collection, id);
                    toDelete.add(uri);
                }
            }
        } catch (Exception e) {
            Log.e("Delete", "查询失败", e);
            return false;
        }

        if (toDelete.isEmpty()) {
            Log.d("Delete", "没有找到要删除的文件");
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
            try {
                // 反射调用 MediaStore.createDeleteRequest()
                Method method = MediaStore.class.getMethod("createDeleteRequest", ContentResolver.class, List.class);
                PendingIntent deleteRequest = (PendingIntent) method.invoke(null, resolver, toDelete);

                if (context instanceof Activity) {
                    ((Activity) context).startIntentSenderForResult(
                            deleteRequest.getIntentSender(),
                            DELETE_REQUEST_CODE,
                            null, 0, 0, 0
                    );
                    return true;
                } else {
                    Log.e("Delete", "Context 不是 Activity，无法请求删除权限");
                    return false;
                }
            } catch (Exception e) {
                Log.e("Delete", "反射调用 createDeleteRequest 失败", e);
                return false;
            }
        } else {
            Log.e("Delete", "系统版本过低，不支持 createDeleteRequest");
            return false;
        }
    }



    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to get files from folder, and
     * directly copy the code from its response.
     */
    public List<Uri> getAlbumImages(String albumName) {
        List<Uri> imageUris = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        // 调整查询条件：使用 LIKE 和通配符
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs;
        if (albumName.equals("所有照片")) {
            selectionArgs = new String[]{Environment.DIRECTORY_DCIM + "/%"};
        } else {
            selectionArgs = new String[]{Environment.DIRECTORY_DCIM + "/" + albumName + "/%"};
        }
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        // 扩展查询字段
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH
        };

        try (Cursor cursor = resolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder)
        ) {
            if (cursor != null) {
                Log.d("MediaQuery", "找到文件数量: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri uri = ContentUris.withAppendedId(collection, id);
                    imageUris.add(uri);

                    // 调试日志
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH));
                    Log.d("MediaQuery", "文件: " + name + " | 路径: " + path);
                }
            } else {
                Log.e("MediaQuery", "查询返回空 Cursor");
            }
        } catch (SecurityException e) {
            Log.e("MediaQuery", "权限不足: " + e.getMessage());
        } catch (Exception e) {
            Log.e("MediaQuery", "查询失败: " + e.getMessage());
        }

        return imageUris;
    }

    // 获取封面（最新一张图片）
    public Uri getAlbumCover(String albumName) {
        List<Uri> images = getAlbumImages(albumName);
        Log.d("FileRepository", "getAlbumCover from " + albumName + " : " + images);
        return images.isEmpty() ? null : images.get(0);
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to solve the problem of asynchronous scan, and
     * directly copy the code from its response.
     */
    public interface MediaScanCallback {
        void onScanCompleted(Uri uri);
        void onScanFailed(String error);
    }

    // 修改扫描方法，增加回调参数
    public void triggerMediaScanForAlbum(String albumName, MediaScanCallback callback) {
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File albumDir = new File(dcimDir, albumName);

        MediaScannerConnection.scanFile(
                context,
                new String[]{albumDir.getAbsolutePath()},
                new String[]{"image/*"},
                (path, uri) -> {
                    if (uri != null) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onScanCompleted(uri));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onScanFailed("Scan failed for: " + path));
                    }
                }
        );
    }

//    public void triggerMediaScanForDirectory(File directory, MediaScanCallback callback) {
//        ContentResolver resolver = context.getContentResolver();
//        Uri collection = MediaStore.Images.Media.getContentUri(
//                MediaStore.VOLUME_EXTERNAL_PRIMARY);
//
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/");
//        values.put(MediaStore.Images.Media.IS_PENDING, 1);
//
//        try {
//            Uri uri = resolver.insert(collection, values);
//            if (uri != null) {
//                values.clear();
//                values.put(MediaStore.Images.Media.IS_PENDING, 0);
//                resolver.update(uri, values, null, null);
//                callback.onScanCompleted(uri);
//            }
//        } catch (Exception e) {
//            callback.onScanFailed(e.getMessage());
//        }
//    }
    public void triggerMediaScanForDirectory(File directory, MediaScanCallback callback) {
        // 扫描当前目录
        scanSingleDirectory(directory, callback);

        // 递归扫描子目录
        scanSubdirectories(directory);
    }

    private void scanSingleDirectory(File dir, MediaScanCallback callback) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{dir.getAbsolutePath()},
                new String[]{"image/*", "video/*"},
                (path, uri) -> {
                    if (uri != null) {
                        Log.d("MediaScan", "Scanned: " + path);
                    }
                }
        );
    }

    private void scanSubdirectories(File parentDir) {
        File[] subDirs = parentDir.listFiles(File::isDirectory);
        if (subDirs == null) return;

        for (File dir : subDirs) {
            scanSingleDirectory(dir, null); // 不需要回调
            scanSubdirectories(dir); // 继续递归
        }
    }
}

