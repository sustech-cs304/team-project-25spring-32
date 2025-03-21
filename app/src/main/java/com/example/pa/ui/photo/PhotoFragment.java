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
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));

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
