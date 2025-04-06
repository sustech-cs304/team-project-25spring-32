package com.example.pa.ui.album;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.data.Daos.PhotoDao.Photo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoinAlbumViewModel extends ViewModel {

    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
    private final Map<String, MutableLiveData<List<Photo>>> albumImages = new HashMap<>();

    public PhotoinAlbumViewModel() {
        // 初始化每个相册的默认图片
        initializeAlbums();
    }

    private void initializeAlbums() {
        addInitialImages("旅行", Arrays.asList(
                new Photo(1, 101, "photo", "/storage/emulated/0/Pictures/anime.png", "https://cdn.pixabay.com/photo/2024/09/21/10/53/anime-9063542_1280.png", "2024-09-21 10:53:00", "2024-09-21 10:50:00", 139.6917, 35.6895, "Tokyo, Japan", "一张精美的动漫风格插画", Arrays.asList("动漫", "人物", "背景")),
                new Photo(2, 102, "photo", "/storage/emulated/0/Pictures/blueberries.jpg", "https://cdn.pixabay.com/photo/2025/03/06/08/25/blueberries-9450130_1280.jpg", "2025-03-06 08:25:00", "2025-03-06 08:20:00", -77.0369, 38.9072, "Washington, USA", "新鲜的蓝莓特写", Arrays.asList("水果", "蓝莓", "健康食品"))
        ));
        addInitialImages("家人", Arrays.asList(
                new Photo(3, 103, "photo", "/storage/emulated/0/Pictures/lotus.jpg", "https://cdn.pixabay.com/photo/2025/03/19/15/04/lotus-9480927_1280.jpg", "2025-03-19 15:04:00", "2025-03-19 14:50:00", 78.9629, 20.5937, "India", "宁静的莲花池", Arrays.asList("莲花", "水", "宁静"))
,
                new Photo(4, 104, "photo", "/storage/emulated/0/Pictures/little-girl.jpg", "https://cdn.pixabay.com/photo/2025/03/03/13/49/little-girl-9444205_1280.jpg", "2025-03-03 13:49:00", "2025-03-03 13:30:00", 2.3522, 48.8566, "Paris, France", "一个可爱的小女孩", Arrays.asList("儿童", "微笑", "户外"))
        ));
        addInitialImages("朋友", Arrays.asList(
                new Photo(5, 105, "photo", "/storage/emulated/0/Pictures/woman.jpg", "https://cdn.pixabay.com/photo/2023/08/10/03/39/woman-8180638_1280.jpg", "2023-08-10 03:39:00", "2023-08-10 03:20:00", -0.1276, 51.5074, "London, UK", "一位优雅的女士", Arrays.asList("女性", "优雅", "城市背景"))
,
                new Photo(6, 106, "photo", "/storage/emulated/0/Pictures/woman2.jpg", "https://cdn.pixabay.com/photo/2025/02/11/04/53/woman-9398011_1280.jpg", "2025-02-11 04:53:00", "2025-02-11 04:30:00", 151.2093, -33.8688, "Sydney, Australia", "一位自信的女性", Arrays.asList("女性", "自信", "时尚"))

        ));
        addInitialImages("宠物", Arrays.asList(
                new Photo(7, 107, "photo", "/storage/emulated/0/Pictures/ballerina.png", "https://cdn.pixabay.com/photo/2025/01/14/18/29/ballerina-9333398_1280.png", "2025-01-14 18:29:00", "2025-01-14 18:10:00", -58.3816, -34.6037, "Buenos Aires, Argentina", "芭蕾舞者的优雅身姿", Arrays.asList("舞蹈", "芭蕾", "优雅")),
                new Photo(8, 108, "photo", "/storage/emulated/0/Pictures/woman3.jpg", "https://cdn.pixabay.com/photo/2021/07/14/15/43/woman-6466382_1280.jpg", "2021-07-14 15:43:00", "2021-07-14 15:30:00", 103.8198, 1.3521, "Singapore", "一位沉思的女子", Arrays.asList("女性", "沉思", "都市生活"))
        ));
    }

    private void addInitialImages(String albumName, List<Photo> images) {
        albumImages.put(albumName, new MutableLiveData<>(images));
    }
    public LiveData<List<Photo>> getImagesByAlbum(String albumName) {
        if (!albumImages.containsKey(albumName)) {
            albumImages.put(albumName, new MutableLiveData<>(new ArrayList<>()));
        }
        return albumImages.get(albumName);
    }

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
