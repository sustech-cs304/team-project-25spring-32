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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.ui.PlayerView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.ui.photo.PhotoDetailActivity;
import com.example.pa.util.VideoPlayerManager;

public class MemoryDetailFragment extends Fragment implements MemoryPhotoAdapter.OnPhotoClickListener {

    private static final String TAG = "MemoryDetailFragment";

    private RecyclerView recyclerView;
    private MemoryPhotoAdapter adapter;
    private MemoryDetailViewModel viewModel;
    private ActivityResultLauncher<Intent> customizeVideoLauncher; // 添加启动器
    private ImageButton btnBack;
    private ImageButton btnAdd;
    private ImageButton btnDelete;
    private ImageButton btnExport;

    // 播放器使用的组件
    private VideoPlayerManager videoPlayerManager;
    private PlayerView playerView;


    public static MemoryDetailFragment newInstance(String memoryName) {
        MemoryDetailFragment fragment = new MemoryDetailFragment();
        Bundle args = new Bundle();
        args.putString("memory_name", memoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPlayerManager = new VideoPlayerManager(requireContext());
        // ViewModel 的创建应该在这里，但不应该直接初始化 FFmpegVideoCreationService
        // FFmpegVideoCreationService 应该由 ViewModel 自身管理
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory_detail, container, false);
        initToolbar(view);

        // 视频预览区
//        View videoPreview = view.findViewById(R.id.video_preview);
        playerView = view.findViewById(R.id.player_view);

        // 图片展示区
        recyclerView = view.findViewById(R.id.photo_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        customizeVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        handleCustomizeResult(data);
                    } else {
                        // 用户取消或返回，可以显示提示或不做任何事
                        Log.d(TAG, "Video customization cancelled or failed.");
                    }
                });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MemoryDetailViewModel.class);

        String memoryName = null;
        if (getArguments() != null) {
            memoryName = getArguments().getString("memory_name");
        }

        // 初始化视频播放器
        videoPlayerManager.initialize(playerView);

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

        // 观察视频 Uri变化并调用 Manager
        viewModel.currentVideoUri.observe(getViewLifecycleOwner(), uri -> {
            videoPlayerManager.loadVideo(uri); // 只调用 loadVideo
        });

        // 加载数据
        viewModel.loadPhotos(memoryName);
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
        btnExport.setOnClickListener(v -> {
            // 启动 CustomizeVideoActivity
            Intent intent = new Intent(getContext(), CustomizeVideoActivity.class);
            customizeVideoLauncher.launch(intent);
        });
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

    // ======== Fragment 生命周期管理 =========
    @Override
    public void onStart() {
        super.onStart();
        // Manager 内部会处理播放逻辑，但如果需要，可以调用 start()
        // 但由于我们 loadVideo 时就自动播放，这里可能不需要额外调用
        // videoPlayerManager.start();
        // 如果是从 Stop 状态回来，需要重新加载或初始化
        Uri currentUri = viewModel.currentVideoUri.getValue();
        if (currentUri != null && playerView != null) { // 确保 playerView 已被初始化
            Log.d(TAG, "onStart: Reloading video for URI: " + currentUri);
            videoPlayerManager.loadVideo(currentUri);
        } else if (playerView == null) {
            Log.w(TAG, "onStart: playerView is null, cannot reload video.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoPlayerManager != null) {
            videoPlayerManager.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (videoPlayerManager != null) {
            videoPlayerManager.release();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 确保在 View 销毁时也释放，以防 onStop 未被充分调用（例如 Fragment 被替换但未停止）
        if (videoPlayerManager != null) {
            videoPlayerManager.release();
        }
        playerView = null; // 清除对 View 的引用
    }
    // ======== Fragment 生命周期管理 ========


    // 加载从CustomizeVideoActivity里面获取的定制视频参数
    private void handleCustomizeResult(Intent data) {
        try {
            int width = data.getIntExtra(CustomizeVideoActivity.EXTRA_WIDTH, 1280);
            int height = data.getIntExtra(CustomizeVideoActivity.EXTRA_HEIGHT, 720);
            int durationMs = data.getIntExtra(CustomizeVideoActivity.EXTRA_DURATION_MS, 3000);
            String transitionName = data.getStringExtra(CustomizeVideoActivity.EXTRA_TRANSITION_TYPE);
            TransitionType transitionType = TransitionType.valueOf(transitionName != null ? transitionName : TransitionType.FADE.name());
            int frameRate = data.getIntExtra(CustomizeVideoActivity.EXTRA_FRAME_RATE, 30);
            String musicUriString = data.getStringExtra(CustomizeVideoActivity.EXTRA_MUSIC_URI);
            Uri musicUri = musicUriString != null ? Uri.parse(musicUriString) : null;
            float musicVolume = data.getFloatExtra(CustomizeVideoActivity.EXTRA_MUSIC_VOLUME, 1.0f); // 默认 1.0

            Log.d(TAG, "Received custom options: " + width + "x" + height +
                    ", Duration=" + durationMs + "ms" +
                    ", Transition=" + transitionType.name() +
                    ", FrameRate=" + frameRate +
                    ", Music=" + (musicUri != null ? musicUri.toString() : "None"));

            // 调用 ViewModel 的导出方法，并传递参数
            viewModel.exportVideo(width, height, durationMs, transitionType, frameRate, musicUri, musicVolume);

        } catch (Exception e) {
            Log.e(TAG, "Error processing customization result", e);
            Toast.makeText(getContext(), "处理定制参数时出错", Toast.LENGTH_SHORT).show();
        }
    }

}