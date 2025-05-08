// MemoryDetailFragment.java
package com.example.pa.ui.memory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.ui.photo.PhotoDetailActivity;

import java.util.List;

public class MemoryDetailFragment extends Fragment implements MemoryPhotoAdapter.OnPhotoClickListener {

    private RecyclerView recyclerView;
    private MemoryPhotoAdapter adapter;
    private MemoryDetailViewModel viewModel;
    private ImageButton btnBack;
    private ImageButton btnAdd;
    private ImageButton btnDelete;
    private ImageButton btnExport;

    public static MemoryDetailFragment newInstance(String memoryId) {
        MemoryDetailFragment fragment = new MemoryDetailFragment();
        Bundle args = new Bundle();
        args.putString("memory_id", memoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory_detail, container, false);

        // 初始化工具栏
        initToolbar(view);
        // 视频预览占位框
        View videoPreview = view.findViewById(R.id.video_preview);

        // 照片列表
        recyclerView = view.findViewById(R.id.photo_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MemoryDetailViewModel.class);

        String memoryId = getArguments().getString("memory_id");

        viewModel.getPhotoUris().observe(getViewLifecycleOwner(), uris -> {
            adapter = new MemoryPhotoAdapter(uris,this);
            recyclerView.setAdapter(adapter);
        });

        // 加载数据
        viewModel.loadPhotos(memoryId);
    }

    private void initToolbar(View rootView) {
        btnBack = rootView.findViewById(R.id.btn_back);
        btnAdd = rootView.findViewById(R.id.btn_add);
        btnDelete = rootView.findViewById(R.id.btn_delete);
        btnExport = rootView.findViewById(R.id.btn_export);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnAdd.setOnClickListener(v -> onAddPhotos());
        btnDelete.setOnClickListener(v -> onBatchDelete());
        btnExport.setOnClickListener(v -> onExportVideo());
    }

    public void onBackPressed() {
        requireActivity().finish();
    }

    public void onAddPhotos() {
        // 通过ViewModel处理
        viewModel.handleAddPhotos();
    }

    public void onBatchDelete() {
//        if (adapter != null) {
//            List<Uri> selectedItems = adapter.getSelectedItems();
//            viewModel.handleBatchDelete(selectedItems);
//        }
    }

    public void onExportVideo() {
        viewModel.handleExportVideo();
    }

    @Override
    public void onPhotoClick(Uri imageUri) {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            intent.putExtra("Uri", imageUri);
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