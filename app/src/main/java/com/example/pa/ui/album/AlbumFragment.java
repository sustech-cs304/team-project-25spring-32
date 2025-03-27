package com.example.pa.ui.album;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.databinding.FragmentAlbumBinding;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private List<String> imageList;

    // 按钮图标
    private ImageView addIcon;
    private ImageView cameraIcon;
    private ImageView moreIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用inflater加载fragment_album布局文件
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);

        // 初始化RecyclerView
        recyclerView = rootView.findViewById(R.id.grid_recycler_view);

        // 设置GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // 创建并设置适配器
        imageList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            imageList.add("Image " + i);
        }
        albumAdapter = new AlbumAdapter(imageList);
        recyclerView.setAdapter(albumAdapter);

        // 初始化右上角的图标
        addIcon = rootView.findViewById(R.id.add_icon);
        cameraIcon = rootView.findViewById(R.id.camera_icon);
        moreIcon = rootView.findViewById(R.id.more_icon);

        // 设置点击事件
        addIcon.setOnClickListener(v -> {
            // 处理加号按钮点击事件
            Toast.makeText(getContext(), "Add clicked", Toast.LENGTH_SHORT).show();
        });

        cameraIcon.setOnClickListener(v -> {
            // 处理相机按钮点击事件
            Toast.makeText(getContext(), "Camera clicked", Toast.LENGTH_SHORT).show();
        });

        moreIcon.setOnClickListener(v -> {
            // 处理更多按钮点击事件
            Toast.makeText(getContext(), "More clicked", Toast.LENGTH_SHORT).show();
        });

        return rootView; // 返回根视图
    }
}
