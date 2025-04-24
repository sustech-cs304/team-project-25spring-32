package com.example.pa.data;

import com.example.pa.data.Daos.*;

import java.util.ArrayList;
import java.util.List;

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

    public MainRepository(
            UserDao userDao,
            AlbumDao albumDao,
            AlbumPhotoDao albumPhotoDao,
            PhotoDao photoDao,
            PhotoTagDao photoTagDao,
            TagDao tagDao,
            SearchHistoryDao searchHistoryDao,
            MemoryVideoDao memoryVideoDao,
            MemoryVideoPhotoDao memoryVideoPhotoDao
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

    // 选择最新的照片作为封面
//    public String getLatestPhotoPathByAlbumName(String albumName) {
//        int id =
//    }

}
