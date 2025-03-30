package com.example.pa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.pa.data.Daos.*;
import com.example.pa.databinding.ActivityMainBinding;
import com.example.pa.util.PasswordUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserDao userDao;
    private PhotoDao photoDao;
    private AlbumDao albumDao;
    private AlbumPhotoDao albumPhotoDao;
    private TagDao tagDao;
    private PhotoTagDao photoTagDao;
    private SearchHistoryDao searchHistoryDao;
    private MemoryVideoDao memoryVideoDao;
    private MemoryVideoPhotoDao memoryVideoPhotoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize all DAOs
        initializeDaos();

        // Test database operations
        testDatabaseOperations();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    /**
     * Initialize all database access objects
     */
    private void initializeDaos() {
        Context context = getApplicationContext();
        userDao = new UserDao(context);
        photoDao = new PhotoDao(context);
        albumDao = new AlbumDao(context);
        albumPhotoDao = new AlbumPhotoDao(context);
        tagDao = new TagDao(context);
        photoTagDao = new PhotoTagDao(context);
        searchHistoryDao = new SearchHistoryDao(context);
        memoryVideoDao = new MemoryVideoDao(context);
        memoryVideoPhotoDao = new MemoryVideoPhotoDao(context);

        Log.d("Database", "All DAOs initialized successfully");
    }

    private void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_search,
                R.id.navigation_photo,
                R.id.navigation_album,
                R.id.navigation_ai)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @SuppressLint("Range")
    private void testDatabaseOperations() {
        try {
            // Clear all test data
            clearAllTables();
            Log.d("Database", "Cleared all test data");

            // ========== User Tests ==========
            testUserOperations();

            // ========== Photo Tests ==========
            testPhotoOperations();

            // ========== Album Tests ==========
            testAlbumOperations();

            // ========== Tag Tests ==========
            testTagOperations();

            // ========== Search History Tests ==========
            testSearchHistoryOperations();

            // ========== Memory Video Tests ==========
            testMemoryVideoOperations();

            //TEST SEARCH
            Cursor cursor = albumDao.getAlbumsByUser(1);
            if (cursor.moveToFirst()) {
                do {
                    Log.d("testSEARCH", "Album name: " + cursor.getString(cursor.getColumnIndex(AlbumDao.COLUMN_NAME)));
                } while (cursor.moveToNext());
            }

            //TEST TAG
            Cursor cursor2 = tagDao.getRandomTags(3);
            if (cursor2.moveToFirst()) {
                do {
                    Log.d("testTAG", "Tag name: " + cursor2.getString(cursor2.getColumnIndex(TagDao.COLUMN_NAME)));
                } while (cursor2.moveToNext());
            }

            Log.d("Database", "All database tests completed successfully");

        } catch (NoSuchAlgorithmException e) {
            Log.e("Database", "Password hashing failed", e);
        } catch (Exception e) {
            Log.e("Database", "Database test exception", e);
        }
    }

    private void testUserOperations() throws NoSuchAlgorithmException {
        String pwdHash1 = PasswordUtil.sha256("123456");
        String pwdHash2 = PasswordUtil.sha256("654321");

        long userId1 = userDao.addUser("张三", "zhangsan@example.com", pwdHash1);
        long userId2 = userDao.addUser("李四", "lisi@example.com", pwdHash2);

        boolean valid = userDao.validateUser("张三", pwdHash1);
        Log.d("UserDB", "User validation result: " + (valid ? "Success" : "Failed"));
    }

    private void testPhotoOperations() {
        // Assume userId1 and userId2 are available from user tests
        long userId1 = 1; // These would come from your actual user creation
        long userId2 = 2;

        long photoId1 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/photo1.jpg");
        long photoId2 = photoDao.addPhoto((int) userId1, "video", "/storage/emulated/0/DCIM/video1.mp4");

        PhotoDao.Photo fullPhoto = new PhotoDao.Photo(
                0, (int) userId2, "photo", "/storage/emulated/0/DCIM/photo3.jpg",
                new Date().toString(), "2023-01-01 12:00:00",
                116.404, 39.915, "北京市天安门", "测试照片描述",
                Arrays.asList("person", "building", "sky"));
        photoDao.addFullPhoto(fullPhoto);
    }

    private void testAlbumOperations() {
        long userId1 = 1;
        long albumId1 = albumDao.addAlbum("旅行相册", (int) userId1, false, false, "private");
        albumDao.updateAlbumVisibility((int) albumId1, "public");
    }

    private void testTagOperations() {
        long tagId1 = tagDao.addTag("风景", false);
        long tagId2 = tagDao.addTag("人物", false);
        long tagId3 = tagDao.addTag("建筑", false);
        long tagId4 = tagDao.addTag("天空", false);
        long tagId5 = tagDao.addTag("动物", false);
        //tagDao.deleteTag((int) tagId1);
    }

    private void testSearchHistoryOperations() {
        long userId1 = 1;
        searchHistoryDao.addSearchHistory((int) userId1, "北京");
    }

    private void testMemoryVideoOperations() {
        long userId1 = 1;
        long videoId1 = memoryVideoDao.addMemoryVideo((int) userId1, "2023回忆", "温馨", "music1.mp3");
        memoryVideoDao.updateVideoUrl((int) videoId1, "video1.mp4");
    }

    private void clearAllTables() {
        memoryVideoPhotoDao.clearTable();
        memoryVideoDao.clearTable();
        searchHistoryDao.clearTable();
        photoTagDao.clearTable();
        tagDao.clearTable();
        albumPhotoDao.clearTable();
        albumDao.clearTable();
        photoDao.clearTable();
        userDao.clearTable();
    }

    @Override
    protected void onDestroy() {
        clearAllTables();
        Log.d("Database", "Cleaned up all test data");
        super.onDestroy();
    }
}