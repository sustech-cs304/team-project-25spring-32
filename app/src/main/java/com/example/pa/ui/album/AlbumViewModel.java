package com.example.pa.ui.album;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumViewModel extends ViewModel {

    private MutableLiveData<List<String>> albumList = new MutableLiveData<>();
    private MutableLiveData<String> event = new MutableLiveData<>();

    public AlbumViewModel() {
        // 初始化数据
        List<String> initialAlbums= new ArrayList<>();
        initialAlbums.add("旅行");
        initialAlbums.add("家人");
        initialAlbums.add("朋友");
        initialAlbums.add("宠物");
        albumList.setValue(initialAlbums);
    }

    public LiveData<List<String>> getAlbumList() {
        return albumList;
    }

    // 添加相册
    public void addAlbum(String albumName) {
        List<String> currentList = albumList.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(albumName);
        albumList.setValue(currentList);
    }

    // 删除相册
    public void removeAlbum(String albumName) {
        List<String> currentList = albumList.getValue();
        if (currentList != null) {
            currentList.remove(albumName);
            albumList.setValue(currentList);
        }
    }
}
