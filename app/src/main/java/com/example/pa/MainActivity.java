package com.example.pa;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pa.data.Daos.*;
import com.example.pa.databinding.ActivityMainBinding;
import com.example.pa.util.PasswordUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化权限请求
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // 检查权限是否全部授予
                    boolean allGranted = true;
                    for (Boolean isGranted : permissions.values()) {
                        if (!isGranted) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "部分权限被拒绝", Toast.LENGTH_SHORT).show();
                        // 可选：跳转到设置引导用户手动开启
                    }
                }
        );

        // 首次启动时请求权限
        requestNecessaryPermissions();
        // 测试数据库操作 (仅用于开发环境)

        testDatabaseOperations();


        // 设置底部导航
        setupBottomNavigation();
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to get permissions, and
     * directly copy the code from its response.
     */
    // 请求所需权限
    private void requestNecessaryPermissions() {
        Log.d("Permission", "开始检查权限...");
        List<String> permissionsToRequest = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasReadMediaImages = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;
            Log.d("Permission", "READ_MEDIA_IMAGES 权限状态: " + hasReadMediaImages);

            if (!hasReadMediaImages) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasWriteStorage = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
            Log.d("Permission", "WRITE_EXTERNAL_STORAGE 权限状态: " + hasWriteStorage);

            if (!hasWriteStorage) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            Log.d("Permission", "需要请求权限: " + permissionsToRequest);
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            Log.d("Permission", "所有权限已授予");
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_search,
                R.id.navigation_photo,
                R.id.navigation_album,
                R.id.navigation_memory)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @SuppressLint("Range")
    private void testDatabaseOperations() {
        try {
            // 获取 Application 中的 DAO 实例
            MyApplication app = (MyApplication) getApplication();

            // ========== 用户测试 ==========
            testUserOperations(app.getUserDao());

            // ========== 照片测试 ==========
            testPhotoOperations(app.getPhotoDao());

            // ========== 相册测试 ==========
            //testAlbumOperations(app.getAlbumDao());

            // ========== 标签测试 ==========
            testTagOperations(app.getTagDao());

            // ========== 搜索历史测试 ==========
            testSearchHistoryOperations(app.getSearchHistoryDao());

            // ========== 记忆视频测试 ==========
            testMemoryVideoOperations(app.getMemoryVideoDao());

            // ========== 照片标签测试 ==========
            testPhotoTagOperations(app.getPhotoTagDao());

            // 测试搜索
            Cursor cursor = app.getAlbumDao().getAlbumsByUser(1);
            if (cursor.moveToFirst()) {
                do {
                    Log.d("testSEARCH", "Album name: " + cursor.getString(cursor.getColumnIndex(AlbumDao.COLUMN_NAME)));
                } while (cursor.moveToNext());
                cursor.close();
            }

            // 测试标签
            Cursor cursor2 = app.getTagDao().getRandomTags(3);
            if (cursor2.moveToFirst()) {
                do {
                    Log.d("testTAG", "Tag name: " + cursor2.getString(cursor2.getColumnIndex(TagDao.COLUMN_NAME)));
                } while (cursor2.moveToNext());
                cursor2.close();
            }

            Log.d("Database", "All database tests completed successfully");

        } catch (NoSuchAlgorithmException e) {
            Log.e("Database", "Password hashing failed", e);
        } catch (Exception e) {
            Log.e("Database", "Database test exception", e);
        }
    }

    private void testUserOperations(UserDao userDao) throws NoSuchAlgorithmException {
        String pwdHash1 = PasswordUtil.sha256("123456");
        String pwdHash2 = PasswordUtil.sha256("654321");

        long userId1 = userDao.addUser("张三", "zhangsan@example.com", pwdHash1);
        long userId2 = userDao.addUser("李四", "lisi@example.com", pwdHash2);

        boolean valid = userDao.validateUser("张三", pwdHash1);
        Log.d("UserDB", "User validation result: " + (valid ? "Success" : "Failed"));
    }

    private void testPhotoOperations(PhotoDao photoDao) {
        long userId1 = 1;
        long userId2 = 2;

        long photoId1 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/ic_launcher.png");
        long photoId2 = photoDao.addPhoto((int) userId1, "video", "/storage/emulated/0/DCIM/video1.mp4");
        // 假设 userId1 是已定义的有效用户ID
        long photoId3 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/girl.jpeg");
        long photoId4 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/boy.jpeg");
        long photoId5 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/pig.jpg");
        long photoId6 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/hehua.jpeg");
        long photoId7 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/bird.jpeg");
        long photoId8 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId9 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId10 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId11 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId12 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId13 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId14 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId15 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId16 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId17 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId18 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId19 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId20 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId21 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId22 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");
        long photoId23 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/juhua.png");

        PhotoDao.Photo fullPhoto = new PhotoDao.Photo(
                0, (int) userId2, "photo", "/storage/emulated/0/DCIM/ic_launcher.png",
                "https://gd-hbimg.huaban.com/758e7de9f82dc52f2c8840915a5acfa9458fa15c50d3e-Bv5Tcc_fw480webp",
                new Date().toString(), "2023-01-01 12:00:00",
                116.404, 39.915, "北京市天安门", "测试照片描述",
                Arrays.asList("person", "building", "sky"));
        photoDao.addFullPhoto(fullPhoto);
    }

    private void testAlbumOperations(AlbumDao albumDao) {
        long userId1 = 1;
        long albumId1 = albumDao.addAlbum("旅行相册", (int) userId1, false, false, "private");
        albumDao.updateAlbumVisibility((int) albumId1, "public");
    }

    private void testTagOperations(TagDao tagDao) {
        long tagId1 = tagDao.addTag("风景", false);
        long tagId2 = tagDao.addTag("人物", false);
        long tagId3 = tagDao.addTag("建筑", false);
        long tagId4 = tagDao.addTag("天空", false);
        long tagId5 = tagDao.addTag("动物", false);
    }

    private void testPhotoTagOperations(PhotoTagDao photoTagDao) {
        long photoId1 = 1;
        // 假设以下 photoId 已通过 addPhoto 生成（对应之前的照片3-8）
        long photoId3 = 3; // girl.jpeg
        long photoId4 = 4; // boy.jpeg
        long photoId5 = 5; // pig.jpg
        long photoId6 = 6; // hehua.jpeg
        long photoId7 = 7; // bird.jpeg
        long photoId8 = 8; // juhua.png

        // 假设已通过 testTagOperations 生成的标签ID
        long tagId1 = 1; // 风景
        long tagId2 = 2; // 人物
        long tagId3 = 3; // 建筑
        long tagId4 = 4; // 天空
        long tagId5 = 5; // 动物

        photoTagDao.addTagToPhoto((int) photoId1, (int) tagId1);
        photoTagDao.addTagToPhoto((int) photoId1, (int) tagId2);

        // 为每张照片添加标签（根据内容模拟逻辑）
        // 1. girl.jpeg → 人物 + 风景
        boolean success1 = photoTagDao.addTagToPhoto((int) photoId3, (int) tagId2); // 人物
        boolean success11 = photoTagDao.addTagToPhoto((int) photoId3, (int) tagId1); // 人物

        // 2. boy.jpeg → 人物
        boolean success2 = photoTagDao.addTagToPhoto((int) photoId4, (int) tagId2);
        boolean success21 = photoTagDao.addTagToPhoto((int) photoId4, (int) tagId1);


        // 3. pig.jpg → 动物
        boolean success3 = photoTagDao.addTagToPhoto((int) photoId5, (int) tagId5);
        boolean success31 = photoTagDao.addTagToPhoto((int) photoId5, (int) tagId1);


        // 4. hehua.jpeg → 风景 + 植物（若存在植物标签）
        boolean success4 = photoTagDao.addTagToPhoto((int) photoId6, (int) tagId1); // 风景

        // 5. bird.jpeg → 动物 + 天空
        boolean success5 = photoTagDao.addTagToPhoto((int) photoId7, (int) tagId4); // 天空
        boolean success6 = photoTagDao.addTagToPhoto((int) photoId7, (int) tagId5); // 动物
        photoTagDao.addTagToPhoto((int) photoId7, (int) tagId1);

        // 6. juhua.png → 建筑（假设菊花是建筑装饰）
        boolean success7 = photoTagDao.addTagToPhoto((int) photoId8, (int) tagId3);
        photoTagDao.addTagToPhoto((int) photoId8, (int) tagId1);

        // 可添加日志验证结果
        Log.d("Test", "标签添加结果: " + success1 + ", " + success2 + ", " + success3 + ", " + success4 + ", " + success5 + ", " + success6 + ", " + success7);

// 遍历为每个 photoId 添加 tag1
        for (long photoId = 9; photoId <= 23; photoId++) {
            boolean success = photoTagDao.addTagToPhoto((int) photoId, (int) tagId1);
            Log.d("TagTest", "照片 " + photoId + " 添加标签1结果: " + (success ? "成功" : "失败"));
        }
    }

    private void testSearchHistoryOperations(SearchHistoryDao searchHistoryDao) {
        long userId1 = 1;
        searchHistoryDao.addSearchHistory((int) userId1, "北京");
    }

    private void testMemoryVideoOperations(MemoryVideoDao memoryVideoDao) {
        long userId1 = 1;
        long videoId1 = memoryVideoDao.addMemoryVideo((int) userId1, "2023回忆", "温馨", "music1.mp3");
        memoryVideoDao.updateVideoUrl((int) videoId1, "video1.mp4");
    }

    private void clearAllTables(MyApplication app) {
        app.getMemoryVideoPhotoDao().clearTable();
        app.getMemoryVideoDao().clearTable();
        app.getSearchHistoryDao().clearTable();
        app.getPhotoTagDao().clearTable();
        app.getTagDao().clearTable();
        app.getAlbumPhotoDao().clearTable();
        app.getAlbumDao().clearTable();
        app.getPhotoDao().clearTable();
        app.getUserDao().clearTable();
    }

    @Override
    protected void onDestroy() {

        clearAllTables((MyApplication) getApplication());
        Log.d("Database", "Cleaned up all test data");

        super.onDestroy();
    }
}