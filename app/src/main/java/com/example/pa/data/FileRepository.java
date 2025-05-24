package com.example.pa.data;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.ContentObserver;
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

import com.example.pa.data.Daos.PhotoDao.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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
    // 新增同步状态标记
    private volatile boolean isIncrementalSyncing = false;
    private final ReentrantLock syncLock = new ReentrantLock();
    private ContentObserver mediaObserver;

    // 新增同步时间记录
    private static final String SYNC_PREFS = "sync_prefs";
    private static final String KEY_LAST_SYNC = "last_sync_time";


    public FileRepository(Context context) {
        this.context = context;
    }

    // 注册 MediaStore 观察者
    public void registerMediaStoreObserver() {
        if (mediaObserver != null) return;

        mediaObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (!selfChange) { // 排除自身操作触发的通知
                    triggerIncrementalSync();
                }
            }
        };

        context.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                mediaObserver
        );
    }

    // 触发增量同步（带防抖）
    private void triggerIncrementalSync() {
        if (!syncLock.tryLock()) return;

        new Thread(() -> {
            try {
                // 防抖：1秒内多次触发只执行一次
                Thread.sleep(1000);
                performIncrementalSync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                syncLock.unlock();
            }
        }).start();
    }

    private void performIncrementalSync() {
        // 获取当前用户ID（根据实际登录状态获取）
        int currentUserId = getCurrentUserId();

        long lastSyncTime = getLastSyncTime();
        long currentSyncTime = System.currentTimeMillis();

        // 查询变更时加入用户过滤
        List<Photo> changedPhotos = queryChangedPhotos(lastSyncTime, currentUserId);
        List<String> deletedUris = queryDeletedPhotos(currentUserId);

        updateLocalDatabase(changedPhotos, deletedUris);
        saveLastSyncTime(currentSyncTime);
    }

    // 带用户过滤的查询
    private List<Photo> queryChangedPhotos(long sinceTime, int userId) {
        String selection = MediaStore.Images.Media.DATE_MODIFIED + " > ? AND " +
                MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] args = {
                String.valueOf(sinceTime / 1000),
                Environment.DIRECTORY_DCIM + "/%/" // 仅同步 DCIM 子目录
        };

        // 查询并转换 Photo 对象时设置 userId
        Photo photo = new Photo();
        photo.setUserId(userId);
        // ...
    }
//    private void performIncrementalSync() {
//        // 获取上次同步时间戳
//        long lastSyncTime = getLastSyncTime();
//        long currentSyncTime = System.currentTimeMillis();
//
//        // 查询 MediaStore 变更
//        List<Photo> changedPhotos = queryChangedPhotos(lastSyncTime);
//        List<String> deletedUris = queryDeletedPhotos();
//
//        // 更新数据库
//        updateLocalDatabase(changedPhotos, deletedUris);
//
//        // 记录新同步时间
//        saveLastSyncTime(currentSyncTime);
//    }
//
//    // 查询变更的图片（新增/修改）
//    private List<Photo> queryChangedPhotos(long sinceTime) {
//        List<Photo> photos = new ArrayList<>();
//        String[] projection = {
//                MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DISPLAY_NAME,
//                MediaStore.Images.Media.RELATIVE_PATH,
//                MediaStore.Images.Media.DATE_MODIFIED
//        };
//
//        String selection = MediaStore.Images.Media.DATE_MODIFIED + " > ?";
//        String[] args = {String.valueOf(sinceTime / 1000)}; // MediaStore 使用秒级时间戳
//
//        try (Cursor cursor = context.getContentResolver().query(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                projection,
//                selection,
//                args,
//                null)) {
//
//            while (cursor != null && cursor.moveToNext()) {
//                long id = cursor.getLong(0);
//                Uri uri = ContentUris.withAppendedId(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                Photo photo = new Photo(
//                        uri.toString(),
//                        cursor.getString(1),
//                        cursor.getString(2),
//                        cursor.getLong(3) * 1000 // 转换为毫秒
//                );
//                photos.add(photo);
//            }
//        }
//        return photos;
//    }

    // 查询被删除的图片
    private List<String> queryDeletedPhotos() {
        // 获取数据库中的所有 URI
        List<String> dbUris = DatabaseHelper.getInstance(context)
                .getPhotoDao().getAllPhotoUris();

        // 获取 MediaStore 中的所有 URI
        Set<String> mediaStoreUris = new HashSet<>();
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                null, null, null)) {

            while (cursor != null && cursor.moveToNext()) {
                long id = cursor.getLong(0);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                mediaStoreUris.add(uri.toString());
            }
        }

        // 计算差异
        dbUris.removeAll(mediaStoreUris);
        return dbUris;
    }

    // 更新数据库
    private void updateLocalDatabase(List<Photo> changedPhotos, List<String> deletedUris) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            // 处理照片新增/修改
            Map<String, Integer> albumCache = new HashMap<>(); // 相册名 -> albumId
            for (Photo photo : changedPhotos) {
                // 1. 插入或更新 Photo 表
                ContentValues photoValues = photoToContentValues(photo);
                long photoId = db.insertWithOnConflict(PhotoDao.TABLE_NAME, null, photoValues, SQLiteDatabase.CONFLICT_REPLACE);

                // 2. 处理相册关联
                String albumName = photo.extractAlbumName();
                if (albumName != null) {
                    int albumId = getOrCreateAlbum(db, albumName, photo.getUserId(), albumCache);
                    linkPhotoToAlbum(db, (int) photoId, albumId);
                }
            }

            // 处理照片删除
            for (String uri : deletedUris) {
                // 1. 获取 PhotoId
                Cursor cursor = db.query(PhotoDao.TABLE_NAME,
                        new String[]{PhotoDao.COLUMN_ID},
                        PhotoDao.COLUMN_FILE_PATH + " LIKE ?",
                        new String[]{"%" + uri}, null, null, null);
                if (cursor.moveToFirst()) {
                    int photoId = cursor.getInt(0);
                    // 2. 删除 AlbumPhoto 关联
                    db.delete(AlbumPhotoDao.TABLE_NAME,
                            AlbumPhotoDao.COLUMN_PHOTO_ID + " = ?",
                            new String[]{String.valueOf(photoId)});
                }
                cursor.close();
            }

            // 清理空相册
            cleanEmptyAlbums(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // 获取或创建相册
    private int getOrCreateAlbum(SQLiteDatabase db, String albumName, int userId, Map<String, Integer> cache) {
        if (cache.containsKey(albumName)) {
            return cache.get(albumName);
        }

        Cursor cursor = db.query(AlbumDao.TABLE_NAME,
                new String[]{AlbumDao.COLUMN_ID},
                AlbumDao.COLUMN_NAME + " = ? AND " + AlbumDao.COLUMN_USER_ID + " = ?",
                new String[]{albumName, String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int albumId = cursor.getInt(0);
            cache.put(albumName, albumId);
            return albumId;
        } else {
            ContentValues albumValues = new ContentValues();
            albumValues.put(AlbumDao.COLUMN_NAME, albumName);
            albumValues.put(AlbumDao.COLUMN_USER_ID, userId);
            albumValues.put(AlbumDao.COLUMN_IS_AUTO_GENERATED, 1); // 标记为自动生成
            long albumId = db.insert(AlbumDao.TABLE_NAME, null, albumValues);
            cache.put(albumName, (int) albumId);
            return (int) albumId;
        }
    }

    // 关联照片与相册
    private void linkPhotoToAlbum(SQLiteDatabase db, int photoId, int albumId) {
        ContentValues values = new ContentValues();
        values.put(AlbumPhotoDao.COLUMN_ALBUM_ID, albumId);
        values.put(AlbumPhotoDao.COLUMN_PHOTO_ID, photoId);
        db.insertWithOnConflict(AlbumPhotoDao.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // 清理空相册
    private void cleanEmptyAlbums(SQLiteDatabase db) {
        String query = "DELETE FROM " + AlbumDao.TABLE_NAME +
                " WHERE " + AlbumDao.COLUMN_IS_AUTO_GENERATED + " = 1 " +
                " AND " + AlbumDao.COLUMN_ID + " NOT IN (" +
                "   SELECT " + AlbumPhotoDao.COLUMN_ALBUM_ID +
                "   FROM " + AlbumPhotoDao.TABLE_NAME +
                ")";
        db.execSQL(query);
    }

    private long getLastSyncTime() {
        SharedPreferences prefs = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }

    private void saveLastSyncTime(long time) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE).edit();
        editor.putLong(KEY_LAST_SYNC, time);
        editor.apply();
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

