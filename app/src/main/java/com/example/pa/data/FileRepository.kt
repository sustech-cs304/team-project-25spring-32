package com.example.pa.data

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.pa.MyApplication
import com.example.pa.data.model.Photo
import com.example.pa.util.ai.ImageClassifier
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock


/**
 * AI-generated-content
 * tool: DeepSeek
 * version: R1
 * usage: I asked how to create a local folder, and
 * directly copy the code from its response.
 */
class FileRepository(private val context: Context) {
    interface DeleteCallback {
        fun onComplete()
        fun onError(error: String?)
    }

    private var deleteCallback: DeleteCallback? = null
    private val myApplication = context.applicationContext as MyApplication
    private var pendingDeleteUris: List<Uri>? = null
    private val syncLock = ReentrantLock(true) // 使用公平锁
    private var albumDirCache: MutableMap<String, File> = HashMap()

    fun setAlbumDirCache(albumDirCache: MutableMap<String, File>) {
        this.albumDirCache = albumDirCache
    }

    fun getAlbumDirCache(): Map<String, File> {
        return albumDirCache
    }

    private var lastTriggerTime: Long = 0
    private var classifier: ImageClassifier? = null

    // 触发增量同步（带防抖）
    fun triggerIncrementalSync() {
        Thread(Runnable {
            if (!syncLock.tryLock()) {
                Log.d("Sync", "同步已在进行中，跳过")
                return@Runnable
            }
            try {
                // 防抖检查
                val now = System.currentTimeMillis()
                if (now - lastTriggerTime < 1000) {
                    return@Runnable
                }
                lastTriggerTime = now

                performIncrementalSync()
            } finally {
                syncLock.unlock()
                Log.d("Sync", "锁已释放")
            }
        }).start()
    }

    private fun performIncrementalSync() {
        // 获取当前用户ID（根据实际登录状态获取）
//        int currentUserId = getCurrentUserId();
        Log.d("Sync", "同步开始，锁状态: " + syncLock.isLocked)
        try {
            val currentUserId = 1

            val lastSyncTime = lastSyncTime
            val currentSyncTime = System.currentTimeMillis()

            // 查询变更时加入用户过滤
            val changedPhotos = queryChangedPhotos(lastSyncTime, currentUserId)
            var deletedUris: List<String?>? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deletedUris = queryDeletedPhotos(currentUserId)
            }

            updateLocalDatabase(changedPhotos, deletedUris!!, currentUserId)
            saveLastSyncTime(currentSyncTime)
        } finally {
            Log.d("Sync", "同步结束，锁状态: " + syncLock.isLocked)
        }
    }

    // 带用户过滤的查询
    private fun queryChangedPhotos(sinceTime: Long, userId: Int): List<Photo> {
        val photos: MutableList<Photo> = ArrayList()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.LATITUDE
        )
        val selection = MediaStore.Images.Media.DATE_MODIFIED + " > ? AND " +
                MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?"
        val args = arrayOf(
            (sinceTime / 1000).toString(),
            Environment.DIRECTORY_DCIM + "/%/" // 仅同步 DCIM 子目录
        )

        var photoCount = 0

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            args,
            null
        ).use { cursor ->
            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                // 获取实际经纬度
                var longitude = cursor.getDouble(4)
                var latitude = cursor.getDouble(5)
                var uploadedTime: String? = null
                var location: String? = null
                var description: String? = null
                MyApplication.getInstance().tagDao.addTag("2024", false)
                MyApplication.getInstance().tagDao.addTag("2025", false)
                MyApplication.getInstance().tagDao.addTag("January", false)
                MyApplication.getInstance().tagDao.addTag("February", false)

                MyApplication.getInstance().tagDao.addTag("Beijing", false)
                MyApplication.getInstance().tagDao.addTag("Shanghai", false)
                MyApplication.getInstance().tagDao.addTag("Guangzhou", false)
                MyApplication.getInstance().tagDao.addTag("Shenzhen", false)

                // 如果图片没有位置信息，分配测试位置
                if (longitude == 0.0 && latitude == 0.0) {
                    // 为不同的图片分配不同的测试位置
                    when (photoCount % 4) {
                        0 -> {
                            // 北京天安门
                            uploadedTime = "2024-01-01 12:00:00"
                            location = "Beijing"
                            latitude = 39.9087
                            longitude = 116.3975
                            description = "0"
                        }

                        1 -> {
                            // 上海东方明珠
                            uploadedTime = "2024-02-01 12:00:00"
                            location = "Shanghai"
                            latitude = 31.2397
                            longitude = 121.4998
                            description = "1"
                        }

                        2 -> {
                            // 广州塔
                            uploadedTime = "2025-01-01 12:00:00"
                            location = "Guangzhou"
                            latitude = 23.1144
                            longitude = 113.3248
                            description = "2"
                        }

                        3 -> {
                            // 深圳腾讯大厦
                            uploadedTime = "2025-02-01 12:00:00"
                            location = "Shenzhen"
                            latitude = 22.5407
                            longitude = 114.0543
                            description = "3"
                        }
                    }
                    photoCount++
                }

                val photo = Photo(
                    id.toInt(),
                    userId,
                    "photo",
                    uri.toString(),
                    null, uploadedTime, null,
                    cursor.getDouble(4),
                    cursor.getDouble(5),
                    location, description, null
                )
                Log.d("Sync", "addPhotos: $id")
                photos.add(photo)
            }
        }
        return photos
    }

    // 查询被删除的图片
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun queryDeletedPhotos(currentUserId: Int): List<String?> {
        Log.d("FileReposi", "queryDeletedPhotos: ")
        // 获取数据库中的所有 URI
        val PAGE_SIZE = 500 // 每页500条
        var page = 0
        val dbUris: MutableSet<String?> = HashSet()

        // 分页加载数据库URI
        do {
            val pageUris = myApplication
                .photoDao.getPhotoPathByUser(currentUserId, page, PAGE_SIZE)
            if (pageUris.isEmpty()) break

            dbUris.addAll(pageUris)
            page++
        } while (true)

        // 分页加载MediaStore URI
        val mediaStoreUris: MutableSet<String?> = HashSet(dbUris.size)
        val selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?"
        val args = arrayOf(Environment.DIRECTORY_DCIM + "/%")
        var offset = 0
        val projection = arrayOf(MediaStore.Images.Media._ID)

        do {
            val queryArgs = Bundle()
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, args)

            queryArgs.putString(
                ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                MediaStore.Images.Media._ID + " ASC"
            )
            queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, PAGE_SIZE)
            queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                queryArgs,  // 使用 Bundle 传递参数
                null
            ).use { cursor ->
                if (cursor == null || cursor.count == 0) return ArrayList(dbUris)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    mediaStoreUris.add(uri.toString())
                }
                offset += PAGE_SIZE
            }
        } while (true)

        // 计算差异
        dbUris.removeAll(mediaStoreUris)
        return ArrayList(dbUris)
    }

    // 更新数据库
    fun updateLocalDatabase(changedPhotos: List<Photo>, deletedUris: List<String?>, userId: Int) {
        // 处理照片新增/修改

        val albumCache: Map<String, Int> = HashMap() // 相册名 -> albumId
        for (photo in changedPhotos) {
            var tagName: String? = null
            try {
                // 初始化分类器
                classifier = ImageClassifier(context)

                // 直接加载固定路径图片并分类
                tagName = classifyImage(Uri.parse(photo.filePath))
            } catch (e: IOException) {
                Log.e("ImageClassifier", "初始化失败", e)
            }
            val tagId = myApplication.tagDao.getTagIdByNameSpec(tagName)
            myApplication.mainRepository.syncInsertPhoto(photo, userId, albumCache, tagId)
        }

        // 处理照片删除
        for (uri in deletedUris) {
            // 1. 获取 PhotoId
            val photoId = myApplication.photoDao.getPhotoIdByPath(uri)
            if (photoId >= 0) {
                myApplication.albumPhotoDao.removePhotoFromAlbumByPhoto(photoId)
                myApplication.photoTagDao.removeTagFromPhotoByPhoto(photoId)
                myApplication.photoDao.deletePhoto(photoId)
            }
        }
        // 清理空相册
        myApplication.mainRepository.cleanEmptyAlbums()
    }

    val lastSyncTime: Long
        get() {
            val prefs = context.getSharedPreferences(
                SYNC_PREFS,
                Context.MODE_PRIVATE
            )
            return prefs.getLong(KEY_LAST_SYNC, 0)
        }

    fun saveLastSyncTime(time: Long) {
        val editor = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE).edit()
        editor.putLong(KEY_LAST_SYNC, time)
        editor.apply()
    }

    fun setDeleteCallback(callback: DeleteCallback?) {
        this.deleteCallback = callback
    }

    fun createAlbum(albumName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createAlbumWithMediaStore(albumName)
        } else {
            // Android 9 及以下逻辑（按需保留）
            false
        }
    }

    interface DeleteRequestProvider {
        fun provideDeleteRequest(deleteIntent: PendingIntent?)
    }

    fun deletePhotos(uris: List<Uri>, provider: DeleteRequestProvider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val deleteIntent = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    uris
                )
                Log.d("FileRepository", "DeleteIntent created")
                this.pendingDeleteUris = uris
                provider.provideDeleteRequest(deleteIntent)
            } catch (e: Exception) {
                Log.e("Delete", "创建删除请求失败", e)
            }
        }
    }

    fun executePhysicalDelete(uris: List<Uri>?) {
        this.pendingDeleteUris = uris // 保存待删除的URI列表

        Thread {
            val success =
                AtomicBoolean(true)
            for (uri in pendingDeleteUris!!) {
                try {
                    val deleted = context.contentResolver.delete(uri, null, null)
                    if (deleted <= 0) {
                        Log.w("Delete", "删除失败: $uri")
                        success.set(false)
                    }
                } catch (e: SecurityException) {
                    Log.e("Delete", "权限不足: " + e.message)
                    success.set(false)
                }
            }

            // 通过Handler回到主线程
            Handler(Looper.getMainLooper()).post {
                if (deleteCallback != null) {
                    if (success.get()) {
                        deleteCallback!!.onComplete()
                    } else {
                        deleteCallback!!.onError("部分文件删除失败")
                    }
                }
                pendingDeleteUris = null // 清理临时数据
            }
        }.start()
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to create a local folder, and
     * directly copy the code from its response.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun createAlbumWithMediaStore(albumName: String): Boolean {
        val resolver = context.contentResolver
        val contentValues = ContentValues()
        // 通过插入一个空文件来隐式创建文件夹
        contentValues.put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "placeholder_" + System.currentTimeMillis() + ".jpg"
        )
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        contentValues.put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_DCIM + "/" + albumName
        )

        try {
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                // 删除占位文件（可选）
                resolver.delete(uri, null, null)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    //=== 复制照片 ===//
    fun copyPhotos(sourceUris: List<Uri>, targetAlbumName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            copyPhotosWithMediaStore(sourceUris, targetAlbumName)
        } else {
            // Android 9及以下实现（使用传统文件操作）
            // 注意需要处理运行时权限
            false
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun copyPhotosWithMediaStore(sourceUris: List<Uri>, targetAlbumName: String): Boolean {
        val resolver = context.contentResolver
        var allSuccess = true

        for (sourceUri in sourceUris) {
            try {
                // 1. 获取源文件信息
                val sourceValues = getMediaInfo(sourceUri)
                if (sourceValues == null) {
                    allSuccess = false
                    continue
                }

                // 2. 创建目标相册（如果不存在）
                if (!createAlbum(targetAlbumName)) {
                    Log.e("Copy", "目标相册创建失败")
                    allSuccess = false
                    continue
                }

                // 3. 创建目标文件元数据
                val targetValues = ContentValues()
                targetValues.put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    generateUniqueFileName(sourceValues.getAsString(MediaStore.Images.Media.DISPLAY_NAME))
                )
                targetValues.put(
                    MediaStore.Images.Media.MIME_TYPE,
                    sourceValues.getAsString(MediaStore.Images.Media.MIME_TYPE)
                )
                targetValues.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + "/" + targetAlbumName
                )

                // 4. 插入目标文件
                val targetUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, targetValues)
                if (targetUri == null) {
                    allSuccess = false
                    continue
                }

                resolver.openInputStream(sourceUri).use { `in` ->
                    resolver.openOutputStream(targetUri).use { out ->
                        if (`in` == null || out == null) {
                            allSuccess = false
                        } else {
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while ((`in`.read(buffer).also { bytesRead = it }) != -1) {
                                out.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }
                // 6. 更新媒体库
                triggerMediaScanForAlbum(targetAlbumName, object : MediaScanCallback {
                    override fun onScanCompleted(uri: Uri?) {}
                    override fun onScanFailed(error: String?) {}
                })
            } catch (e: Exception) {
                Log.e("Copy", "复制失败: $sourceUri", e)
                allSuccess = false
            }
        }

        return allSuccess
    }

    //=== 移动照片 ===//
    fun movePhotos(
        sourceUris: List<Uri>,
        targetAlbumName: String,
        provider: DeleteRequestProvider
    ): Boolean {
        var copySuccess = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            copySuccess = copyPhotosWithMediaStore(sourceUris, targetAlbumName)
        }
        if (!copySuccess) {
            Log.e("Move", "复制失败，终止移动操作")
            return false
        }

        deletePhotos(sourceUris, provider)
        return true
    }

    fun getAlbumDir(albumName: String): File? {
        if (albumDirCache.containsKey(albumName)) {
            return albumDirCache[albumName]
        }

        val albumDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            albumName
        )

        if (!albumDir.exists()) {
            if (!albumDir.mkdirs()) return null
        }

        albumDirCache[albumName] = albumDir
        return albumDir
    }

    //=== 辅助方法 ===//
    private fun getMediaInfo(uri: Uri): ContentValues? {
        val resolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        try {
            resolver.query(uri, projection, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val values = ContentValues()
                    values.put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                    )
                    values.put(
                        MediaStore.Images.Media.MIME_TYPE,
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
                    )
                    values.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
                    )
                    return values
                }
            }
        } catch (e: Exception) {
            Log.e("MediaInfo", "获取文件信息失败", e)
        }
        return null
    }

    fun generateUniqueFileName(originalName: String?): String {
        if (originalName == null) return System.currentTimeMillis().toString() + ".jpg"

        val dotIndex = originalName.lastIndexOf('.')
        val name = if (dotIndex != -1) originalName.substring(0, dotIndex) else originalName
        val ext = if (dotIndex != -1) originalName.substring(dotIndex) else ".jpg"

        return name + "_" + System.currentTimeMillis() + ext
    }


    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to get files from folder, and
     * directly copy the code from its response.
     */
    fun getAlbumImages(albumName: String): List<Uri> {
        val imageUris: MutableList<Uri> = ArrayList()
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        // 调整查询条件：使用 LIKE 和通配符
        val selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?"
        val selectionArgs = if (albumName == "All Photos") {
            arrayOf(Environment.DIRECTORY_DCIM + "/%")
        } else {
            arrayOf(Environment.DIRECTORY_DCIM + "/" + albumName + "/%")
        }
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"

        // 扩展查询字段
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )

        try {
            resolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            ).use { cursor ->
                if (cursor != null) {
                    Log.d("MediaQuery", "找到文件数量: " + cursor.count)
                    while (cursor.moveToNext()) {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        val uri = ContentUris.withAppendedId(collection, id)
                        imageUris.add(uri)

                        // 调试日志
                        val name =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                        val path =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
                        Log.d("MediaQuery", "文件: $name | 路径: $path")
                    }
                } else {
                    Log.e("MediaQuery", "查询返回空 Cursor")
                }
            }
        } catch (e: SecurityException) {
            Log.e("MediaQuery", "权限不足: " + e.message)
        } catch (e: Exception) {
            Log.e("MediaQuery", "查询失败: " + e.message)
        }

        return imageUris
    }

    // 获取封面（最新一张图片）
    fun getAlbumCover(albumName: String): Uri? {
        val images = getAlbumImages(albumName)
        Log.d("FileRepository", "getAlbumCover from $albumName : $images")
        return if (images.isEmpty()) null else images[0]
    }

    fun saveBitmapToFile(scaledBitmap: Bitmap, image: String?): String? {
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val file = File(directory, fileName)

        try {
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                return file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to solve the problem of asynchronous scan, and
     * directly copy the code from its response.
     */
    interface MediaScanCallback {
        fun onScanCompleted(uri: Uri?)
        fun onScanFailed(error: String?)
    }

    // 修改扫描方法，增加回调参数
    fun triggerMediaScanForAlbum(albumName: String, callback: MediaScanCallback) {
        val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val albumDir = File(dcimDir, albumName)

        MediaScannerConnection.scanFile(
            context,
            arrayOf(albumDir.absolutePath),
            arrayOf("image/*")
        ) { path: String, uri: Uri? ->
            if (uri != null) {
                Handler(Looper.getMainLooper()).post { callback.onScanCompleted(uri) }
            } else {
                Handler(Looper.getMainLooper()).post {
                    callback.onScanFailed(
                        "Scan failed for: $path"
                    )
                }
            }
        }
    }

    fun triggerMediaScanForDirectory(rootDir: File?, callback: MediaScanCallback) {
        val allDirs: MutableList<File> = ArrayList()
        collectAllDirectories(rootDir, allDirs)

        if (allDirs.isEmpty()) {
            callback.onScanFailed("没有可扫描的目录")
            return
        }

        val remaining = AtomicInteger(allDirs.size)
        val anyFailed = AtomicBoolean(false)

        for (dir in allDirs) {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(dir.absolutePath),
                arrayOf("image/*", "video/*")
            ) { path: String, uri: Uri? ->
                Log.d(
                    "MediaScan",
                    "Scanned: $path"
                )
                if (uri == null) {
                    anyFailed.set(true)
                }
                if (remaining.decrementAndGet() == 0) {
                    if (anyFailed.get()) {
                        callback.onScanFailed("部分或全部扫描失败")
                    } else {
                        callback.onScanCompleted(Uri.EMPTY) // 表示全部完成
                    }
                }
            }
        }
    }

    private fun collectAllDirectories(dir: File?, allDirs: MutableList<File>) {
        if (dir != null && dir.exists() && dir.isDirectory) {
            allDirs.add(dir)
            val subDirs = dir.listFiles(FileFilter { obj: File -> obj.isDirectory })
            if (subDirs != null) {
                for (sub in subDirs) {
                    collectAllDirectories(sub, allDirs)
                }
            }
        }
    }

    private fun classifyImage(imageUri: Uri): String {
        val TAG = "ImageClassifier"
        //assert fileRepository!=null;
        var result = "null"

        try {
            // 1. 从URI加载原始图片（确保使用ARGB_8888配置）
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888 // 关键设置

            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (originalBitmap == null) {
                throw IOException("Failed to decode bitmap")
            }

            // 2. 转换为模型需要的尺寸（保持ARGB_8888格式）
            val modelInputSize = 224 // MobileNet通常需要224x224
            var scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                modelInputSize,
                modelInputSize,
                true
            )

            // 3. 确保Alpha通道存在（虽然模型只用RGB，但TensorImage要求ARGB格式）
            if (scaledBitmap.config != Bitmap.Config.ARGB_8888) {
                val argbBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, false)
                scaledBitmap.recycle() // 回收临时bitmap
                scaledBitmap = argbBitmap
            }


            // 4. 进行分类
            Log.d("scan", scaledBitmap.toString())
            result = classifier!!.classify(scaledBitmap)


            // 5. 输出结果
            Log.d(TAG, "分类结果: $result")

            // 6. 更新UI（显示原始图片）

            // 7. 回收不再需要的bitmap
            scaledBitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "分类出错", e)
        }

        return result
    }

    companion object {
        const val DELETE_REQUEST_CODE: Int = 1002

        // 新增同步时间记录
        private const val SYNC_PREFS = "sync_prefs"
        private const val KEY_LAST_SYNC = "last_sync_time"
    }
}

