package com.example.pa.ui.photo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        imageList = new ArrayList<>();
        // 假设这些是你的图片 URL，真实情况下你可能会从网络或本地加载
        imageList.add(new ImageItem("https://example.com/photo1.jpg"));
        imageList.add(new ImageItem("https://example.com/photo2.jpg"));
        imageList.add(new ImageItem("https://example.com/photo3.jpg"));
        // 添加更多图片

        photoAdapter = new PhotoAdapter(imageList);
        recyclerView.setAdapter(photoAdapter);

        return root;
    }
}
