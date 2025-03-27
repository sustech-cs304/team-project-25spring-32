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
import com.example.pa.data.DatabaseHelper;
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

        // 初始化数据库
        userDao = new UserDao(this);
        userDao.open();
        Log.d("Database", "数据库和表已创建");

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

    private void testDatabaseOperations() {
        // 添加用户
        userDao.addUser("张三", "zhangsan@example.com");
        userDao.addUser("李四", "lisi@example.com");

        // 查询并显示所有用户
        Cursor cursor = userDao.getAllUsers();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));

            Log.d("User", "ID: " + id + ", Name: " + name + ", Email: " + email);
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接

        userDao.close();
    }
}