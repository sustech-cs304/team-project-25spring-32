package com.example.pa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.pa.data.UserDao;
import com.example.pa.databinding.ActivityMainBinding;
import com.example.pa.util.PasswordUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化UserDao
        userDao = new UserDao(this);
        Log.d("UserDB", "UserDao初始化完成");

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
            Log.d("UserDB", "已清空测试数据");

            // 添加测试用户（密码需要哈希处理）
            String pwdHash1 = PasswordUtil.sha256("123456");
            String pwdHash2 = PasswordUtil.sha256("654321");

            long id1 = userDao.addUser("张三", "zhangsan@example.com", pwdHash1);
            long id2 = userDao.addUser("李四", "lisi@example.com", pwdHash2);

            if (id1 == -1 || id2 == -1) {
                Log.e("UserDB", "添加测试用户失败");
                return;
            }

            // 验证用户登录
            boolean valid = userDao.validateUser("张三", pwdHash1);
            Log.d("UserDB", "用户验证结果: " + (valid ? "成功" : "失败"));

            // 查询用户详情
            UserDao.User user = userDao.getUserByUsername("李四");
            if (user != null) {
                Log.d("UserDB", String.format(
                        "用户详情: ID=%d, 用户名=%s, 注册时间=%s",
                        user.id, user.username, user.createdTime
                ));
            }

            // 更新用户头像
            userDao.updateUserAvatar((int)id1,
                    "/data/user/0/com.example.pa/avatar_1.jpg",
                    "https://example.com/avatars/1.jpg");

        } catch (NoSuchAlgorithmException e) {
            Log.e("UserDB", "密码哈希失败", e);
        } catch (Exception e) {
            Log.e("UserDB", "数据库测试异常", e);
        }
    }

    @Override
    protected void onDestroy() {
        userDao.clearTable();
        Log.d("UserDB", "已清理测试数据");

        super.onDestroy();
    }
}