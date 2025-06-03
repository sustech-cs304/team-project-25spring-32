package com.example.pa.data;

import static com.example.pa.util.UriToPathHelper.getRealPathFromUri;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.system.Os;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.pa.MyApplication;
import com.example.pa.data.model.Photo;
import com.example.pa.util.ai.ImageClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final MyApplication myApplication;
    private List<Uri> pendingDeleteUris;
    private final Handler handler = new Handler(Looper.getMainLooper());
    // 新增同步状态标记
    private volatile boolean isIncrementalSyncing = false;
    private final ReentrantLock syncLock = new ReentrantLock(true); // 使用公平锁
    private ContentObserver mediaObserver;
    private Map<String, File> albumDirCache = new HashMap<>();

    // 新增同步时间记录
    private static final String SYNC_PREFS = "sync_prefs";
    private static final String KEY_LAST_SYNC = "last_sync_time";
    private long lastTriggerTime = 0;
    private ImageClassifier classifier;


    public FileRepository(Context context) {
        this.context = context;
        this.myApplication = (MyApplication) context.getApplicationContext();
    }

    // 触发增量同步（带防抖）
    public void triggerIncrementalSync() {
        new Thread(() -> {
            if (!syncLock.tryLock()) {
                Log.d("Sync", "同步已在进行中，跳过");
                return;
            }

            try {
                // 防抖检查
                long now = System.currentTimeMillis();
                if (now - lastTriggerTime < 1000) {
                    return;
                }
                lastTriggerTime = now;

                performIncrementalSync();
            } finally {
                syncLock.unlock();
                Log.d("Sync", "锁已释放");
            }
        }).start();
    }

    private void performIncrementalSync() {
        // 获取当前用户ID（根据实际登录状态获取）
//        int currentUserId = getCurrentUserId();
        Log.d("Sync", "同步开始，锁状态: " + syncLock.isLocked());
        try {
            int currentUserId = 1;

            long lastSyncTime = getLastSyncTime();
            long currentSyncTime = System.currentTimeMillis();

            // 查询变更时加入用户过滤
            List<Photo> changedPhotos = queryChangedPhotos(lastSyncTime, currentUserId);
            List<String> deletedUris = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deletedUris = queryDeletedPhotos(currentUserId);
            }

            updateLocalDatabase(changedPhotos, deletedUris, currentUserId);
            saveLastSyncTime(currentSyncTime);
        } finally {
            Log.d("Sync", "同步结束，锁状态: " + syncLock.isLocked());
        }

    }

    // 带用户过滤的查询
    private List<Photo> queryChangedPhotos(long sinceTime, int userId) {
        List<Photo> photos = new ArrayList<>();
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.LATITUDE
        };
        String selection = MediaStore.Images.Media.DATE_MODIFIED + " > ? AND " +
                MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] args = {
                String.valueOf(sinceTime / 1000),
                Environment.DIRECTORY_DCIM + "/%/" // 仅同步 DCIM 子目录
        };

        // 查询并转换 Photo 对象时设置 userId
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                args,
                null)) {

            while (cursor != null && cursor.moveToNext()) {
                long id = cursor.getLong(0);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                Photo photo = new Photo(
                        (int)id,
                        userId,
                        "photo",
                        uri.toString(),
                        null, null, null,
                        cursor.getDouble(4),
                        cursor.getDouble(5),
                        null, null, null
                );
                Log.d("Sync", "addPhotos: " + id);
                photos.add(photo);
            }
        }
        return photos;
    }

    // 查询被删除的图片
    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> queryDeletedPhotos(int currentUserId) {
        Log.d("FileReposi", "queryDeletedPhotos: ");
        // 获取数据库中的所有 URI
        final int PAGE_SIZE = 500; // 每页500条
        int page = 0;
        Set<String> dbUris = new HashSet<>();

        // 分页加载数据库URI
        do {
            List<String> pageUris = myApplication
                    .getPhotoDao().getPhotoPathByUser(currentUserId, page, PAGE_SIZE);
            if (pageUris.isEmpty()) break;

            dbUris.addAll(pageUris);
            page++;
        } while (true);

        // 分页加载MediaStore URI
        Set<String> mediaStoreUris = new HashSet<>(dbUris.size());
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] args = {Environment.DIRECTORY_DCIM + "/%"};
        int offset = 0;
        String[] projection = {MediaStore.Images.Media._ID};

        do {
            Bundle queryArgs = new Bundle();
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, args);

            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    MediaStore.Images.Media._ID + " ASC");
            queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, PAGE_SIZE);
            queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, offset);
            try (Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    queryArgs, // 使用 Bundle 传递参数
                    null)) {

                if (cursor == null || cursor.getCount() == 0) break;

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    Uri uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    mediaStoreUris.add(uri.toString());
                }
                offset += PAGE_SIZE;
            }
        } while (true);

        // 计算差异
        dbUris.removeAll(mediaStoreUris);
        return new ArrayList<>(dbUris);
    }

    // 更新数据库
    private void updateLocalDatabase(List<Photo> changedPhotos, List<String> deletedUris, int userId) {

        // 处理照片新增/修改
        Map<String, Integer> albumCache = new HashMap<>(); // 相册名 -> albumId
        for (Photo photo : changedPhotos) {
//            // 1. 插入或更新 Photo 表
//            myApplication.getPhotoDao().addFullPhoto(photo);
//
//            // 2. 处理相册关联
//            String albumName = photo.extractAlbumName(context);
//            Log.d("sync", "AlbumName: " + albumName);
//            if (albumName != null) {
//                int albumId = myApplication.getAlbumDao().getOrCreateAlbum(albumName, userId, albumCache);
//                myApplication.getAlbumPhotoDao().addPhotoToAlbum(albumId, photo.id);
//            }
            String tagName = null;
            try {
                // 初始化分类器
                classifier = new ImageClassifier(context);

                // 直接加载固定路径图片并分类
                tagName=classifyImage(Uri.parse(photo.filePath));
            } catch (IOException e) {
                Log.e("ImageClassifier", "初始化失败", e);
            }
            int tagId = myApplication.getTagDao().getTagIdByNameSpec(tagName);
            myApplication.getMainRepository().syncInsertPhoto(photo, userId, albumCache, tagId);
        }

        // 处理照片删除
        for (String uri : deletedUris) {
            // 1. 获取 PhotoId
            int photoId = myApplication.getPhotoDao().getPhotoIdByPath(uri);
            if (photoId >= 0) {
                myApplication.getAlbumPhotoDao().removePhotoFromAlbumByPhoto(photoId);
                myApplication.getPhotoTagDao().removeTagFromPhotoByPhoto(photoId);
                myApplication.getPhotoDao().deletePhoto(photoId);
            }
        }
        // 清理空相册
        myApplication.getMainRepository().cleanEmptyAlbums();
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

    public void deletePhotos(List<Uri> uris, DeleteRequestProvider provider) {
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
    public boolean copyPhotos(List<Uri> sourceUris, String targetAlbumName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return copyPhotosWithMediaStore(sourceUris, targetAlbumName);
        } else {
            // Android 9及以下实现（使用传统文件操作）
            // 注意需要处理运行时权限
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean copyPhotosWithMediaStore(List<Uri> sourceUris, String targetAlbumName) {
        ContentResolver resolver = context.getContentResolver();
        boolean allSuccess = true;

        for (Uri sourceUri : sourceUris) {
            try {
                // 1. 获取源文件信息
                ContentValues sourceValues = getMediaInfo(sourceUri);
                if (sourceValues == null) {
                    allSuccess = false;
                    continue;
                }

                // 2. 创建目标相册（如果不存在）
                if (!createAlbum(targetAlbumName)) {
                    Log.e("Copy", "目标相册创建失败");
                    allSuccess = false;
                    continue;
                }

                // 3. 创建目标文件元数据
                ContentValues targetValues = new ContentValues();
                targetValues.put(MediaStore.Images.Media.DISPLAY_NAME,
                        generateUniqueFileName(sourceValues.getAsString(MediaStore.Images.Media.DISPLAY_NAME)));
                targetValues.put(MediaStore.Images.Media.MIME_TYPE,
                        sourceValues.getAsString(MediaStore.Images.Media.MIME_TYPE));
                targetValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_DCIM + "/" + targetAlbumName);

                // 4. 插入目标文件
                Uri targetUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, targetValues);
                if (targetUri == null) {
                    allSuccess = false;
                    continue;
                }

                // 5. 执行文件拷贝
                try (InputStream in = resolver.openInputStream(sourceUri);
                     OutputStream out = resolver.openOutputStream(targetUri)) {
                    if (in == null || out == null) {
                        allSuccess = false;
                        continue;
                    }

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                // 6. 更新媒体库
                triggerMediaScanForAlbum(targetAlbumName, new MediaScanCallback() {
                    @Override public void onScanCompleted(Uri uri) {}
                    @Override public void onScanFailed(String error) {}
                });

            } catch (Exception e) {
                Log.e("Copy", "复制失败: " + sourceUri, e);
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    //=== 移动照片 ===//
    public boolean movePhotos(List<Uri> sourceUris, String targetAlbumName, DeleteRequestProvider provider) {
        boolean copySuccess = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            copySuccess = copyPhotosWithMediaStore(sourceUris, targetAlbumName);
        }
        if (!copySuccess) {
            Log.e("Move", "复制失败，终止移动操作");
            return false;
        }

        deletePhotos(sourceUris, provider);
        return true;
    }

    private File getAlbumDir(String albumName) {
        if (albumDirCache.containsKey(albumName)) {
            return albumDirCache.get(albumName);
        }

        File albumDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                albumName
        );

        if (!albumDir.exists()) {
            if (!albumDir.mkdirs()) return null;
        }

        albumDirCache.put(albumName, albumDir);
        return albumDir;
    }

    // 核心重命名方法
    private boolean renameFile(File source, File dest) {
        try {
            // 使用 Linux 级别的重命名（最快最有效）
            Os.rename(source.getAbsolutePath(), dest.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e("FileMove", "重命名失败", e);
            return false;
        }
    }

    // 更新媒体库路径
    private void updateMediaStorePath(Uri originalUri, File newFile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, newFile.getAbsolutePath());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, newFile.getName());

        context.getContentResolver().update(
                originalUri,
                values,
                null,
                null
        );
    }
    //=== 辅助方法 ===//
    private ContentValues getMediaInfo(Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.RELATIVE_PATH
        };

        try (Cursor cursor = resolver.query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                values.put(MediaStore.Images.Media.MIME_TYPE,
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)));
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)));
                return values;
            }
        } catch (Exception e) {
            Log.e("MediaInfo", "获取文件信息失败", e);
        }
        return null;
    }

    private String generateUniqueFileName(String originalName) {
        if (originalName == null) return System.currentTimeMillis() + ".jpg";

        int dotIndex = originalName.lastIndexOf('.');
        String name = (dotIndex != -1) ? originalName.substring(0, dotIndex) : originalName;
        String ext = (dotIndex != -1) ? originalName.substring(dotIndex) : ".jpg";

        return name + "_" + System.currentTimeMillis() + ext;
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
    private String classifyImage(Uri imageUri) {
        String TAG = "ImageClassifier";
        //assert fileRepository!=null;
        String result="null";

        try {
            // 1. 从URI加载原始图片（确保使用ARGB_8888配置）
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888; // 关键设置

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) inputStream.close();

            if (originalBitmap == null) {
                throw new IOException("Failed to decode bitmap");
            }

            // 2. 转换为模型需要的尺寸（保持ARGB_8888格式）
            int modelInputSize = 224; // MobileNet通常需要224x224
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    modelInputSize,
                    modelInputSize,
                    true
            );

            // 3. 确保Alpha通道存在（虽然模型只用RGB，但TensorImage要求ARGB格式）
            if (scaledBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                Bitmap argbBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, false);
                scaledBitmap.recycle(); // 回收临时bitmap
                scaledBitmap = argbBitmap;
            }
            // 确保 scaledBitmap 是 RGB_565 或使用 copy 去掉 alpha
            //保存bitmap为图片并存储
            //String path = fileRepository.saveBitmapToFile(scaledBitmap, "test_image.jpg");
            //Log.d(TAG, "保存图片路径: " + path);



            // 4. 进行分类
            Log.d("scan",scaledBitmap.toString());
            result = classifier.classify(scaledBitmap);


            // 5. 输出结果
            Log.d(TAG, "分类结果: " + result);

            // 6. 更新UI（显示原始图片）

            // 7. 回收不再需要的bitmap
            scaledBitmap.recycle();

        } catch (Exception e) {
            Log.e(TAG, "分类出错", e);
        }

        return result;
    }
}

