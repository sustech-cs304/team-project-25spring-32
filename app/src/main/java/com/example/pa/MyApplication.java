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
import com.example.pa.data.FileRepository;
import com.example.pa.data.MainRepository;
import com.example.pa.data.cloudRepository.GroupRepository;
import com.example.pa.data.cloudRepository.UserRepository;

import org.tensorflow.lite.support.common.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MyApplication extends Application {
    private static MyApplication instance;
    private DatabaseHelper databaseHelper;//这里是数据库帮助类的实例，有个警告，
    // 但是getInstance方法之后是直接执行了onCreate
    private MainRepository mainRepository;
    private FileRepository fileRepository;
    private UserRepository userRepository;
    private GroupRepository groupRepository;
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


    //测试专用
    public static void setTestDaoProviders(PhotoTagDao pt, PhotoDao p, TagDao t, SearchHistoryDao sh, MainRepository mr) {
        instance.photoTagDao = pt;
        instance.photoDao = p;
        instance.tagDao = t;
        instance.searchHistoryDao = sh;
        instance.mainRepository = mr;
    }


    public UserRepository getUserRepository() {
        return userRepository;
    }

    public GroupRepository getGroupRepository() {
        return groupRepository;
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
                memoryVideoPhotoDao,
                this
        );

        try {
            insertAllTags();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fileRepository = new FileRepository(this);
        userRepository = new UserRepository(this);
        groupRepository = new GroupRepository();

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
    private void insertAllTags() throws IOException {
        // 这里可以添加一些默认的标签
        List<String> labels = FileUtil.loadLabels(this, "labels_mobilenet_quant_v1_224.txt");
        for (String label : labels){
            tagDao.addTag(label,true);
        }
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

    public FileRepository getFileRepository() {return fileRepository;}
}