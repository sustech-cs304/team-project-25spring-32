package com.example.pa;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pa.ui.select.PhotoSelectViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PhotoSelectViewModelTest {

    private MyApplication app;

    private PhotoSelectViewModel viewModel;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        app = (MyApplication) context.getApplicationContext();
        viewModel = new PhotoSelectViewModel(app);
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
    public void testLoadPhotos() {
        viewModel.loadPhotos("");
    }

    @Test
    public void testGetImagesByAlbum() {
        LiveData<List<Uri>> images = viewModel.getPhotos();
    }

    @Test
    public void testUpdateSelectedCount() {
        viewModel.updateSelectionCount(1);
    }

    @Test
    public void testGetSelectedCount() {
        LiveData<Integer> count = viewModel.getSelectedCount();
    }
}
