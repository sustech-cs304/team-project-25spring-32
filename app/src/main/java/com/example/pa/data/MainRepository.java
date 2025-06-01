package com.example.pa.data;

import android.content.Context;
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
                int albumId = albumDao.getOrCreateAlbum(albumName, userId, albumCache);
                albumPhotoDao.addPhotoToAlbum(albumId, id);
            }

            // 3. 插入 Tag
            photoTagDao.addTagToPhoto(id, tagId);

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

}
