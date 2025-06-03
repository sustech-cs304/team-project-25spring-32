package com.example.pa;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pa.MyApplication;
import com.example.pa.data.FileRepository;
import com.example.pa.data.model.Photo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@RunWith(AndroidJUnit4.class)
public class FileRepositoryUnitTest {

    private FileRepository fileRepository;
    @Mock private Context mockContext;
    @Mock private MyApplication mockApplication;
    @Mock private SharedPreferences mockPrefs;
    @Mock private SharedPreferences.Editor mockEditor;
    @Mock private FileRepository.DeleteCallback mockDeleteCallback;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockContext.getApplicationContext()).thenReturn(mockApplication);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor);

        fileRepository = new FileRepository(mockContext);
    }

    @Test
    public void testGenerateUniqueFileName() {
        // 测试带扩展名的文件名
        String original1 = "photo.jpg";
        String result1 = fileRepository.generateUniqueFileName(original1);
        assertTrue(result1.startsWith("photo_"));
        assertTrue(result1.endsWith(".jpg"));
        assertNotEquals(original1, result1);

        // 测试不带扩展名的文件名
        String original2 = "document";
        String result2 = fileRepository.generateUniqueFileName(original2);
        assertTrue(result2.startsWith("document_"));
        assertTrue(result2.endsWith(".jpg"));

        // 测试空输入
        String result3 = fileRepository.generateUniqueFileName(null);
        assertNotNull(result3);
        assertTrue(result3.endsWith(".jpg"));
    }

//    @Test
//    public void testSyncLockManagement() {
//        ReentrantLock lock = new ReentrantLock(true);
//        fileRepository = new FileRepository(mockContext) {
//            @Override
//            protected void performIncrementalSync() {
//                assertTrue(lock.isLocked());
//            }
//        };
//
//        // 测试锁获取和释放
//        fileRepository.triggerIncrementalSync();
//    }
//
//    @Test
//    public void testDebounceLogic() {
//        AtomicBoolean syncCalled = new AtomicBoolean(false);
//        fileRepository = new FileRepository(mockContext) {
//            @Override
//            protected void performIncrementalSync() {
//                syncCalled.set(true);
//            }
//        };
//
//        // 第一次调用应该触发同步
//        fileRepository.triggerIncrementalSync();
//        assertTrue(syncCalled.get());
//
//        // 重置状态
//        syncCalled.set(false);
//
//        // 快速连续调用应该被防抖
//        fileRepository.triggerIncrementalSync();
//        fileRepository.triggerIncrementalSync();
//        assertFalse(syncCalled.get());
//    }

    @Test
    public void testGetLastSyncTime() {
        when(mockPrefs.getLong(eq("last_sync_time"), eq(0L))).thenReturn(123456789L);
        long result = fileRepository.getLastSyncTime();
        assertEquals(123456789L, result);
    }

    @Test
    public void testSaveLastSyncTime() {
        fileRepository.saveLastSyncTime(987654321L);
        verify(mockEditor).putLong("last_sync_time", 987654321L);
        verify(mockEditor).apply();
    }

    @Test
    public void testAlbumDirCaching() {
        Map<String, File> cache = new HashMap<>();
        fileRepository.setAlbumDirCache(cache);

        // 第一次获取应该创建新条目
        fileRepository.getAlbumDir("testAlbum");
        assertEquals(1, cache.size());

        // 第二次获取应该使用缓存
        fileRepository.getAlbumDir("testAlbum");
        assertEquals(1, cache.size());
    }

//    @Test
//    public void testUpdateLocalDatabase() {
//        // 创建测试数据
//        List<Photo> changedPhotos = new ArrayList<>();
//        changedPhotos.add(new Photo(1, 1, "type", "path1", null, null, null, 0, 0, null, null, null));
//        changedPhotos.add(new Photo(2, 1, "type", "path2", null, null, null, 0, 0, null, null, null));
//
//        List<String> deletedUris = Arrays.asList("deleted1", "deleted2");
//
//        // 调用方法
//        fileRepository.updateLocalDatabase(changedPhotos, deletedUris, 1);
//
//        // 验证回调和状态（实际实现中应有更多验证点）
//        assertNotNull(fileRepository);
//    }
}