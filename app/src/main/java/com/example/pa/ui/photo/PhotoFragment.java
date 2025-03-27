package com.example.pa.ui.photo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoFragment extends Fragment {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<ImageItem> imageList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);
        // 设置网格布局，每行3个
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // 初始化数据集合，可先为空，后续通过接口传入数据
        imageList = new ArrayList<>();
        // 初始化一些样例图片
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2024/09/21/10/53/anime-9063542_1280.png"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/06/08/25/blueberries-9450130_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/19/15/04/lotus-9480927_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/03/13/49/little-girl-9444205_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2023/08/10/03/39/woman-8180638_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/02/11/04/53/woman-9398011_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/01/14/18/29/ballerina-9333398_1280.png"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2021/07/14/15/43/woman-6466382_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2024/09/08/20/30/architecture-9033164_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2023/06/26/21/22/shack-8090832_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2021/04/12/04/22/woman-6171278_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2022/10/07/08/59/sky-7504583_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2024/10/16/16/14/cat-9125207_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/02/03/22/59/mountain-9380557_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2021/11/26/20/45/lantern-6826691_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2023/10/27/10/28/woman-8344944_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/03/18/06/13/ai-generated-9477429_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2022/07/14/09/41/asian-woman-7320903_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2022/07/06/12/58/woman-7305089_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/02/23/23/53/ai-generated-9426863_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2024/09/19/13/14/ai-generated-9058755_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2018/04/20/17/18/cat-3336579_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2018/04/15/05/13/mountain-3320884_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2025/01/16/13/20/universe-9337607_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2019/11/09/14/01/sunset-4613612_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2023/02/10/16/07/new-york-7781184_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2021/10/31/16/10/hot-air-balloons-6757939_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2023/12/30/21/14/fields-8478994_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2022/08/19/13/31/woman-7396948_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2024/12/13/20/29/alps-9266131_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2023/07/21/21/05/bus-8142339_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2024/12/28/14/31/little-red-riding-hood-9296256_1280.jpg"));
        imageList.add(new ImageItem("https://cdn.pixabay.com/photo/2024/12/03/01/31/accessories-9241057_1280.jpg"));

        photoAdapter = new PhotoAdapter(imageList);
        recyclerView.setAdapter(photoAdapter);

        return root;
    }

    // 提供一个接口供外部输入图片列表并实时刷新界面
    // 如果传入的列表list不为空，就会交由PhotoAdapter更新list
    public void setImageList(List<ImageItem> list) {
        // 确保在主线程更新
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            // 直接通过适配器更新数据
            if (photoAdapter == null) {
                imageList = new ArrayList<>(list); // 创建新列表避免引用问题
                photoAdapter = new PhotoAdapter(imageList);
                recyclerView.setAdapter(photoAdapter);
            } else {
                photoAdapter.updateData(list); // 调用适配器的更新方法
            }
        });
    }
}
