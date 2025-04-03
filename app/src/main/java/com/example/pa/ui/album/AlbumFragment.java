package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.ui.photo.PhotoFragment;

import java.util.ArrayList;
import java.util.Objects;

public class AlbumFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private AlbumViewModel albumViewModel;

    private ImageView addIcon;
    private ImageView cameraIcon;
    private ImageView moreIcon;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用 inflater 加载 fragment_album 布局文件
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);

        // 初始化 RecyclerView
        recyclerView = rootView.findViewById(R.id.grid_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // 设置适配器
        albumAdapter = new AlbumAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(albumAdapter);

        // 初始化右上角的图标
        addIcon = rootView.findViewById(R.id.add_icon);
        cameraIcon = rootView.findViewById(R.id.order_icon);
        moreIcon = rootView.findViewById(R.id.set_icon);

        // 获取 ViewModel
        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);

        // 观察图片列表变化
        albumViewModel.getAlbumList().observe(getViewLifecycleOwner(), albums -> {
            albumAdapter = new AlbumAdapter(albums, this);
            recyclerView.setAdapter(albumAdapter);
        });

        // 观察事件变化
        albumViewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            Toast.makeText(getContext(), event, Toast.LENGTH_SHORT).show();
        });

        // 设置点击事件
        addIcon.setOnClickListener(v -> albumViewModel.onAddClicked());
        cameraIcon.setOnClickListener(v -> albumViewModel.onOrderClicked());
        moreIcon.setOnClickListener(v -> albumViewModel.onSetClicked());

        return rootView;
    }

    @Override
    public void onAlbumClick(String albumName) {
        // 进入相册二级界面
        PhotoFragment photoFragment = PhotoFragment.newInstance(albumName);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, photoFragment)
                .addToBackStack(null)
                .commit();
    }
}

