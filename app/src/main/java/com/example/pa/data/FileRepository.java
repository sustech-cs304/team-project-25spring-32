package com.example.pa.data;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI-generated-content
 * tool: DeepSeek
 * version: R1
 * usage: I asked how to create a local folder, and
 * directly copy the code from its response.
 */
public class FileRepository {
    public interface DeleteCallback {
        void onComplete();
        void onError(String error);
    }

    private DeleteCallback deleteCallback;
    public static final int DELETE_REQUEST_CODE = 1002;
    private final Context context;
    private List<Uri> pendingDeleteUris;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public FileRepository(Context context) {
        this.context = context;
    }

    public void setDeleteCallback(DeleteCallback callback) {
        this.deleteCallback = callback;
    }

    public boolean createAlbum(String albumName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return createAlbumWithMediaStore(albumName);
        } else {
            // Android 9 及以下逻辑（按需保留）
            return false;
        }
    }

//    public boolean deleteAlbum(String albumName, Activity activity) {
//        // 类似 createAlbum 的实现，反向操作删除文件夹
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            return deleteAlbumWithMediaStore(albumName, activity);
//        } else {
//            // Android 9 及以下逻辑（按需保留）
//            return false;
//        }
//    }

    // FileRepository.java
    public interface DeleteRequestProvider {
        void provideDeleteRequest(PendingIntent deleteIntent);
    }

    public void deleteAlbum(List<Uri> uris, DeleteRequestProvider provider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                PendingIntent deleteIntent = MediaStore.createDeleteRequest(
                        context.getContentResolver(),
                        uris
                );
                Log.d("FileRepository", "DeleteIntent created");
                this.pendingDeleteUris = uris;
                provider.provideDeleteRequest(deleteIntent);
            } catch (Exception e) {
                Log.e("Delete", "创建删除请求失败", e);
            }
        }
    }

    public void executePhysicalDelete(List<Uri> uris) {
        this.pendingDeleteUris = uris; // 保存待删除的URI列表

        new Thread(() -> {
            AtomicBoolean success = new AtomicBoolean(true);
            for (Uri uri : pendingDeleteUris) {
                try {
                    int deleted = context.getContentResolver().delete(uri, null, null);
                    if (deleted <= 0) {
                        Log.w("Delete", "删除失败: " + uri);
                        success.set(false);
                    }
                } catch (SecurityException e) {
                    Log.e("Delete", "权限不足: " + e.getMessage());
                    success.set(false);
                }
            }

            // 通过Handler回到主线程
            new Handler(Looper.getMainLooper()).post(() -> {
                if (deleteCallback != null) {
                    if (success.get()) {
                        deleteCallback.onComplete();
                    } else {
                        deleteCallback.onError("部分文件删除失败");
                    }
                }
                pendingDeleteUris = null; // 清理临时数据
            });
        }).start();
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
//    private boolean deleteEmptyFolder(String albumName, ContentResolver resolver) {
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + albumName);
//        values.put(MediaStore.Images.Media.IS_PENDING, 1);
//        try {
//            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            if (uri != null) {
//                resolver.delete(uri, null, null);
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//    @RequiresApi(api = Build.VERSION_CODES.Q) // minSdk 29
//    private boolean deleteAlbumWithMediaStore(String albumName, Activity activity) {
//        ContentResolver resolver = context.getContentResolver();
//        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
//
//        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
//        String[] selectionArgs = new String[]{Environment.DIRECTORY_DCIM + "/" + albumName + "/%"};
//
//        List<Uri> toDelete = new ArrayList<>();
//
//        try (Cursor cursor = resolver.query(collection, null, selection, selectionArgs, null)) {
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//                    Uri uri = ContentUris.withAppendedId(collection, id);
//                    toDelete.add(uri);
//                }
//            }
//        } catch (Exception e) {
//            Log.e("Delete", "查询失败", e);
//            return false;
//        }
//
//        if (toDelete.isEmpty()) {
//            Log.d("Delete", "没有找到要删除的文件");
//            return true;
//        }
//
//        Log.d("Delete", "找到要删除的文件数量：" + toDelete.size() );
//        return deleteImages(toDelete, activity);
//    }

    // 在FileRepository类中添加以下方法

    //=== 删除照片 ===//
//    public boolean deleteImages(List<Uri> imageUri, Activity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            return deleteImagesWithMediaStore(imageUri, activity);
//        } else {
//            // Android 9及以下实现
//            try {
////                return context.getContentResolver().delete(imageUri, null, null) > 0;
//                Log.e("Delete", "版本低于Android10");
//                return false;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//    }

//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private boolean deleteImagesWithMediaStore(List<Uri> imageUri) {
//        try {
//            // 处理需要用户确认的删除请求
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                List<Uri> uris = new ArrayList<>(imageUri);
//
//                PendingIntent deleteIntent = MediaStore.createDeleteRequest(
//                        context.getContentResolver(),
//                        uris
//                );
//
//                if (context instanceof Activity) {
//                    ((Activity) context).startIntentSenderForResult(
//                            deleteIntent.getIntentSender(),
//                            DELETE_REQUEST_CODE,
//                            null, 0, 0, 0
//                    );
//                    return true;
//                }
//                return false;
//            } else {
//                Log.e("Delete", "版本低于Android10");
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//private static final String TAG = "FileRepoDelete"; // 统一定义日志标签
//
//    // 修改方法签名，添加Activity参数
//    public interface DeleteRequestLauncher {
//        void launchDeleteIntent(PendingIntent deleteIntent) throws IntentSender.SendIntentException;
//    }
//
//    private boolean deleteImagesWithMediaStore(List<Uri> imageUris, Activity activity) {
//        Log.d(TAG, "开始删除操作 | URIs数量: " + imageUris.size());
//
//        try {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//                Log.e(TAG, "系统版本过低 (API " + Build.VERSION.SDK_INT + ")");
//                return false;
//            }
//
//            // 检查URI有效性（同上个回答）
//            for (Uri uri : imageUris) {
//                if (!isValidMediaUri(uri)) return false;
//            }
//
//            ContentResolver resolver = activity.getContentResolver();
//            PendingIntent deleteIntent = MediaStore.createDeleteRequest(
//                    resolver,
//                    imageUris// Android 12+需要
//            );
//
//            // 通过回调启动删除请求
//            launcher.launchDeleteIntent(deleteIntent);
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "删除操作异常: " + e.getMessage());
//            return false;
//        }
//    }
//
//    // 新增URI有效性检查方法
//    private boolean isValidMediaUri(Uri uri) {
//        try {
//            ContentResolver resolver = context.getContentResolver();
//            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
//                return cursor != null && cursor.getCount() > 0;
//            }
//        } catch (SecurityException e) {
//            Log.e(TAG, "无权限访问URI: " + uri, e);
//            return false;
//        } catch (Exception e) {
//            Log.e(TAG, "URI检查失败: " + uri, e);
//            return false;
//        }
//    }
    //=== 复制照片 ===//
//    public boolean copyImage(Uri sourceUri, String targetAlbumName) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            return copyImageWithMediaStore(sourceUri, targetAlbumName);
//        } else {
//            // Android 9及以下实现（使用传统文件操作）
//            // 注意需要处理运行时权限
//            return false;
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private boolean copyImageWithMediaStore(Uri sourceUri, String targetAlbumName) {
//        ContentResolver resolver = context.getContentResolver();
//
//        try {
//            // 1. 获取源文件信息
//            ContentValues sourceValues = getMediaInfo(sourceUri);
//            if (sourceValues == null) return false;
//
//            // 2. 创建目标相册（如果不存在）
//            if (!createAlbum(targetAlbumName)) {
//                Log.e("Copy", "目标相册创建失败");
//                return false;
//            }
//
//            // 3. 创建目标文件元数据
//            ContentValues targetValues = new ContentValues();
//            targetValues.put(MediaStore.Images.Media.DISPLAY_NAME,
//                    generateUniqueFileName(sourceValues.getAsString(MediaStore.Images.Media.DISPLAY_NAME)));
//            targetValues.put(MediaStore.Images.Media.MIME_TYPE,
//                    sourceValues.getAsString(MediaStore.Images.Media.MIME_TYPE));
//            targetValues.put(MediaStore.Images.Media.RELATIVE_PATH,
//                    Environment.DIRECTORY_DCIM + "/" + targetAlbumName);
//
//            // 4. 插入目标文件
//            Uri targetUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, targetValues);
//            if (targetUri == null) return false;
//
//            // 5. 执行文件拷贝
//            try (InputStream in = resolver.openInputStream(sourceUri);
//                 OutputStream out = resolver.openOutputStream(targetUri)) {
//                if (in == null || out == null) return false;
//
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//            }
//
//            // 6. 更新媒体库
//            triggerMediaScanForAlbum(targetAlbumName, new MediaScanCallback() {
//                @Override public void onScanCompleted(Uri uri) {}
//                @Override public void onScanFailed(String error) {}
//            });
//
//            return true;
//        } catch (Exception e) {
//            Log.e("Copy", "复制失败", e);
//            return false;
//        }
//    }
//
//    //=== 移动照片 ===//
//    public boolean moveImage(Uri sourceUri, String targetAlbumName) {
//        if (copyImage(sourceUri, targetAlbumName)) {
//            return deleteImages(sourceUri);
//        }
//        return false;
//    }
//
//    //=== 辅助方法 ===//
//    private ContentValues getMediaInfo(Uri uri) {
//        ContentResolver resolver = context.getContentResolver();
//        String[] projection = new String[]{
//                MediaStore.Images.Media.DISPLAY_NAME,
//                MediaStore.Images.Media.MIME_TYPE,
//                MediaStore.Images.Media.RELATIVE_PATH
//        };
//
//        try (Cursor cursor = resolver.query(uri, projection, null, null, null)) {
//            if (cursor != null && cursor.moveToFirst()) {
//                ContentValues values = new ContentValues();
//                values.put(MediaStore.Images.Media.DISPLAY_NAME,
//                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
//                values.put(MediaStore.Images.Media.MIME_TYPE,
//                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)));
//                values.put(MediaStore.Images.Media.RELATIVE_PATH,
//                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)));
//                return values;
//            }
//        } catch (Exception e) {
//            Log.e("MediaInfo", "获取文件信息失败", e);
//        }
//        return null;
//    }
//
//    private String generateUniqueFileName(String originalName) {
//        if (originalName == null) return System.currentTimeMillis() + ".jpg";
//
//        int dotIndex = originalName.lastIndexOf('.');
//        String name = (dotIndex != -1) ? originalName.substring(0, dotIndex) : originalName;
//        String ext = (dotIndex != -1) ? originalName.substring(dotIndex) : ".jpg";
//
//        return name + "_" + System.currentTimeMillis() + ext;
//    }


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

    public String saveBitmapToFile(Bitmap scaledBitmap, String image) {
        String fileName = System.currentTimeMillis() + ".jpg";
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(directory, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

