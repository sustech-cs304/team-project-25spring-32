package com.example.pa;

import static com.example.pa.util.checkLogin.checkLoginStatus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

//import com.example.pa.auth.LoginActivity;
import com.example.pa.auth.LoginActivity;
import com.example.pa.data.cloudRepository.UserRepository;
import com.example.pa.data.model.Photo;

import com.example.pa.data.Daos.*;
import com.example.pa.data.FileRepository;
import com.example.pa.databinding.ActivityMainBinding;
import com.example.pa.ui.group.myGroup.MyGroupActivity;
import com.example.pa.ui.help.HelpActivity;
import com.example.pa.ui.album.AlbumViewModel;
import com.example.pa.util.PasswordUtil;
import com.example.pa.util.UriToPathHelper;
import com.example.pa.util.removeLogin;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.Manifest;

public class MainActivity extends AppCompatActivity implements FileRepository.DeleteCallback {
    private ActivityMainBinding binding;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private AppBarConfiguration appBarConfiguration;
    private FileRepository fileRepository;
    private AlbumViewModel viewModel;
    //检查是否处于登录状态
    private boolean isLoggedIn = false;
    private List<Uri> pendingDeleteUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        isLoggedIn=checkLoginStatus(this);
        // 设置导航菜单
        setupNavigationDrawerMenu();
        setupNavHeader();

        // 1. 设置Toolbar前确保没有默认ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // 或者使用 getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. 设置Toolbar
        setSupportActionBar(binding.toolbar);

        // 3. 初始化导航控制器
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // 4. 配置AppBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_search,
                R.id.navigation_photo,
                R.id.navigation_album,
                R.id.navigation_memory,
                R.id.navigation_social)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        // 5. 设置Toolbar与导航控制器
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 6. 绑定底部导航
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 7. 绑定抽屉导航 使用检查登录的形式，取消这种固定形式
        //NavigationUI.setupWithNavController(binding.navigationDrawer, navController);

        // 8. 动态显示/隐藏底部导航
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isTopLevelDestination = destination.getId() == R.id.navigation_search ||
                    destination.getId() == R.id.navigation_photo ||
                    destination.getId() == R.id.navigation_album ||
                    destination.getId() == R.id.navigation_memory ||
                    destination.getId() == R.id.navigation_social;
            binding.navView.setVisibility(isTopLevelDestination ? View.VISIBLE : View.GONE);
        });



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
                        performInitialMediaScan(() -> fileRepository.triggerIncrementalSync());
                        Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "部分权限被拒绝", Toast.LENGTH_SHORT).show();
                        // 可选：跳转到设置引导用户手动开启
                    }
                }
        );

        viewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        fileRepository=MyApplication.getInstance().getFileRepository();
        fileRepository.setDeleteCallback(this);


        initialDao();


        // 首次启动时请求权限
        requestNecessaryPermissions();

        // 测试数据库操作 (仅用于开发环境)


//        fileRepository=MyApplication.getInstance().getFileRepository();



        observeViewModel();
        // 设置底部导航
        //setupBottomNavigation();
    }

    private void initialDao() {
        // 测试用户
        String pwdHash1 = null;
        try {
            pwdHash1 = PasswordUtil.sha256("123456");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        UserDao userDao = MyApplication.getInstance().getUserDao();
        long userId1 = userDao.addUser("张三", "zhangsan@example.com", pwdHash1);

        // 测试标签
        TagDao tagDao = MyApplication.getInstance().getTagDao();
        long tagId1 = tagDao.addTag("apple", false);
        long tagId2 = tagDao.addTag("mouse", false);
        long tagId3 = tagDao.addTag("house", false);
        long tagId4 = tagDao.addTag("sky", false);
        long tagId6 = tagDao.addTag("rose", false);
    }

    private void observeViewModel() {
        viewModel.getDeleteEvent().observe(this, event -> {
            if (event != null) {
                Log.d("Delete", "成功观察");
                handleDeleteEvent(event.uris);
            }
        });
    }

    private void handleDeleteEvent(List<Uri> uris) {
        pendingDeleteUris = uris;
        fileRepository.deletePhotos(uris, deleteIntent -> {
            // 创建自定义Intent携带数据
            Intent fillInIntent = new Intent();
            fillInIntent.putParcelableArrayListExtra("DELETE_URIS", new ArrayList<>(uris));

            // 构建请求
            IntentSenderRequest request = new IntentSenderRequest.Builder(
                    deleteIntent.getIntentSender())
                    .setFillInIntent(fillInIntent) // 附加数据
                    .build();

//            deleteLauncher.launch(request);
            Log.d("Delete", "准备启动 deleteLauncher");
            try {
                deleteLauncher.launch(request);
            } catch (Exception e) {
                Log.e("Delete", "启动 deleteLauncher 失败", e);
            }
        });
    }

    private final ActivityResultLauncher<IntentSenderRequest> deleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.d("Delete", "成功返回 ");
                    List<String> uris = UriToPathHelper.uriToString(pendingDeleteUris);
                    MyApplication.getInstance().getMainRepository().deletePhotosByUri(uris);
                    MyApplication.getInstance().getMainRepository().cleanEmptyAlbums();
                }
                viewModel.loadAlbums();
            });


    private void setupNavHeader() {
        if (!isLoggedIn) return; // 如果未登录，不设置头部信息

        View headerView = binding.navigationDrawer.getHeaderView(0);
        ImageView imageView = headerView.findViewById(R.id.imageView);
        TextView usernameTextView = headerView.findViewById(R.id.username);
        //TextView emailTextView = headerView.findViewById(R.id.email);

        // 使用Glide加载头像,由于Url未实现，头像部分注释
//        Glide.with(this)
//                .load(getProfileImageUrl())
//                .placeholder(R.drawable.ic_default_profile)
//                .error(R.drawable.ic_default_profile)
//                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
//                .into(imageView);

        // 设置用户名和邮箱
//        usernameTextView.setText(getUsername());
//        emailTextView.setText(getEmail());
          usernameTextView.setText(UserRepository.getUsername());
          //emailTextView.setText("getEmail()");
    }
    private void setupNavigationDrawerMenu() {
        // 根据登录状态加载不同的菜单
        binding.navigationDrawer.getMenu().clear();
        binding.navigationDrawer.inflateMenu(isLoggedIn ? R.menu.menu_nav_logged_in : R.menu.menu_nav_logged_out);

        // 设置导航项点击监听器
        binding.navigationDrawer.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_login) {
                // 处理登录/注册
                startActivity(new Intent(this, LoginActivity.class));
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (id == R.id.nav_logout) {
                // 处理退出登录
                performLogout();
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            if (id==R.id.nav_help){
                // 处理帮助
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            }

            if (id==R.id.nav_group){
                Intent intent = new Intent(this, MyGroupActivity.class);
                startActivity(intent);
                return true;
            }


//            if (id == R.id.nav_settings) {
//                navController.navigate(R.id.navigation_settings);
//            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout_title))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.positive_button), (dialog, which) -> {
                    // 清除登录状态
                    SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("is_logged_in", false).apply();

                    // 更新登录状态并刷新菜单
                    isLoggedIn = false;
                    setupNavigationDrawerMenu();

                    // 可选：跳转到登录页面
                    //startActivity(new Intent(this, LoginActivity.class));
                    recreate();
                })
                .setNegativeButton(getString(R.string.negative_button), null)
                .show();
    }


    private void performInitialMediaScan(Runnable onScanComplete) {
        Log.d("MediaScan", "开始初始化扫描...");
        try {
            File dcimDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM
            );
            Log.d("MediaScan", "扫描路径: " + dcimDir.getAbsolutePath());
            Log.d("MediaScan", "路径是否存在: " + dcimDir.exists());

            fileRepository.triggerMediaScanForDirectory(dcimDir, new FileRepository.MediaScanCallback() {
                @Override
                public void onScanCompleted(Uri uri) {
                    Log.i("MediaScan", "扫描完成: " + uri);
                    if (onScanComplete != null) onScanComplete.run();
                }

                @Override
                public void onScanFailed(String error) {
                    Log.e("MediaScan", "扫描失败: " + error);
                    if (onScanComplete != null) onScanComplete.run();
                }
            });
        } catch (SecurityException e) {
            Log.e("MediaScan", "权限异常: " + e.getMessage());
        } catch (Exception e) {
            Log.e("MediaScan", "未知异常: " + e.toString());
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
        //setupBottomNavigation();
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
            performInitialMediaScan(() -> fileRepository.triggerIncrementalSync());
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回时重新检查登录状态
        isLoggedIn = checkLoginStatus(this);
        setupNavigationDrawerMenu();
        setupNavHeader();
    }

    @Override
    protected void onDestroy() {

//        clearAllTables((MyApplication) getApplication());
        //Log.d("Database", "Cleaned up all test data");
        removeLogin.removeLoginStatus(this);
        super.onDestroy();
    }

    @Override
    public void onComplete() {
        viewModel.loadAlbums(); // 删除成功后刷新列表
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
}