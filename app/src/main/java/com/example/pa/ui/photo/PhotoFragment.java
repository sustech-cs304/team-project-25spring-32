package com.example.pa.ui.photo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;

import java.util.ArrayList;

public class PhotoFragment extends Fragment implements PhotoAdapter.OnPhotoClickListener {

    private static final String ARG_ALBUM_NAME = "album_name";
    private String albumName;
    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private PhotoViewModel photoViewModel;

    public static PhotoFragment newInstance(String albumName) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALBUM_NAME, albumName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumName = getArguments().getString(ARG_ALBUM_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo, container, false);

        // 设置 RecyclerView 网格布局，每行3个
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // 初始化适配器，初始数据为空，由 ViewModel 提供
        photoAdapter = new PhotoAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(photoAdapter);

        // 获取 ViewModel 实例，并观察图片列表的 LiveData
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        photoViewModel.getImagesByAlbum(albumName).observe(getViewLifecycleOwner(), images -> {
            // 当数据更新时，刷新适配器的数据
            photoAdapter.updateData(images);
        });

        // 加载初始数据（也可以通过网络请求获取）
//        photoViewModel.loadInitialData();

        return root;
    }

    // 实现点击事件回调，处理图片点击后跳转到大图展示页面
    @Override
    public void onPhotoClick(ImageItem imageItem) {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            intent.putExtra("image_url", imageItem.getUrl());
            startActivity(intent);

            // 添加Activity过渡动画
            if (getActivity() != null) {
                getActivity().overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
            }
        }
    }
}
