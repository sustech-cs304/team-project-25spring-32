package com.example.pa;

import android.app.Application;
import android.util.Log;
import android.content.Context;

import com.example.pa.data.Daos.AlbumDao;
import com.example.pa.data.Daos.AlbumPhotoDao;
import com.example.pa.data.Daos.MemoryVideoDao;
import com.example.pa.data.Daos.MemoryVideoPhotoDao;
import com.example.pa.data.Daos.PhotoDao;
import com.example.pa.data.Daos.PhotoTagDao;
import com.example.pa.data.Daos.SearchHistoryDao;
import com.example.pa.data.Daos.TagDao;
import com.example.pa.data.Daos.UserDao;
import com.example.pa.data.DatabaseHelper;
import com.example.pa.data.MainRepository;

public class MyApplication extends Application {
    private static MyApplication instance;
    private DatabaseHelper databaseHelper;//这里是数据库帮助类的实例，有个警告，
    // 但是getInstance方法之后是直接执行了onCreate
    private MainRepository mainRepository;
    private UserDao userDao;
    private PhotoDao photoDao;
    private AlbumDao albumDao;
    private AlbumPhotoDao albumPhotoDao;
    private TagDao tagDao;
    private PhotoTagDao photoTagDao;
    private SearchHistoryDao searchHistoryDao;
    private MemoryVideoDao memoryVideoDao;
    private MemoryVideoPhotoDao memoryVideoPhotoDao;

    private static Context appContext;


    public static Context getAppContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        instance = this;

        // 初始化数据库帮助类
        databaseHelper = DatabaseHelper.getInstance(this);

        // 初始化所有DAO
        initializeDaos();
        mainRepository = new MainRepository(
                userDao,
                albumDao,
                albumPhotoDao,
                photoDao, photoTagDao,
                tagDao, searchHistoryDao,
                memoryVideoDao,
                memoryVideoPhotoDao
        );

        Log.d("MyApplication", "Application and DAOs initialized");
    }

    private void initializeDaos() {
        userDao = new UserDao(this);
        photoDao = new PhotoDao(this);
        albumDao = new AlbumDao(this);
        albumPhotoDao = new AlbumPhotoDao(this);
        tagDao = new TagDao(this);
        photoTagDao = new PhotoTagDao(this);
        searchHistoryDao = new SearchHistoryDao(this);
        memoryVideoDao = new MemoryVideoDao(this);
        memoryVideoPhotoDao = new MemoryVideoPhotoDao(this);
    }

    public static MyApplication getInstance() {
        return instance;
    }

    // DAO 获取方法
    public UserDao getUserDao() {
        return userDao;
    }

    public PhotoDao getPhotoDao() {
        return photoDao;
    }

    public AlbumDao getAlbumDao() {
        return albumDao;
    }

    public AlbumPhotoDao getAlbumPhotoDao() {
        return albumPhotoDao;
    }

    public TagDao getTagDao() {
        return tagDao;
    }

    public PhotoTagDao getPhotoTagDao() {
        return photoTagDao;
    }

    public SearchHistoryDao getSearchHistoryDao() {
        return searchHistoryDao;
    }

    public MemoryVideoDao getMemoryVideoDao() {
        return memoryVideoDao;
    }

    public MemoryVideoPhotoDao getMemoryVideoPhotoDao() {
        return memoryVideoPhotoDao;
    }

    public MainRepository getMainRepository() {
        return mainRepository;
    }
}