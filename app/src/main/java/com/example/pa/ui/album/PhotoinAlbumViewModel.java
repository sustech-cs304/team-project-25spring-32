package com.example.pa.ui.album;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.MyApplication;
import com.example.pa.data.model.Photo;
import com.example.pa.data.FileRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoinAlbumViewModel extends ViewModel {

    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
//    private final Map<String, MutableLiveData<List<Photo>>> albumImages = new HashMap<>();
    private final FileRepository fileRepository;
    private final MutableLiveData<List<Uri>> photos = new MutableLiveData<>();

    public PhotoinAlbumViewModel() {
        // 初始化每个相册的默认图片
        this.fileRepository = MyApplication.getInstance().getFileRepository();
    }

    // 暴露给Fragment的LiveData
    public LiveData<List<Uri>> getImagesByAlbum(String albumName) {
        loadAlbumPhotos(albumName);
        return photos;
    }

    // 核心方法：加载相册内容
    public void loadAlbumPhotos(String albumName) {
        List<Uri> result = fileRepository.getAlbumImages(albumName);
        photos.postValue(result);
    }

//    public LiveData<List<Photo>> getImagesByAlbum(String albumName) {
//        if (!albumImages.containsKey(albumName)) {
//            albumImages.put(albumName, new MutableLiveData<>(new ArrayList<>()));
//        }
//        return albumImages.get(albumName);
//    }

//    public void addImage(String albumName, String imageUrl) {
//        MutableLiveData<List<Photo>> liveData = albumImages.get(albumName);
//        if (liveData != null) {
//            List<Photo> currentList = new ArrayList<>(liveData.getValue());
//            currentList.add(new Photo(imageUrl));
//            liveData.setValue(currentList);
//        }
//    }

//    public void removeImage(String albumName, Photo imageItem) {
//        MutableLiveData<List<Photo>> liveData = albumImages.get(albumName);
//        if (liveData != null) {
//            List<Photo> currentList = new ArrayList<>(liveData.getValue());
//            currentList.remove(imageItem);
//            liveData.setValue(currentList);
//        }
//    }

}
