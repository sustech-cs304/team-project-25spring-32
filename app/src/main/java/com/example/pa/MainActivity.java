package com.example.pa;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.pa.data.UserDao;
import com.example.pa.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化UserDao（不再需要手动open）
        userDao = new UserDao(this);
        Log.d("Database", "数据库访问对象已初始化");

        // 测试数据库操作
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
        // 清空旧数据（测试用）
        userDao.clearTable();

        // 添加新用户
        long id1 = userDao.addUser("张三", "zhangsan@example.com");
        long id2 = userDao.addUser("李四", "lisi@example.com");
        Log.d("Database", "添加用户完成，ID: " + id1 + ", " + id2);

        // 查询并显示所有用户
        try (Cursor cursor = userDao.getAllUsers()) {
            StringBuilder userInfo = new StringBuilder("当前用户列表:\n");
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(UserDao.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME));
                String email = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EMAIL));

                userInfo.append("ID: ").append(id)
                        .append(", 姓名: ").append(name)
                        .append(", 邮箱: ").append(email).append("\n");
            }
            Log.d("Database", userInfo.toString());
        } catch (Exception e) {
            Log.e("Database", "查询用户失败", e);
        }
    }

    @Override
    protected void onDestroy() {
        // 清空表（根据需求决定是否保留）
        userDao.clearTable();
        Log.d("Database", "已清空用户表数据");

        // 注意：不再需要手动close，由DatabaseHelper统一管理
        super.onDestroy();
    }
}