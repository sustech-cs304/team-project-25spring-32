package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.ui.photo.ImageItem;
import com.example.pa.ui.photo.PhotoDetailActivity;

import java.util.ArrayList;

public class PhotoinAlbumFragment extends Fragment implements PhotoinAlbumAdapter.OnPhotoClickListener {

    private static final String ARG_ALBUM_NAME = "album_name";
    private String albumName;
    private RecyclerView recyclerView;
    private PhotoinAlbumAdapter photoinAlbumAdapter;
    private PhotoinAlbumViewModel photoinAlbumViewModel;
    private ImageView backButton;  // 用于处理返回按钮

    public static PhotoinAlbumFragment newInstance(String albumName) {
        PhotoinAlbumFragment fragment = new PhotoinAlbumFragment();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_in_album, container, false);

        // 设置 RecyclerView 网格布局，每行3个
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // 初始化适配器，初始数据为空，由 ViewModel 提供
        photoinAlbumAdapter = new PhotoinAlbumAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(photoinAlbumAdapter);

        // 获取 ViewModel 实例，并观察图片列表的 LiveData
        photoinAlbumViewModel = new ViewModelProvider(this).get(PhotoinAlbumViewModel.class);
        photoinAlbumViewModel.getImagesByAlbum(albumName).observe(getViewLifecycleOwner(), images -> {
            // 当数据更新时，刷新适配器的数据
            photoinAlbumAdapter.updateData(images);
        });

//        // 设置返回按钮的点击事件
//        backButton = root.findViewById(R.id.back_button);
//        backButton.setOnClickListener(v -> {
//            requireActivity().getSupportFragmentManager().popBackStack();  // 返回到上一个 Fragment (AlbumFragment)
//        });

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
