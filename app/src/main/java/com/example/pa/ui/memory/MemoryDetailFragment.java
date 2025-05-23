package com.example.pa.ui.memory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.ui.photo.PhotoDetailActivity;

import java.util.List;

public class MemoryDetailFragment extends Fragment implements MemoryPhotoAdapter.OnPhotoClickListener {

    private static final String TAG = "MemoryDetailFragment";

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ViewModel 的创建应该在这里，但不应该直接初始化 FFmpegVideoCreationService
        // FFmpegVideoCreationService 应该由 ViewModel 自身管理
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory_detail, container, false);
        initToolbar(view);
        // 视频预览占位框 (仅作为布局元素存在)
        View videoPreview = view.findViewById(R.id.video_preview);
        recyclerView = view.findViewById(R.id.photo_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MemoryDetailViewModel.class);

        String memoryId = null;
        if (getArguments() != null) {
            memoryId = getArguments().getString("memory_id");
        }

        viewModel.getPhotoUris().observe(getViewLifecycleOwner(), uris -> {
            adapter = new MemoryPhotoAdapter(uris, this);
            recyclerView.setAdapter(adapter);
        });

        // 观察 ViewModel 中的 Toast 消息
        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                // 确保 Toast 在主线程显示 (LiveData.observe 默认就在主线程)
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // 观察视频生成状态，可以用来控制按钮的可用性或显示进度条
        viewModel.isCreatingVideo.observe(getViewLifecycleOwner(), isCreating -> {
            btnExport.setEnabled(!isCreating); // 如果正在生成，禁用导出按钮
            // 可以在这里显示/隐藏一个进度指示器
            // 例如：progressBar.setVisibility(isCreating ? View.VISIBLE : View.GONE);
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
        btnAdd.setOnClickListener(v -> Toast.makeText(getContext(), "添加照片 (TODO)", Toast.LENGTH_SHORT).show());
        btnDelete.setOnClickListener(v -> Toast.makeText(getContext(), "批量删除 (TODO)", Toast.LENGTH_SHORT).show());
        // Fragment 触发 ViewModel 的导出逻辑
        btnExport.setOnClickListener(v -> viewModel.exportVideo());
    }

    public void onBackPressed() {
        requireActivity().finish();
    }

    @Override
    public void onPhotoClick(Uri uri) {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            intent.putExtra("Uri", uri);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 在 Fragment 销毁视图时，不再需要手动取消 FFmpeg 任务，因为 ViewModel 已经负责了
        // ViewModel 的 onCleared() 会处理资源的释放
    }
}