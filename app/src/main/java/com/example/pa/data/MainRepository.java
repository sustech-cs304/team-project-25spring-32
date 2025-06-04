package com.example.pa.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.Daos.*;
import com.example.pa.data.model.Photo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainRepository {
    private final UserDao userDao;
    private final AlbumDao albumDao;
    private final AlbumPhotoDao albumPhotoDao;
    private final PhotoDao photoDao;
    private final PhotoTagDao photoTagDao;
    private final TagDao tagDao;
    private final SearchHistoryDao searchHistoryDao;
    private final MemoryVideoDao memoryVideoDao;
    private final MemoryVideoPhotoDao memoryVideoPhotoDao;
    private final Context context;
    private final SQLiteDatabase db;

    public MainRepository(
            UserDao userDao,
            AlbumDao albumDao,
            AlbumPhotoDao albumPhotoDao,
            PhotoDao photoDao,
            PhotoTagDao photoTagDao,
            TagDao tagDao,
            SearchHistoryDao searchHistoryDao,
            MemoryVideoDao memoryVideoDao,
            MemoryVideoPhotoDao memoryVideoPhotoDao,
            Context context
    ) {
        this.userDao = userDao;
        this.albumDao = albumDao;
        this.albumPhotoDao = albumPhotoDao;
        this.photoDao = photoDao;
        this.photoTagDao = photoTagDao;
        this.tagDao = tagDao;
        this.searchHistoryDao = searchHistoryDao;
        this.memoryVideoDao = memoryVideoDao;
        this.memoryVideoPhotoDao = memoryVideoPhotoDao;
        this.context = context;
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }


    public List<String> getPhotoPathByTagName(String tagName) {
        List<String> photoPaths = new ArrayList<>();
        List<Integer> ids = tagDao.getTagIdByName(tagName);//先获取tag的id
        for (int id:ids){
            if (id != -1) {
                List<Integer> photoIds = photoTagDao.getPhotoIdsByTag(id);//获取tag对应的所有照片id
                for (int photoId : photoIds) {
                    photoPaths.add(photoDao.getPhotoPathById(photoId));
                }
            }
        }
        if (photoPaths.isEmpty())return null;
        return photoPaths;
    }
//    public void cleanEmptyAlbums() {
//        String query = "DELETE FROM " + AlbumDao.TABLE_NAME +
//                " WHERE " + AlbumDao.COLUMN_ID + " NOT IN (" +
//                "   SELECT " + AlbumPhotoDao.COLUMN_ALBUM_ID +
//                "   FROM " + AlbumPhotoDao.TABLE_NAME +
//                ")";
//        db.execSQL(query);
//    }
    public void cleanEmptyAlbums() {
        // 添加调试日志
        Log.d("AlbumCleaner", "cleanEmptyAlbums() called"); // 或者 System.out.println("Method called");

        // 打印要执行的 SQL 语句（调试用）
        String query = "DELETE FROM " + AlbumDao.TABLE_NAME +
                " WHERE " + AlbumDao.COLUMN_ID + " NOT IN (" +
                "   SELECT " + AlbumPhotoDao.COLUMN_ALBUM_ID +
                "   FROM " + AlbumPhotoDao.TABLE_NAME +
                ")";

        Log.d("AlbumCleaner", "Executing query: " + query); // 或者 System.out.println(query);

        try {
            // 执行删除操作
            db.execSQL(query);
            Log.d("AlbumCleaner", "Delete completed"); // 或者 System.out.println("Delete succeeded");
        } catch (SQLException e) {
            Log.e("AlbumCleaner", "Delete failed", e); // 打印错误日志
        }
    }

    public void syncInsertPhoto(Photo photo, int userId, Map<String, Integer> albumCache, int tagId) {
        try {
            db.beginTransaction();

            // 1. 插入 Photo
            int id = (int) photoDao.addFullPhoto(photo);

            String albumName = photo.extractAlbumName(context);

            // 2. 插入 Album 和关联
            if (albumName != null) {
                int albumId = albumDao.getOrCreateAlbum(albumName, userId, albumCache, false);
                albumPhotoDao.addPhotoToAlbum(albumId, id);
            }

            // 3. 插入 Tag
            photoTagDao.addTagToPhoto(id, tagId);

            // 插入地点相册
            if (photo.location != null) {
                int albumId = albumDao.getOrCreateAlbum(photo.location, userId, albumCache, true);
                albumPhotoDao.addPhotoToAlbum(albumId, id);
            }

            // 插入时间相册
            if (photo.uploadedTime != null) {
                String month = photo.uploadedTime.substring(0, 7);
                int albumId = albumDao.getOrCreateAlbum(month, userId, albumCache, true);
                albumPhotoDao.addPhotoToAlbum(albumId, id);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean deletePhoto(int photoId) {
        return albumPhotoDao.removePhotoFromAlbumByPhoto(photoId)
                && photoTagDao.removeTagFromPhotoByPhoto(photoId)
                && photoDao.deletePhoto(photoId);
    }

    public void deletePhotosByUri(List<String> photoUris) {
        for (String uri: photoUris) {
            int photoId = photoDao.getPhotoIdByPath(uri);
            deletePhoto(photoId);
        }
    }

    public boolean deleteAlbum(int albumId) {
        List<Integer> photoIds = albumPhotoDao.getPhotoIdsInAlbum(albumId);
        for (int photoId : photoIds) {
            boolean result = deletePhoto(photoId);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    public String getAlbumNameOfPhoto(String photoUri) {
        int photoId = photoDao.getPhotoIdByPath(photoUri);
        Log.d("getAlbumNameOfPhoto", "photoId: " + photoId);
        int albumId = albumPhotoDao.getAlbumOfPhoto(photoId);
        Log.d("getAlbumNameOfPhoto", "albumId: " + albumId);
        return albumDao.getAlbumNameById(albumId);
    }

    public void copyPhotosToAlbum(List<String> photoUris, String albumName) {
        int albumId = albumDao.getAlbumIdByName(albumName);
        if (albumId != -1) {
            for (String uri: photoUris) {
                int photoId = photoDao.getPhotoIdByPath(uri);
                if (photoId != -1) {
                    Photo photo = photoDao.getPhotoById(photoId);
                    int newPhotoId = (int) photoDao.addFullPhoto(photo);
                    albumPhotoDao.addPhotoToAlbum(albumId, newPhotoId);
                    List<Integer> tagIds = photoTagDao.getTagsForPhoto(photoId);
                    for (int tagId: tagIds) {
                        photoTagDao.addTagToPhoto(newPhotoId, tagId);
                    }
                }
            }
        }
    }

    public void movePhotosToAlbum(List<String> photoUris, String albumName) {
        copyPhotosToAlbum(photoUris, albumName);
        deletePhotosByUri(photoUris);
    }

    @SuppressLint("Range")
    public String getLatestPhotoPath(String albumName) {
        String photoPath = null;
        String query = "SELECT p." + PhotoDao.COLUMN_FILE_PATH +
                " FROM " + AlbumDao.TABLE_NAME + " a" +
                " INNER JOIN " + AlbumPhotoDao.TABLE_NAME + " ap ON a." + AlbumDao.COLUMN_ID + " = ap." + AlbumPhotoDao.COLUMN_ALBUM_ID +
                " INNER JOIN " + PhotoDao.TABLE_NAME + " p ON ap." + AlbumPhotoDao.COLUMN_PHOTO_ID + " = p." + PhotoDao.COLUMN_ID +
                " WHERE a." + AlbumDao.COLUMN_NAME + " = ?" +
                " ORDER BY p." + PhotoDao.COLUMN_UPLOADED_TIME + " DESC" +
                " LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{albumName});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                photoPath = cursor.getString(cursor.getColumnIndex(PhotoDao.COLUMN_FILE_PATH));
            }
            cursor.close();
        }
        return photoPath;
    }

    public List<String> getPhotoUrisByAlbumName(String albumName) {
        List<String> photoUris = new ArrayList<>();
        int albumId = albumDao.getAlbumIdByName(albumName);
        List<Integer> photoIds = albumPhotoDao.getPhotoIdsInAlbum(albumId);
        for (int photoId: photoIds) {
            photoUris.add(photoDao.getPhotoPathById(photoId));
        }
        return photoUris;
    }

}
