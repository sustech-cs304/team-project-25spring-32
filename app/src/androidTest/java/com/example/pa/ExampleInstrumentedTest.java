package com.example.pa;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.pa.data.Daos.AlbumDao;
import com.example.pa.data.Daos.AlbumPhotoDao;
import com.example.pa.data.Daos.MemoryVideoDao;
import com.example.pa.data.Daos.MemoryVideoPhotoDao;
import com.example.pa.data.Daos.PhotoDao;
import com.example.pa.data.Daos.PhotoTagDao;
import com.example.pa.data.Daos.SearchHistoryDao;
import com.example.pa.data.Daos.TagDao;
import com.example.pa.data.Daos.UserDao;
import com.example.pa.data.cloudRepository.UserRepository;
import com.example.pa.data.model.user.RegisterResponse;

import io.reactivex.rxjava3.core.Observable;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private MyApplication app;
    UserRepository userRepository;

    @Before //这个就是测试前的准备工作
    public void setUp() {
        // 获取 Application 实例, 这个一定要有
        app = (MyApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        // 初始化 UserRepository
        userRepository = new UserRepository(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @After
    public void tearDown() {
        // 清理工作，如果需要的话
        app = null;
        if (com.example.pa.util.checkLogin.checkLoginStatus(InstrumentationRegistry.getInstrumentation().getTargetContext())){
            // 如果用户已登录，清除登录状态
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_logged_in", false)
                    .apply();
        }
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.pa", appContext.getPackageName());
    }

    @Test
    public void test_daos() {
        // 测试 UserDao
        UserDao userDao = app.getUserDao();
        assertNotNull(userDao);

        // 测试 PhotoDao
        PhotoDao photoDao = app.getPhotoDao();
        assertNotNull(photoDao);

        // 测试 AlbumDao
        AlbumDao albumDao = app.getAlbumDao();
        assertNotNull(albumDao);

        // 测试 AlbumPhotoDao
        AlbumPhotoDao albumPhotoDao = app.getAlbumPhotoDao();
        assertNotNull(albumPhotoDao);

        // 测试 TagDao
        TagDao tagDao = app.getTagDao();
        assertNotNull(tagDao);

        // 测试 PhotoTagDao
        PhotoTagDao photoTagDao = app.getPhotoTagDao();
        assertNotNull(photoTagDao);

        // 测试 SearchHistoryDao
        SearchHistoryDao searchHistoryDao = app.getSearchHistoryDao();
        assertNotNull(searchHistoryDao);

        // 测试 MemoryVideoDao
        MemoryVideoDao memoryVideoDao = app.getMemoryVideoDao();
        assertNotNull(memoryVideoDao);

        // 测试 MemoryVideoPhotoDao
        MemoryVideoPhotoDao memoryVideoPhotoDao = app.getMemoryVideoPhotoDao();
        assertNotNull(memoryVideoPhotoDao);
    }

//    @Test
//    public void test_getPhoto_from_path(){
//        // 测试从路径获取照片
//        String path = "/storage/emulated/0/DCIM/ic_launcher.png";
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        assertNotNull("Failed to load image from path", bitmap);
//
//
//    }

    @Test
    public void checkLoginStatus() {
        // 测试登录状态检查
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        boolean isLoggedIn = com.example.pa.util.checkLogin.checkLoginStatus(context);
        assertFalse("User should not be logged in by default", isLoggedIn);
    }

    @Test
    public void test_getAppContext() {
        // 测试获取应用上下文
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertNotNull("Application context should not be null", appContext);
        assertEquals("com.example.pa", appContext.getPackageName());
    }

//    @Test
//    public void test_ImageClassifier(){
//        // 测试 ImageClassifier
//        try {
//            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//            com.example.pa.util.ai.ImageClassifier classifier = new com.example.pa.util.ai.ImageClassifier(context);
//            assertNotNull("ImageClassifier should not be null", classifier);
//
//            // 测试分类功能
//            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
//            String result = classifier.classify(bitmap);
//            assertNotNull("Classification result should not be null", result);
//            assertFalse("Classification result should not be empty", result.isEmpty());
//        } catch (Exception e) {
//            fail("ImageClassifier initialization or classification failed: " + e.getMessage());
//        }
//
//    }

    @Test
    public void test_registerRx(){
        Observable<RegisterResponse> t= userRepository.registerRx("wang","123@qq.com","123456");
        t.subscribe(
                response -> {
                    // 只判断成功状态
                    if (response.isSuccess()) {
                        System.out.println("注册成功");
                        Log.d("success", "注册成功: ");
                    } else {
                        System.out.println("注册失败: " + response.getMessage());
                        Log.d("fail", "注册失败: " + response.getMessage());
                    }
                },
                error -> {
                    // 网络错误等异常
                    System.err.println("请求失败: " + error.getMessage());
                    Log.e("error", "请求失败: " + error.getMessage());
                }
        );
        assertNull(t);
    }
}