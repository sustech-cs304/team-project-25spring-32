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
        // 如果需要也可以初始化一些样例图片
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
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        // 添加更多图片

        photoAdapter = new PhotoAdapter(imageList);
        recyclerView.setAdapter(photoAdapter);

        return root;
    }

    // 提供一个接口供外部输入图片列表并实时刷新界面
    public void setImageList(List<ImageItem> list) {
        imageList.clear();
        imageList.addAll(list);
        if (photoAdapter == null) {
            photoAdapter = new PhotoAdapter(imageList);
            recyclerView.setAdapter(photoAdapter);
        } else {
            photoAdapter.notifyDataSetChanged();
        }
    }
}
