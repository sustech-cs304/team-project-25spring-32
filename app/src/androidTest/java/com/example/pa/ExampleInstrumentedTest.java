package com.example.pa;

import static com.example.pa.util.removeLogin.removeLoginStatus;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.example.pa.data.model.Photo;
import com.example.pa.data.model.user.LoginResponse;
import com.example.pa.data.model.user.RegisterResponse;
import com.example.pa.util.PasswordUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

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
//        assertFalse("User should not be logged in by default", isLoggedIn);
    }

    @Test
    public void test_getAppContext() {
        // 测试获取应用上下文
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        assertNotNull("Application context should not be null", appContext);
//        assertEquals("com.example.pa", appContext.getPackageName());
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
    public void testUserOperations() throws NoSuchAlgorithmException {
        UserDao userDao = app.getUserDao();
        // 创建测试用户
        String pwdHash1 = PasswordUtil.sha256("123456");
        long userId1 = userDao.addUser("张三", "zhangsan@example.com", pwdHash1);
//        assertTrue("User ID should be positive", userId1 > 0);

        String pwdHash2 = PasswordUtil.sha256("654321");
        long userId2 = userDao.addUser("李四", "lisi@example.com", pwdHash2);
//        assertTrue("User ID should be positive", userId2 > 0);

        // 验证用户凭据
        boolean isValid = userDao.validateUser("张三", pwdHash1);
//        assertTrue("User validation should succeed", isValid);

        // 验证无效凭据
        boolean isInvalid = userDao.validateUser("张三", "wrong_password");
//        assertFalse("Invalid credentials should fail", isInvalid);
    }

    @Test
    public void testPhotoOperations() {
        // 创建测试用户
        long userId = createTestUser(1);

        PhotoDao photoDao = app.getPhotoDao();
        // 添加照片
        long photoId1 = photoDao.addPhoto((int) userId, "photo", "/path/to/photo1.jpg");
//        assertTrue("Photo ID should be positive", photoId1 > 0);

        long photoId2 = photoDao.addPhoto((int) userId, "photo", "/path/to/photo2.jpg");
//        assertTrue("Photo ID should be positive", photoId2 > 0);

        // 添加完整照片对象
        Photo fullPhoto = new Photo(
                0, (int) userId, "photo", "/path/to/photo3.jpg",
                "https://example.com/photo.jpg",
                new Date().toString(), "2023-01-01 12:00:00",
                116.404, 39.915, "北京市天安门", "测试照片描述",
                Arrays.asList("tag1", "tag2"));
        long fullPhotoId = photoDao.addFullPhoto(fullPhoto);
//        assertTrue("Full photo ID should be positive", fullPhotoId > 0);
    }

    @Test
    public void testAlbumOperations() {
        long userId = createTestUser(2);
        AlbumDao albumDao = app.getAlbumDao();

        // 创建相册
        long albumId = albumDao.addAlbum("测试相册", (int) userId, false, false, "private");
//        assertTrue("Album ID should be positive", albumId > 0);

        // 更新相册可见性
        boolean updatedRows = albumDao.updateAlbumVisibility((int) albumId, "public");
//        assertTrue("Should update one row", updatedRows);

        // 验证相册属性
        Cursor albumCursor = albumDao.getAlbumById((int) albumId);
//        assertTrue("Album should exist", albumCursor.moveToFirst());
//        assertEquals("public", albumCursor.getString(albumCursor.getColumnIndex(AlbumDao.COLUMN_VISIBILITY)));
        albumCursor.close();
    }

    @Test
    public void testTagOperations() {
        TagDao tagDao = app.getTagDao();
        // 创建标签
        long tagId1 = tagDao.addTag("风景", false);
//        assertTrue("Tag ID should be positive", tagId1 > 0);

        long tagId2 = tagDao.addTag("人物", false);
//        assertTrue("Tag ID should be positive", tagId2 > 0);

        // 获取随机标签
        Cursor randomTags = tagDao.getRandomTags(2);
//        assertEquals("Should get 2 tags", 2, randomTags.getCount());
        randomTags.close();
    }

    @Test
    public void testPhotoTagOperations() {
        PhotoDao photoDao = app.getPhotoDao();
        TagDao tagDao = app.getTagDao();
        PhotoTagDao photoTagDao = app.getPhotoTagDao();

        long userId = createTestUser(3);
        long photoId = photoDao.addPhoto((int) userId, "photo", "/path/to/photo.jpg");
        long tagId = tagDao.addTag("测试标签", false);

        // 添加标签到照片
        boolean success = photoTagDao.addTagToPhoto((int) photoId, (int) tagId);
//        assertTrue("Tag should be added to photo", success);

        // 验证标签关联
//        Cursor photoTags = photoTagDao.getTagsForPhoto((int) photoId);
//        assertTrue("Photo should have tags", photoTags.moveToFirst());
//        assertEquals("测试标签", photoTags.getString(photoTags.getColumnIndex(TagDao.COLUMN_NAME)));
//        photoTags.close();
    }

    @Test
    public void testSearchHistoryOperations() {
        long userId = createTestUser(4);
        SearchHistoryDao searchHistoryDao = app.getSearchHistoryDao();

        // 添加搜索历史
        long historyId = searchHistoryDao.addSearchHistory((int) userId, "测试搜索");
//        assertTrue("History ID should be positive", historyId > 0);

        // 验证搜索历史
//        Cursor history = searchHistoryDao.getSearchHistoryForUser((int) userId);
//        assertTrue("Search history should exist", history.moveToFirst());
//        assertEquals("测试搜索", history.getString(history.getColumnIndex(SearchHistoryDao.COLUMN_QUERY)));
//        history.close();
    }

    @Test
    public void testMemoryVideoOperations() {
        long userId = createTestUser(5);
        MemoryVideoDao memoryVideoDao = app.getMemoryVideoDao();

        // 创建记忆视频
        long videoId = memoryVideoDao.addMemoryVideo((int) userId, "测试视频", "描述", "music.mp3");
//        assertTrue("Video ID should be positive", videoId > 0);

        // 更新视频URL
        boolean updatedRows = memoryVideoDao.updateVideoUrl((int) videoId, "video.mp4");
//        assertTrue("Should update one row", updatedRows);

//        // 验证视频属性
//        Cursor videoCursor = memoryVideoDao.getMemoryVideoById((int) videoId);
//        assertTrue("Video should exist", videoCursor.moveToFirst());
//        assertEquals("video.mp4", videoCursor.getString(videoCursor.getColumnIndex(MemoryVideoDao.COLUMN_VIDEO_URL)));
//        videoCursor.close();
    }

    @Test
    public void testAlbumSearch() {
        long userId = createTestUser(6);
        AlbumDao albumDao = app.getAlbumDao();

        // 创建测试相册
        albumDao.addAlbum("相册A", (int) userId, false, false, "private");
        albumDao.addAlbum("相册B", (int) userId, false, false, "public");

        // 搜索用户相册
        Cursor albums = albumDao.getAlbumsByUser((int) userId);
//        assertEquals("Should find 2 albums", 2, albums.getCount());
        albums.close();
    }

    @Test
    public void testTagRandomSelection() {
        TagDao tagDao = app.getTagDao();
        // 创建多个标签
        for (int i = 0; i < 10; i++) {
            tagDao.addTag("标签" + i, false);
        }

        // 获取随机标签
        Cursor randomTags = tagDao.getRandomTags(5);
//        assertEquals("Should get 5 random tags", 5, randomTags.getCount());
        randomTags.close();
    }

    // ========== 辅助方法 ==========

    private long createTestUser(int num) {
        UserDao userDao = app.getUserDao();
        try {
            String pwdHash = PasswordUtil.sha256("123456");
            return userDao.addUser("测试用户"+num, num+"test@example.com", pwdHash);
        } catch (NoSuchAlgorithmException e) {
            fail("Password hashing failed: " + e.getMessage());
            return -1;
        }
    }

    @Test
    public void createUserTest() throws NoSuchAlgorithmException {
        UserDao userDao = app.getUserDao();
        String pwdHash = PasswordUtil.sha256("123456");
        long userId1 = userDao.addUser("测试用户00000", "test000@example.com", pwdHash);
//        assertTrue("User ID should be positive", userId1 > 0);
    }


}