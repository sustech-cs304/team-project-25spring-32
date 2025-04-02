package com.example.pa.ui.album;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumViewModel extends ViewModel {

    private MutableLiveData<List<String>> imageList = new MutableLiveData<>();
    private MutableLiveData<String> event = new MutableLiveData<>();

    public AlbumViewModel() {
        // 初始化数据
        List<String> initialData = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            initialData.add("Image " + i);
        }
        imageList.setValue(initialData);
    }

    public LiveData<List<String>> getImageList() {
        return imageList;
    }

    public LiveData<String> getEvent() {
        return event;
    }

    public void onAddClicked() {
        event.setValue("Add clicked");
    }

    public void onOrderClicked() {
        event.setValue("Order clicked");
    }

    public void onSetClicked() {
        event.setValue("Set clicked");
    }
}
