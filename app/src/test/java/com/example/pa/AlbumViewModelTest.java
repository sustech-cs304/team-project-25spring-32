//package com.example.pa;
//
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.net.Uri;
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
//import androidx.lifecycle.Observer;
//
//import com.example.pa.data.Daos.AlbumDao;
//import com.example.pa.data.Daos.AlbumDao.Album;
//import com.example.pa.data.FileRepository;
//import com.example.pa.ui.album.AlbumViewModel;
//import com.example.pa.util.testHelper.CursorTestUtil;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//public class AlbumViewModelTest {
//
//    @Rule
//    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
//
//    @Mock
//    AlbumDao albumDao;
//
//    @Mock
//    FileRepository fileRepository;
//
//    AlbumViewModel viewModel;
//    @Mock private Context mockContext;
//    @Mock private MyApplication mockApplication;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(mockContext.getApplicationContext()).thenReturn(mockApplication);
//        albumDao = new AlbumDao(mockContext);
//        fileRepository = new FileRepository(mockContext);
//        viewModel = new AlbumViewModel();
//    }
//
//    @Test
//    public void testLoadAlbums_includesAllPhotos() {
//        Cursor cursor = CursorTestUtil.createAlbumCursor();
//        when(albumDao.getAlbumsByUser(1)).thenReturn(cursor);
//
//        viewModel.loadAlbums();
//        List<Album> result = viewModel.getAlbumList().getValue();
//
//        assertNotNull(result);
//        assertEquals(2, result.size()); // “All Photos” + 1 个实际数据
//        assertEquals("All Photos", result.get(0).name);
//        assertEquals("测试", result.get(1).name);
//    }
//
//    @Test
//    public void testAddAlbum_albumExists() {
//        Cursor cursor = CursorTestUtil.createCursorWithAlbumName("已有相册");
//        when(albumDao.getAlbumsByUser(1)).thenReturn(cursor);
//
//        viewModel.addAlbum("已有相册", 1, false, false, "private");
//
//        assertEquals("Album has existed", viewModel.getEvent().getValue());
//    }
//
//    @Test
//    public void testAddAlbum_createFolderFails() {
//        Cursor cursor = CursorTestUtil.createEmptyCursor();
//        when(albumDao.getAlbumsByUser(1)).thenReturn(cursor);
//        when(fileRepository.createAlbum("新相册")).thenReturn(false);
//
//        viewModel.addAlbum("新相册", 1, false, false, "private");
//
//        assertEquals("Failed to create folder", viewModel.getEvent().getValue());
//    }
//
//    @Test
//    public void testAddAlbum_success() {
//        Cursor cursor = CursorTestUtil.createEmptyCursor();
//        when(albumDao.getAlbumsByUser(1)).thenReturn(cursor);
//        when(fileRepository.createAlbum("新相册")).thenReturn(true);
//        when(albumDao.addAlbum(anyString(), anyInt(), anyBoolean(), anyBoolean(), anyString()))
//                .thenReturn(1L);
//
//        viewModel.addAlbum("新相册", 1, false, false, "private");
//
//        assertEquals("Album added successfully", viewModel.getEvent().getValue());
//    }
//
//
//    @Test
//    public void testDeleteAlbum_triggersDeleteEvent() {
//        Uri uri1 = Uri.parse("content://media/1");
//        Uri uri2 = Uri.parse("content://media/2");
//
//        when(fileRepository.getAlbumImages("TestAlbum"))
//                .thenReturn(Arrays.asList(uri1, uri2));
//
//        Observer<AlbumViewModel.DeleteEvent> observer = mock(Observer.class);
//        viewModel.getDeleteEvent().observeForever(observer);
//
//        viewModel.deleteAlbum(1, "TestAlbum");
//
//        ArgumentCaptor<AlbumViewModel.DeleteEvent> captor = ArgumentCaptor.forClass(AlbumViewModel.DeleteEvent.class);
//        verify(observer).onChanged(captor.capture());
//
//        AlbumViewModel.DeleteEvent event = captor.getValue();
//        assertEquals(2, event.uris.size());
//        assertEquals("Album deleted successfully", viewModel.getEvent().getValue());
//    }
//}
