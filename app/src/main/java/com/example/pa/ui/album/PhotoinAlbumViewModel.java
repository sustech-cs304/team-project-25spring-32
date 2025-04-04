package com.example.pa.ui.album;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.ui.photo.ImageItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoinAlbumViewModel extends ViewModel {

    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
    private final Map<String, MutableLiveData<List<ImageItem>>> albumImages = new HashMap<>();

    public PhotoinAlbumViewModel() {
        // 初始化每个相册的默认图片
        initializeAlbums();
    }

    private void initializeAlbums() {
        addInitialImages("旅行", Arrays.asList(
                "https://cdn.pixabay.com/photo/2024/09/21/10/53/anime-9063542_1280.png",
                "https://cdn.pixabay.com/photo/2025/03/06/08/25/blueberries-9450130_1280.jpg"
        ));
        addInitialImages("家人", Arrays.asList(
                "https://cdn.pixabay.com/photo/2025/03/19/15/04/lotus-9480927_1280.jpg",
                "https://cdn.pixabay.com/photo/2025/03/03/13/49/little-girl-9444205_1280.jpg"
        ));
        addInitialImages("朋友", Arrays.asList(
                "https://cdn.pixabay.com/photo/2023/08/10/03/39/woman-8180638_1280.jpg",
                "https://cdn.pixabay.com/photo/2025/02/11/04/53/woman-9398011_1280.jpg"
        ));
        addInitialImages("宠物", Arrays.asList(
                "https://cdn.pixabay.com/photo/2025/01/14/18/29/ballerina-9333398_1280.png",
                "https://cdn.pixabay.com/photo/2021/07/14/15/43/woman-6466382_1280.jpg"
        ));
    }

    private void addInitialImages(String albumName, List<String> imageUrls) {
        List<ImageItem> images = new ArrayList<>();
        for (String url : imageUrls) {
            images.add(new ImageItem(url));
        }
        albumImages.put(albumName, new MutableLiveData<>(images));
    }
    public LiveData<List<ImageItem>> getImagesByAlbum(String albumName) {
        if (!albumImages.containsKey(albumName)) {
            albumImages.put(albumName, new MutableLiveData<>(new ArrayList<>()));
        }
        return albumImages.get(albumName);
    }

    public void addImage(String albumName, String imageUrl) {
        MutableLiveData<List<ImageItem>> liveData = albumImages.get(albumName);
        if (liveData != null) {
            List<ImageItem> currentList = new ArrayList<>(liveData.getValue());
            currentList.add(new ImageItem(imageUrl));
            liveData.setValue(currentList);
        }
    }

    public void removeImage(String albumName, ImageItem imageItem) {
        MutableLiveData<List<ImageItem>> liveData = albumImages.get(albumName);
        if (liveData != null) {
            List<ImageItem> currentList = new ArrayList<>(liveData.getValue());
            currentList.remove(imageItem);
            liveData.setValue(currentList);
        }
    }

}
