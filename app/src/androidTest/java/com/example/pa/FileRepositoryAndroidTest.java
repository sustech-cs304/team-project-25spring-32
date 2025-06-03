//package com.example.pa;
//
//import static androidx.test.InstrumentationRegistry.getInstrumentation;
//import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
//import static org.junit.Assert.*;
//
//import android.content.ContentResolver;
//import android.content.ContentValues;
//import android.content.Context;
//import android.net.Uri;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.util.Log;
//
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.filters.SdkSuppress;
//
//import com.example.pa.MyApplication;
//import com.example.pa.R;
//import com.example.pa.data.FileRepository;
//import com.example.pa.util.UriToPathHelper;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//@RunWith(AndroidJUnit4.class)
//@SdkSuppress(minSdkVersion = 21) // 支持 Android 5.0+
//public class FileRepositoryAndroidTest {
//
//    private Context context;
//    private FileRepository fileRepository;
//    private List<Uri> testUris = new ArrayList<>();
//
//    @Before
//    public void setUp() {
//        context = getInstrumentation().getTargetContext();
//        fileRepository = new FileRepository(context);
//    }
//
//    @After
//    public void tearDown() {
//        // 清理测试文件
//        ContentResolver resolver = context.getContentResolver();
//        for (Uri uri : testUris) {
//            try {
//                resolver.delete(uri, null, null);
//            } catch (Exception e) {
//                Log.e("TestCleanup", "Error deleting test file", e);
//            }
//        }
//        testUris.clear();
//    }
//
//    private Uri createTestImage(String albumName) throws Exception {
//        ContentResolver resolver = context.getContentResolver();
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.DISPLAY_NAME, "test_image_" + System.currentTimeMillis() + ".jpg");
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//
//        if (albumName != null) {
//            values.put(MediaStore.Images.Media.RELATIVE_PATH,
//                    Environment.DIRECTORY_DCIM + "/" + albumName);
//        }
//
//        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        assertNotNull(uri);
//
//        try (OutputStream os = resolver.openOutputStream(uri);
//             InputStream is = context.getResources().openRawResource(R.drawable.example)) {
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = is.read(buffer)) != -1) {
//                os.write(buffer, 0, bytesRead);
//            }
//        }
//
//        testUris.add(uri);
//        return uri;
//    }
//
//    @Test
//    public void testCreateAlbum() throws Exception {
//        String albumName = "test_album_" + System.currentTimeMillis();
//
//        // 创建相册
//        boolean result = fileRepository.createAlbum(albumName);
//        assertTrue(result);
//
//        // 验证相册存在
//        List<Uri> images = fileRepository.getAlbumImages(albumName);
//        assertNotNull(images);
//        assertEquals(0, images.size()); // 新相册应为空
//    }
//
//    @Test
//    public void testGetAlbumImages() throws Exception {
//        String albumName = "test_album_" + System.currentTimeMillis();
//
//        // 创建相册并添加图片
//        fileRepository.createAlbum(albumName);
//        Uri testImage1 = createTestImage(albumName);
//        Uri testImage2 = createTestImage(albumName);
//
//        // 获取相册图片
//        List<Uri> images = fileRepository.getAlbumImages(albumName);
//        assertNotNull(images);
//        assertEquals(2, images.size());
//        assertTrue(images.contains(testImage1));
//        assertTrue(images.contains(testImage2));
//    }
//
//    @Test
//    public void testGetAlbumCover() throws Exception {
//        String albumName = "test_album_" + System.currentTimeMillis();
//        fileRepository.createAlbum(albumName);
//
//        // 添加多张图片
//        createTestImage(albumName);
//        Uri lastImage = createTestImage(albumName);
//
//        // 封面应为最后添加的图片
//        Uri cover = fileRepository.getAlbumCover(albumName);
//        assertEquals(lastImage, cover);
//    }
//
//    @Test
//    public void testCopyPhotos() throws Exception {
//        String sourceAlbum = "source_album_" + System.currentTimeMillis();
//        String targetAlbum = "target_album_" + System.currentTimeMillis();
//
//        fileRepository.createAlbum(sourceAlbum);
//        fileRepository.createAlbum(targetAlbum);
//
//        // 创建源文件
//        Uri sourceUri = createTestImage(sourceAlbum);
//        List<Uri> sourceUris = Collections.singletonList(sourceUri);
//
//        // 执行复制
//        boolean result = fileRepository.copyPhotos(sourceUris, targetAlbum);
//        assertTrue(result);
//
//        // 验证目标相册
//        List<Uri> targetImages = fileRepository.getAlbumImages(targetAlbum);
//        assertEquals(1, targetImages.size());
//
//        // 验证源文件仍然存在
//        List<Uri> sourceImages = fileRepository.getAlbumImages(sourceAlbum);
//        assertEquals(1, sourceImages.size());
//    }
//
//    @Test
//    public void testDeletePhotos() throws Exception {
//        String albumName = "delete_test_" + System.currentTimeMillis();
//        fileRepository.createAlbum(albumName);
//        Uri testUri = createTestImage(albumName);
//
//        // 设置删除回调
//        FileRepository.DeleteCallback callback = new FileRepository.DeleteCallback() {
//            @Override public void onComplete() {}
//            @Override public void onError(String error) { fail("Delete failed: " + error); }
//        };
//        fileRepository.setDeleteCallback(callback);
//
//        // 执行删除
//        fileRepository.executePhysicalDelete(Collections.singletonList(testUri));
//
//        // 验证文件已删除
//        Thread.sleep(1000); // 等待异步操作完成
//        List<Uri> images = fileRepository.getAlbumImages(albumName);
//        assertTrue(images.isEmpty());
//    }
//
//    @Test
//    public void testIncrementalSync() throws Exception {
//        // 创建测试文件
//        Uri newFile = createTestImage(null);
//
//        // 重置同步时间
//        fileRepository.saveLastSyncTime(0);
//
//        // 触发同步
//        fileRepository.triggerIncrementalSync();
//
//        // 等待同步完成
//        Thread.sleep(3000);
//
//        // 验证新文件被检测到（实际应用中应检查数据库）
//        String filePath = UriToPathHelper.getRealPathFromUri(context, newFile);
//        assertNotNull(filePath);
//
//        // 清理
//        context.getContentResolver().delete(newFile, null, null);
//    }
//
//    @Test
//    public void testMediaScan() throws Exception {
//        FileRepository.MediaScanCallback callback = new FileRepository.MediaScanCallback() {
//            @Override public void onScanCompleted(Uri uri) {}
//            @Override public void onScanFailed(String error) { fail("Media scan failed: " + error); }
//        };
//
//        // 创建测试相册
//        String albumName = "scan_test_" + System.currentTimeMillis();
//        fileRepository.createAlbum(albumName);
//
//        // 触发扫描
//        fileRepository.triggerMediaScanForAlbum(albumName, callback);
//
//        // 等待扫描完成
//        Thread.sleep(2000);
//
//        // 验证扫描结果（创建新文件后立即扫描）
//        Uri newFile = createTestImage(albumName);
//        List<Uri> images = fileRepository.getAlbumImages(albumName);
//        assertTrue(images.contains(newFile));
//    }
//}
