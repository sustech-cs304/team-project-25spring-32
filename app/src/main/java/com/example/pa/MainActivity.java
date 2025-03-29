package com.example.pa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.pa.data.PhotoDao;
import com.example.pa.data.UserDao;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化UserDao
        userDao = new UserDao(this);
        Log.d("UserDB", "UserDao初始化完成");

        // 初始化PhotoDao
        photoDao = new PhotoDao(this);
        Log.d("PhotoDB", "PhotoDao初始化完成");

        // 测试数据库操作（仅在debug模式运行）
        testDatabaseOperations();


        // 设置底部导航
        setupBottomNavigation();
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
            // 清空测试数据
            userDao.clearTable();
            photoDao.clearTable();
            Log.d("Database", "已清空测试数据");

            // ========== 用户测试 ==========
            // 添加测试用户（密码需要哈希处理）
            String pwdHash1 = PasswordUtil.sha256("123456");
            String pwdHash2 = PasswordUtil.sha256("654321");

            long userId1 = userDao.addUser("张三", "zhangsan@example.com", pwdHash1);
            long userId2 = userDao.addUser("李四", "lisi@example.com", pwdHash2);

            if (userId1 == -1 || userId2 == -1) {
                Log.e("UserDB", "添加测试用户失败");
                return;
            }

            // 验证用户登录
            boolean valid = userDao.validateUser("张三", pwdHash1);
            Log.d("UserDB", "用户验证结果: " + (valid ? "成功" : "失败"));

            // ========== 照片测试 ==========
            // 添加基本照片
            long photoId1 = photoDao.addPhoto((int) userId1, "photo", "/storage/emulated/0/DCIM/photo1.jpg");
            long photoId2 = photoDao.addPhoto((int) userId1, "video", "/storage/emulated/0/DCIM/video1.mp4");
            long photoId3 = photoDao.addPhoto((int) userId2, "photo", "/storage/emulated/0/DCIM/photo2.jpg");

            Log.d("PhotoDB", "添加照片结果: " + photoId1 + ", " + photoId2 + ", " + photoId3);

            // 添加完整照片信息
            PhotoDao.Photo fullPhoto = new PhotoDao.Photo(
                    0, // ID will be auto-generated
                    (int) userId2,
                    "photo",
                    "/storage/emulated/0/DCIM/photo3.jpg",
                    new Date().toString(),
                    "2023-01-01 12:00:00",
                    116.404,
                    39.915,
                    "北京市天安门",
                    "测试照片描述",
                    Arrays.asList("person", "building", "sky")
            );
            long fullPhotoId = photoDao.addFullPhoto(fullPhoto);
            Log.d("PhotoDB", "添加完整照片结果: " + fullPhotoId);

            // 更新照片描述
            boolean updateDescResult = photoDao.updateDescription((int) photoId1, "更新后的描述");
            Log.d("PhotoDB", "更新描述结果: " + updateDescResult);

            // 更新AI识别结果
            boolean updateAIResult = photoDao.updateAIObjects((int) photoId2, Arrays.asList("car", "road", "tree"));
            Log.d("PhotoDB", "更新AI结果: " + updateAIResult);

            // 查询照片详情
            PhotoDao.Photo queriedPhoto = photoDao.getPhotoById((int) fullPhotoId);
            if (queriedPhoto != null) {
                Log.d("PhotoDB", "查询照片详情: " + queriedPhoto.description +
                        ", AI对象: " + queriedPhoto.aiObjects.toString());
            }

            // 测试删除照片
            boolean deleteResult = photoDao.deletePhoto((int) photoId3);
            Log.d("PhotoDB", "删除照片结果: " + deleteResult);

        } catch (NoSuchAlgorithmException e) {
            Log.e("Database", "密码哈希失败", e);
        } catch (Exception e) {
            Log.e("Database", "数据库测试异常", e);
        }
    }

    @Override
    protected void onDestroy() {
        userDao.clearTable();
        Log.d("UserDB", "已清理测试数据");

        super.onDestroy();
    }
}