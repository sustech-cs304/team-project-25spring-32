// PhotoViewModel.java
package com.example.pa.ui.photo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PhotoViewModel extends ViewModel {

    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
    private final MutableLiveData<List<ImageItem>> imageList = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<ImageItem>> getImageList() {
        return imageList;
    }

    // 加载初始数据，可来自网络、数据库或本地资源，这里仅作为示例
    public void loadInitialData() {
        List<ImageItem> images = new ArrayList<>();
        // 初始化一些样例图片
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2024/09/21/10/53/anime-9063542_1280.png"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/06/08/25/blueberries-9450130_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/19/15/04/lotus-9480927_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/03/13/49/little-girl-9444205_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2023/08/10/03/39/woman-8180638_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/02/11/04/53/woman-9398011_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/01/14/18/29/ballerina-9333398_1280.png"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2021/07/14/15/43/woman-6466382_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2024/09/08/20/30/architecture-9033164_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2023/06/26/21/22/shack-8090832_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2021/04/12/04/22/woman-6171278_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2022/10/07/08/59/sky-7504583_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2024/10/16/16/14/cat-9125207_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/02/03/22/59/mountain-9380557_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2021/11/26/20/45/lantern-6826691_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2023/10/27/10/28/woman-8344944_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/18/06/13/ai-generated-9477429_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2022/07/14/09/41/asian-woman-7320903_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2022/07/06/12/58/woman-7305089_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/02/23/23/53/ai-generated-9426863_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2024/09/19/13/14/ai-generated-9058755_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2018/04/20/17/18/cat-3336579_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2018/04/15/05/13/mountain-3320884_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2025/01/16/13/20/universe-9337607_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2019/11/09/14/01/sunset-4613612_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2023/02/10/16/07/new-york-7781184_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2021/10/31/16/10/hot-air-balloons-6757939_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2023/12/30/21/14/fields-8478994_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2022/08/19/13/31/woman-7396948_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2024/12/13/20/29/alps-9266131_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2023/07/21/21/05/bus-8142339_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2024/12/28/14/31/little-red-riding-hood-9296256_1280.jpg"));
        images.add(new ImageItem("https://cdn.pixabay.com/photo/2024/12/03/01/31/accessories-9241057_1280.jpg"));


        // 更新 LiveData，通知观察者更新 UI
        imageList.setValue(images);
    }

    // 提供对外更新图片列表的方法
    public void updateImageList(List<ImageItem> newImages) {
        imageList.setValue(newImages);
    }
}
