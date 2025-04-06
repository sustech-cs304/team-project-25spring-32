// PhotoViewModel.java
package com.example.pa.ui.photo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.pa.data.Daos.PhotoDao.Photo;

public class PhotoViewModel extends ViewModel {

    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
    private final MutableLiveData<List<Photo>> imageList = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Photo>> getImageList() {
        return imageList;
    }

    // 加载初始数据，可来自网络、数据库或本地资源，这里仅作为示例
    public void loadInitialData() {
        List<Photo> images = new ArrayList<>();
        // 初始化一些样例图片
        images.add(new Photo(1, 101, "photo", "/storage/emulated/0/Pictures/anime.png", "https://cdn.pixabay.com/photo/2024/09/21/10/53/anime-9063542_1280.png", "2024-09-21 10:53:00", "2024-09-21 10:50:00", 139.6917, 35.6895, "Tokyo, Japan", "一张精美的动漫风格插画", Arrays.asList("动漫", "人物", "背景")));
        images.add(new Photo(2, 102, "photo", "/storage/emulated/0/Pictures/blueberries.jpg", "https://cdn.pixabay.com/photo/2025/03/06/08/25/blueberries-9450130_1280.jpg", "2025-03-06 08:25:00", "2025-03-06 08:20:00", -77.0369, 38.9072, "Washington, USA", "新鲜的蓝莓特写", Arrays.asList("水果", "蓝莓", "健康食品")));
        images.add(new Photo(3, 103, "photo", "/storage/emulated/0/Pictures/lotus.jpg", "https://cdn.pixabay.com/photo/2025/03/19/15/04/lotus-9480927_1280.jpg", "2025-03-19 15:04:00", "2025-03-19 14:50:00", 78.9629, 20.5937, "India", "宁静的莲花池", Arrays.asList("莲花", "水", "宁静")));
        images.add(new Photo(4, 104, "photo", "/storage/emulated/0/Pictures/little-girl.jpg", "https://cdn.pixabay.com/photo/2025/03/03/13/49/little-girl-9444205_1280.jpg", "2025-03-03 13:49:00", "2025-03-03 13:30:00", 2.3522, 48.8566, "Paris, France", "一个可爱的小女孩", Arrays.asList("儿童", "微笑", "户外")));
        images.add(new Photo(5, 105, "photo", "/storage/emulated/0/Pictures/woman.jpg", "https://cdn.pixabay.com/photo/2023/08/10/03/39/woman-8180638_1280.jpg", "2023-08-10 03:39:00", "2023-08-10 03:20:00", -0.1276, 51.5074, "London, UK", "一位优雅的女士", Arrays.asList("女性", "优雅", "城市背景")));
        images.add(new Photo(6, 106, "photo", "/storage/emulated/0/Pictures/woman2.jpg", "https://cdn.pixabay.com/photo/2025/02/11/04/53/woman-9398011_1280.jpg", "2025-02-11 04:53:00", "2025-02-11 04:30:00", 151.2093, -33.8688, "Sydney, Australia", "一位自信的女性", Arrays.asList("女性", "自信", "时尚")));
        images.add(new Photo(7, 107, "photo", "/storage/emulated/0/Pictures/ballerina.png", "https://cdn.pixabay.com/photo/2025/01/14/18/29/ballerina-9333398_1280.png", "2025-01-14 18:29:00", "2025-01-14 18:10:00", -58.3816, -34.6037, "Buenos Aires, Argentina", "芭蕾舞者的优雅身姿", Arrays.asList("舞蹈", "芭蕾", "优雅")));
        images.add(new Photo(8, 108, "photo", "/storage/emulated/0/Pictures/woman3.jpg", "https://cdn.pixabay.com/photo/2021/07/14/15/43/woman-6466382_1280.jpg", "2021-07-14 15:43:00", "2021-07-14 15:30:00", 103.8198, 1.3521, "Singapore", "一位沉思的女子", Arrays.asList("女性", "沉思", "都市生活")));
        images.add(new Photo(9, 109, "photo", "/storage/emulated/0/Pictures/architecture.jpg", "https://cdn.pixabay.com/photo/2024/09/08/20/30/architecture-9033164_1280.jpg", "2024-09-08 20:30:00", "2024-09-08 20:15:00", 34.0522, 48.2437, "Los Angeles, USA", "现代建筑设计", Arrays.asList("建筑", "现代", "设计")));
        images.add(new Photo(10, 110, "photo", "/storage/emulated/0/Pictures/shack.jpg", "https://cdn.pixabay.com/photo/2023/06/26/21/22/shack-8090832_1280.jpg", "2023-06-26 21:22:00", "2023-06-26 21:10:00", 55.2708, 25.2048, "Dubai, UAE", "一个破旧的小屋", Arrays.asList("房屋", "简陋", "乡村风光")));
        images.add(new Photo(11, 111, "photo", "/storage/emulated/0/Pictures/photo11.jpg", "https://cdn.pixabay.com/photo/2021/04/12/04/22/woman-6171278_1280.jpg", "2021-04-12 04:22:00", "2021-04-12 04:20:00", 0.0, 0.0, "Unknown", "A photo of a woman", Arrays.asList("woman")));
        images.add(new Photo(12, 112, "photo", "/storage/emulated/0/Pictures/photo12.jpg", "https://cdn.pixabay.com/photo/2022/10/07/08/59/sky-7504583_1280.jpg", "2022-10-07 08:59:00", "2022-10-07 08:57:00", 0.0, 0.0, "Unknown", "A scenic photo of the sky", Arrays.asList("sky")));
        images.add(new Photo(13, 113, "photo", "/storage/emulated/0/Pictures/photo13.jpg", "https://cdn.pixabay.com/photo/2024/10/16/16/14/cat-9125207_1280.jpg", "2024-10-16 16:14:00", "2024-10-16 16:12:00", 0.0, 0.0, "Unknown", "A photo of a cat", Arrays.asList("cat")));
        images.add(new Photo(14, 114, "photo", "/storage/emulated/0/Pictures/photo14.jpg", "https://cdn.pixabay.com/photo/2025/02/03/22/59/mountain-9380557_1280.jpg", "2025-02-03 22:59:00", "2025-02-03 22:57:00", 0.0, 0.0, "Unknown", "A photo of a mountain", Arrays.asList("mountain")));
        images.add(new Photo(15, 115, "photo", "/storage/emulated/0/Pictures/photo15.jpg", "https://cdn.pixabay.com/photo/2021/11/26/20/45/lantern-6826691_1280.jpg", "2021-11-26 20:45:00", "2021-11-26 20:43:00", 0.0, 0.0, "Unknown", "A photo of a lantern", Arrays.asList("lantern")));
        images.add(new Photo(16, 116, "photo", "/storage/emulated/0/Pictures/photo16.jpg", "https://cdn.pixabay.com/photo/2023/10/27/10/28/woman-8344944_1280.jpg", "2023-10-27 10:28:00", "2023-10-27 10:26:00", 0.0, 0.0, "Unknown", "A photo of a woman", Arrays.asList("woman")));
        images.add(new Photo(17, 117, "photo", "/storage/emulated/0/Pictures/photo17.jpg", "https://cdn.pixabay.com/photo/2025/03/18/06/13/ai-generated-9477429_1280.jpg", "2025-03-18 06:13:00", "2025-03-18 06:11:00", 0.0, 0.0, "Unknown", "An AI-generated image", Arrays.asList("AI-generated")));
        images.add(new Photo(18, 118, "photo", "/storage/emulated/0/Pictures/photo18.jpg", "https://cdn.pixabay.com/photo/2022/07/14/09/41/asian-woman-7320903_1280.jpg", "2022-07-14 09:41:00", "2022-07-14 09:39:00", 0.0, 0.0, "Unknown", "A photo of an Asian woman", Arrays.asList("Asian woman")));
        images.add(new Photo(19, 119, "photo", "/storage/emulated/0/Pictures/photo19.jpg", "https://cdn.pixabay.com/photo/2022/07/06/12/58/woman-7305089_1280.jpg", "2022-07-06 12:58:00", "2022-07-06 12:56:00", 0.0, 0.0, "Unknown", "A photo of a woman", Arrays.asList("woman")));
        images.add(new Photo(20, 120, "photo", "/storage/emulated/0/Pictures/photo20.jpg", "https://cdn.pixabay.com/photo/2025/02/23/23/53/ai-generated-9426863_1280.jpg", "2025-02-23 23:53:00", "2025-02-23 23:51:00", 0.0, 0.0, "Unknown", "An AI-generated image", Arrays.asList("AI-generated")));
        images.add(new Photo(21, 121, "photo", "/storage/emulated/0/Pictures/photo21.jpg", "https://cdn.pixabay.com/photo/2024/09/19/13/14/ai-generated-9058755_1280.jpg", "2024-09-19 13:14:00", "2024-09-19 13:12:00", 0.0, 0.0, "Unknown", "An AI-generated image", Arrays.asList("AI-generated")));
        images.add(new Photo(22, 122, "photo", "/storage/emulated/0/Pictures/photo22.jpg", "https://cdn.pixabay.com/photo/2018/04/20/17/18/cat-3336579_1280.jpg", "2018-04-20 17:18:00", "2018-04-20 17:16:00", 0.0, 0.0, "Unknown", "A photo of a cat", Arrays.asList("cat")));
        images.add(new Photo(23, 123, "photo", "/storage/emulated/0/Pictures/photo23.jpg", "https://cdn.pixabay.com/photo/2018/04/15/05/13/mountain-3320884_1280.jpg", "2018-04-15 05:13:00", "2018-04-15 05:11:00", 0.0, 0.0, "Unknown", "A photo of a mountain", Arrays.asList("mountain")));
        images.add(new Photo(24, 124, "photo", "/storage/emulated/0/Pictures/photo24.jpg", "https://cdn.pixabay.com/photo/2025/01/16/13/20/universe-9337607_1280.jpg", "2025-01-16 13:20:00", "2025-01-16 13:18:00", 0.0, 0.0, "Unknown", "A photo of the universe", Arrays.asList("universe")));
        images.add(new Photo(25, 125, "photo", "/storage/emulated/0/Pictures/photo25.jpg", "https://cdn.pixabay.com/photo/2019/11/09/14/01/sunset-4613612_1280.jpg", "2019-11-09 14:01:00", "2019-11-09 13:59:00", 0.0, 0.0, "Unknown", "A photo of a sunset", Arrays.asList("sunset")));
        images.add(new Photo(26, 126, "photo", "/storage/emulated/0/Pictures/photo26.jpg", "https://cdn.pixabay.com/photo/2023/02/10/16/07/new-york-7781184_1280.jpg", "2023-02-10 16:07:00", "2023-02-10 16:05:00", 0.0, 0.0, "Unknown", "A photo of New York cityscape", Arrays.asList("New York", "cityscape")));
        images.add(new Photo(27, 127, "photo", "/storage/emulated/0/Pictures/photo27.jpg", "https://cdn.pixabay.com/photo/2021/10/31/16/10/hot-air-balloons-6757939_1280.jpg", "2021-10-31 16:10:00", "2021-10-31 16:08:00", 0.0, 0.0, "Unknown", "A photo of hot air balloons", Arrays.asList("hot air balloons")));
        images.add(new Photo(28, 128, "photo", "/storage/emulated/0/Pictures/photo28.jpg", "https://cdn.pixabay.com/photo/2023/12/30/21/14/fields-8478994_1280.jpg", "2023-12-30 21:14:00", "2023-12-30 21:12:00", 0.0, 0.0, "Unknown", "A photo of fields", Arrays.asList("fields")));
        images.add(new Photo(29, 129, "photo", "/storage/emulated/0/Pictures/photo29.jpg", "https://cdn.pixabay.com/photo/2022/08/19/13/31/woman-7396948_1280.jpg", "2022-08-19 13:31:00", "2022-08-19 13:29:00", 0.0, 0.0, "Unknown", "A photo of a woman", Arrays.asList("woman")));
        images.add(new Photo(30, 130, "photo", "/storage/emulated/0/Pictures/photo30.jpg", "https://cdn.pixabay.com/photo/2024/12/13/20/29/alps-9266131_1280.jpg", "2024-12-13 20:29:00", "2024-12-13 20:27:00", 0.0, 0.0, "Unknown", "A photo of the Alps", Arrays.asList("Alps")));
        images.add(new Photo(31, 131, "photo", "/storage/emulated/0/Pictures/photo31.jpg", "https://cdn.pixabay.com/photo/2023/07/21/21/05/bus-8142339_1280.jpg", "2023-07-21 21:05:00", "2023-07-21 21:03:00", 0.0, 0.0, "Unknown", "A photo of a bus", Arrays.asList("bus")));
        images.add(new Photo(32, 132, "photo", "/storage/emulated/0/Pictures/photo32.jpg", "https://cdn.pixabay.com/photo/2024/12/28/14/31/little-red-riding-hood-9296256_1280.jpg", "2024-12-28 14:31:00", "2024-12-28 14:29:00", 0.0, 0.0, "Unknown", "A photo of Little Red Riding Hood", Arrays.asList("Little Red Riding Hood")));
        images.add(new Photo(33, 133, "photo", "/storage/emulated/0/Pictures/photo33.jpg", "https://cdn.pixabay.com/photo/2024/12/03/01/31/accessories-9241057_1280.jpg", "2024-12-03 01:31:00", "2024-12-03 01:29:00", 0.0, 0.0, "Unknown", "A photo of accessories", Arrays.asList("accessories")));

        // 更新 LiveData，通知观察者更新 UI
        imageList.setValue(images);
    }

    // 提供对外更新图片列表的方法
    public void updateImageList(List<Photo> newImages) {
        imageList.setValue(newImages);
    }
}
