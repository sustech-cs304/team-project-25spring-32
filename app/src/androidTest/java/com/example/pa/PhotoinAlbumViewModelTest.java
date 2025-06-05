package com.example.pa;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pa.data.Daos.AlbumDao;
import com.example.pa.data.FileRepository;
import com.example.pa.ui.album.AlbumViewModel;
import com.example.pa.ui.album.PhotoinAlbumViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PhotoinAlbumViewModelTest {
    private MyApplication app;

    private PhotoinAlbumViewModel viewModel;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        app = (MyApplication) context.getApplicationContext();
        viewModel = new PhotoinAlbumViewModel();
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
    public void testGetImagesByAlbum() {
        LiveData<List<Uri>> images = viewModel.getImagesByAlbum("");
    }

    @Test
    public void testLoadAlbumPhotos() {
        viewModel.loadAlbumPhotos("", false);
        viewModel.loadAlbumPhotos("", true);
    }

    @Test
    public void testDeletePhotos() {
        viewModel.deletePhotos(new ArrayList<>(), "");
    }

    @Test
    public void testCopyPhotos() {
        viewModel.copyPhotosToAlbum(new ArrayList<>(), "");
    }

    @Test
    public void testMovePhotos() {
        viewModel.movePhotosToAlbum(new ArrayList<>(), "");
    }

    @Test
    public void testGetDeleteEvent() {
        LiveData<AlbumViewModel.DeleteEvent> deleteEvent = viewModel.getDeleteEvent();
    }
}
