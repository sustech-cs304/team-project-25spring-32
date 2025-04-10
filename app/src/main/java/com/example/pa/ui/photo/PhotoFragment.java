// PhotoFragment.java
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
import com.example.pa.data.Daos.PhotoDao.Photo;

import java.util.ArrayList;
import java.util.List;

public class PhotoFragment extends Fragment implements PhotoAdapter.OnPhotoClickListener {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private PhotoViewModel photoViewModel;

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
        photoViewModel.initPhotoDao(requireContext());
        photoViewModel.getImageList().observe(getViewLifecycleOwner(), images -> {
            // 当数据更新时，刷新适配器的数据
            photoAdapter.updateData(images);
        });



        // 从数据库加载数据
        photoViewModel.loadPhotosFromDatabase();


        return root;
    }

    // 实现点击事件回调，处理图片点击后跳转到大图展示页面
    @Override
    public void onPhotoClick(Photo photo) {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            intent.putExtra("image_path", photo.filePath);

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