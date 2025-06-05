package com.example.pa;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.lifecycle.Observer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pa.data.Daos.AlbumDao;
import com.example.pa.data.Daos.AlbumDao.Album;
import com.example.pa.data.FileRepository;
import com.example.pa.ui.album.AlbumViewModel;
import com.example.pa.util.testHelper.CursorTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AlbumViewModelTest {
    private MyApplication app;
    private FileRepository fileRepository;
    private AlbumDao albumDao;
    private AlbumViewModel viewModel;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        app = (MyApplication) context.getApplicationContext();
        fileRepository = app.getFileRepository();
        albumDao = app.getAlbumDao();
        viewModel = new AlbumViewModel();
    }

    @After
    public void tearDown() {
        app = null;
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", false)
                .apply();
    }

    @Test
    public void testLoadAlbums_includesAllPhotos() {
        Cursor cursor = CursorTestUtil.createAlbumCursor();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
//            albumDao.clearAlbums();
//            albumDao.insertCursorData(cursor);
            viewModel.loadAlbums();
        });

        List<Album> result = viewModel.getAlbumList().getValue();
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("All Photos", result.get(0).name);
//        assertEquals("测试", result.get(1).name);
    }

    @Test
    public void testAddAlbum_albumExists() {
        Cursor cursor = CursorTestUtil.createCursorWithAlbumName("已有相册");

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
//            albumDao.clearAlbums();
//            albumDao.insertCursorData(cursor);
            viewModel.addAlbum("已有相册", 1, false, false, "private");
        });

//        assertEquals("Album has existed", viewModel.getEvent().getValue());
    }

    @Test
    public void testAddAlbum_createFolderFails() {
        Cursor cursor = CursorTestUtil.createEmptyCursor();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
//            albumDao.clearAlbums();
//            albumDao.insertCursorData(cursor);
//            fileRepository.deleteAlbum("新相册"); // 确保不存在
            viewModel.addAlbum("新相册", 1, false, false, "private");
        });

//        assertEquals("Failed to create folder", viewModel.getEvent().getValue());
    }

    @Test
    public void testAddAlbum_success() {
        Cursor cursor = CursorTestUtil.createEmptyCursor();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
//            albumDao.clearAlbums();
//            albumDao.insertCursorData(cursor);
            fileRepository.createAlbum("新相册"); // 创建文件夹
            viewModel.addAlbum("新相册", 1, false, false, "private");
        });

//        assertEquals("Album added successfully", viewModel.getEvent().getValue());
    }

    @Test
    public void testDeleteAlbum_triggersDeleteEvent() {
        Uri uri1 = Uri.parse("content://media/1");
        Uri uri2 = Uri.parse("content://media/2");

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            viewModel.getDeleteEvent().observeForever(new Observer<AlbumViewModel.DeleteEvent>() {
                @Override
                public void onChanged(AlbumViewModel.DeleteEvent event) {
//                    assertEquals(2, event.uris.size());
//                    assertEquals("Album deleted successfully", viewModel.getEvent().getValue());
                }
            });

            // 将文件模拟为存在
            fileRepository.createAlbum("TestAlbum");
            viewModel.deleteAlbum(1, "TestAlbum");
        });
    }

    @Test
    public void testGetAlbumByUserId() {
        viewModel.getAlbumsByUserId(1);
    }
}
