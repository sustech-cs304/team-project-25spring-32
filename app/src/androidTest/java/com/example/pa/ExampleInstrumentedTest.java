package com.example.pa;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private MyApplication app;

    @Before //这个就是测试前的准备工作
    public void setUp() {
        // 获取 Application 实例, 这个一定要有
        app = (MyApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
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
}