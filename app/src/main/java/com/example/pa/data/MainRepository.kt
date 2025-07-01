package com.example.pa.data

import android.annotation.SuppressLint
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.pa.data.Daos.AlbumDao
import com.example.pa.data.Daos.AlbumPhotoDao
import com.example.pa.data.Daos.MemoryVideoDao
import com.example.pa.data.Daos.MemoryVideoPhotoDao
import com.example.pa.data.Daos.PhotoDao
import com.example.pa.data.Daos.PhotoTagDao
import com.example.pa.data.Daos.SearchHistoryDao
import com.example.pa.data.Daos.TagDao
import com.example.pa.data.Daos.UserDao
import com.example.pa.data.DatabaseHelper.Companion.getInstance
import com.example.pa.data.model.Photo

class MainRepository(
    private val userDao: UserDao,
    private val albumDao: AlbumDao,
    private val albumPhotoDao: AlbumPhotoDao,
    private val photoDao: PhotoDao,
    private val photoTagDao: PhotoTagDao,
    private val tagDao: TagDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val memoryVideoDao: MemoryVideoDao,
    private val memoryVideoPhotoDao: MemoryVideoPhotoDao,
    private val context: Context
) {
    private val db: SQLiteDatabase = getInstance(context).writableDatabase


    fun getPhotoPathByTagName(tagName: String?): List<String>? {
        val photoPaths: MutableList<String> = ArrayList()
        val ids = tagDao.getTagIdByName(tagName) //先获取tag的id
        for (id in ids) {
            if (id != -1) {
                val photoIds = photoTagDao.getPhotoIdsByTag(id) //获取tag对应的All Photosid
                for (photoId in photoIds) {
                    photoPaths.add(photoDao.getPhotoPathById(photoId))
                }
            }
        }
        if (photoPaths.isEmpty()) return null
        return photoPaths
    }

    //    public void cleanEmptyAlbums() {
    //        String query = "DELETE FROM " + AlbumDao.TABLE_NAME +
    //                " WHERE " + AlbumDao.COLUMN_ID + " NOT IN (" +
    //                "   SELECT " + AlbumPhotoDao.COLUMN_ALBUM_ID +
    //                "   FROM " + AlbumPhotoDao.TABLE_NAME +
    //                ")";
    //        db.execSQL(query);
    //    }
    fun cleanEmptyAlbums() {
        // 添加调试日志
        Log.d(
            "AlbumCleaner",
            "cleanEmptyAlbums() called"
        ) // 或者 System.out.println("Method called");

        // 打印要执行的 SQL 语句（调试用）
        val query = "DELETE FROM " + AlbumDao.TABLE_NAME +
                " WHERE " + AlbumDao.COLUMN_ID + " NOT IN (" +
                "   SELECT " + AlbumPhotoDao.COLUMN_ALBUM_ID +
                "   FROM " + AlbumPhotoDao.TABLE_NAME +
                ")"

        Log.d("AlbumCleaner", "Executing query: $query") // 或者 System.out.println(query);

        try {
            // 执行删除操作
            db.execSQL(query)
            Log.d("AlbumCleaner", "Delete completed") // 或者 System.out.println("Delete succeeded");
        } catch (e: SQLException) {
            Log.e("AlbumCleaner", "Delete failed", e) // 打印错误日志
        }
    }

    fun syncInsertPhoto(photo: Photo, userId: Int, albumCache: Map<String?, Int?>, tagId: Int) {
        try {
            db.beginTransaction()

            // 1. 插入 Photo
            val id = photoDao.addFullPhoto(photo).toInt()

            val albumName = photo.extractAlbumName(context)

            // 2. 插入 Album 和关联
            if (albumName != null) {
                val albumId = albumDao.getOrCreateAlbum(albumName, userId, albumCache, false)
                albumPhotoDao.addPhotoToAlbum(albumId, id)
            }

            // 3. 插入 Tag
            photoTagDao.addTagToPhoto(id, tagId)

            //            int tag_id = tagDao.getTagIdByNameSpec("apple");
//
//            int tag_id = tagDao.getTagIdByNameSpec("mouse");
//            int tag_id = tagDao.getTagIdByNameSpec("sky");
//            int tag_id = tagDao.getTagIdByNameSpec("house");
//            int tag_id = tagDao.getTagIdByNameSpec("rose");
//            photoTagDao.addTagToPhoto(id, tag_id);

            // 插入地点相册
            if (photo.location != null) {
                val albumId = albumDao.getOrCreateAlbum(photo.location, userId, albumCache, true)
                albumPhotoDao.addPhotoToAlbum(albumId, id)
            }

            // 插入时间相册
            if (photo.uploadedTime != null) {
                val month = photo.uploadedTime.substring(0, 7)
                val albumId = albumDao.getOrCreateAlbum(month, userId, albumCache, true)
                albumPhotoDao.addPhotoToAlbum(albumId, id)
            }

            val photoTag0 = tagDao.getTagIdByNameSpec("2024")
            val photoTag1 = tagDao.getTagIdByNameSpec("2025")
            val photoTag2 = tagDao.getTagIdByNameSpec("January")
            val photoTag3 = tagDao.getTagIdByNameSpec("February")
            val photoTag4 = tagDao.getTagIdByNameSpec("Beijing")
            val photoTag5 = tagDao.getTagIdByNameSpec("Shanghai")
            val photoTag6 = tagDao.getTagIdByNameSpec("Guangzhou")
            val photoTag7 = tagDao.getTagIdByNameSpec("Shenzhen")

            //            String description = "2";
            when (photo.description) {
                "0" -> {
                    photoTagDao.addTagToPhoto(id, photoTag0)
                    photoTagDao.addTagToPhoto(id, photoTag2)
                    photoTagDao.addTagToPhoto(id, photoTag4)
                }

                "1" -> {
                    photoTagDao.addTagToPhoto(id, photoTag0)
                    photoTagDao.addTagToPhoto(id, photoTag3)
                    photoTagDao.addTagToPhoto(id, photoTag5)
                }

                "2" -> {
                    photoTagDao.addTagToPhoto(id, photoTag1)
                    photoTagDao.addTagToPhoto(id, photoTag2)
                    photoTagDao.addTagToPhoto(id, photoTag6)
                }

                "3" -> {
                    photoTagDao.addTagToPhoto(id, photoTag1)
                    photoTagDao.addTagToPhoto(id, photoTag3)
                    photoTagDao.addTagToPhoto(id, photoTag7)
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deletePhoto(photoId: Int): Boolean {
        return albumPhotoDao.removePhotoFromAlbumByPhoto(photoId)
                && photoTagDao.removeTagFromPhotoByPhoto(photoId)
                && photoDao.deletePhoto(photoId)
    }

    fun deletePhotosByUri(photoUris: List<String?>) {
        for (uri in photoUris) {
            val photoId = photoDao.getPhotoIdByPath(uri)
            deletePhoto(photoId)
        }
    }

    fun deleteAlbum(albumId: Int): Boolean {
        val photoIds = albumPhotoDao.getPhotoIdsInAlbum(albumId)
        for (photoId in photoIds) {
            val result = deletePhoto(photoId)
            if (!result) {
                return false
            }
        }
        return true
    }

    fun getAlbumNameOfPhoto(photoUri: String?): String {
        val photoId = photoDao.getPhotoIdByPath(photoUri)
        Log.d("getAlbumNameOfPhoto", "photoId: $photoId")
        val albumId = albumPhotoDao.getAlbumOfPhoto(photoId)
        Log.d("getAlbumNameOfPhoto", "albumId: $albumId")
        return albumDao.getAlbumNameById(albumId)
    }

    fun copyPhotosToAlbum(photoUris: List<String?>, albumName: String?) {
        val albumId = albumDao.getAlbumIdByName(albumName)
        if (albumId != -1) {
            for (uri in photoUris) {
                val photoId = photoDao.getPhotoIdByPath(uri)
                if (photoId != -1) {
                    val photo = photoDao.getPhotoById(photoId)
                    val newPhotoId = photoDao.addFullPhoto(photo).toInt()
                    albumPhotoDao.addPhotoToAlbum(albumId, newPhotoId)
                    val tagIds = photoTagDao.getTagsForPhoto(photoId)
                    for (tagId in tagIds) {
                        photoTagDao.addTagToPhoto(newPhotoId, tagId)
                    }
                }
            }
        }
    }

    fun movePhotosToAlbum(photoUris: List<String?>, albumName: String?) {
        copyPhotosToAlbum(photoUris, albumName)
        deletePhotosByUri(photoUris)
    }

    @SuppressLint("Range")
    fun getLatestPhotoPath(albumName: String): String? {
        var photoPath: String? = null
        val query = "SELECT p." + PhotoDao.COLUMN_FILE_PATH +
                " FROM " + AlbumDao.TABLE_NAME + " a" +
                " INNER JOIN " + AlbumPhotoDao.TABLE_NAME + " ap ON a." + AlbumDao.COLUMN_ID + " = ap." + AlbumPhotoDao.COLUMN_ALBUM_ID +
                " INNER JOIN " + PhotoDao.TABLE_NAME + " p ON ap." + AlbumPhotoDao.COLUMN_PHOTO_ID + " = p." + PhotoDao.COLUMN_ID +
                " WHERE a." + AlbumDao.COLUMN_NAME + " = ?" +
                " ORDER BY p." + PhotoDao.COLUMN_UPLOADED_TIME + " DESC" +
                " LIMIT 1"

        val cursor = db.rawQuery(query, arrayOf(albumName))
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                photoPath = cursor.getString(cursor.getColumnIndex(PhotoDao.COLUMN_FILE_PATH))
            }
            cursor.close()
        }
        return photoPath
    }

    fun getPhotoUrisByAlbumName(albumName: String?): List<String> {
        val photoUris: MutableList<String> = ArrayList()
        val albumId = albumDao.getAlbumIdByName(albumName)
        val photoIds = albumPhotoDao.getPhotoIdsInAlbum(albumId)
        for (photoId in photoIds) {
            photoUris.add(photoDao.getPhotoPathById(photoId))
        }
        return photoUris
    }
}
