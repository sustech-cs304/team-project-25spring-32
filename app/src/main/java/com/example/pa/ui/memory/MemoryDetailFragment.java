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
import androidx.media3.common.PlaybackParameters;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.ui.photo.PhotoDetailActivity;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.ui.PlayerView;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

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
    private PlayerView playerView;
    private ExoPlayer player;
    private ImageButton btnPlayPause;
    private SeekBar seekBar;
    private TextView timeCurrent, timeTotal;
    private Spinner spinnerSpeed;
    private View controlsContainer; // 用于长按

    private boolean isSeeking = false; // 标志位，防止拖动时与自动更新冲突
    private final Handler progressHandler = new Handler(Looper.getMainLooper()); // 用于更新进度条
    private final float[] speedValues = {0.5f, 0.8f, 1.0f, 1.2f, 1.5f, 2.0f}; // 速度值数组
    private float currentSpeed = 1.0f; // 当前速度

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

        // 视频预览区
//        View videoPreview = view.findViewById(R.id.video_preview);
        playerView = view.findViewById(R.id.player_view);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        seekBar = view.findViewById(R.id.seek_bar);
        timeCurrent = view.findViewById(R.id.time_current);
        timeTotal = view.findViewById(R.id.time_total);
        spinnerSpeed = view.findViewById(R.id.spinner_speed);
        controlsContainer = view.findViewById(R.id.controls_container); // 假设这是你想长按的区域

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

        // 观察视频 Uri 并初始化播放器
        viewModel.currentVideoUri.observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                Log.d(TAG, "Video URI updated: " + uri);
                initializePlayer(uri);
            } else {
                Log.d(TAG, "Video URI is null, releasing player.");
                releasePlayer();
                // 可以在这里显示一个占位符或提示
            }
        });

        setupPlayerControls(); // 设置播放器控件监听器

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
        // 如果有 Uri，尝试初始化或恢复播放器
        if (viewModel.currentVideoUri.getValue() != null) {
            initializePlayer(viewModel.currentVideoUri.getValue());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.pause(); // 暂停播放，但不释放资源
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer(); // 在 Stop 时释放资源，避免后台播放
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer(); // 确保视图销毁时释放
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

    // ================ 视频播放器 ================
    // 初始化播放器
    private void initializePlayer(Uri videoUri) {
        if (getContext() == null) return;

        if (player == null) {
            player = new ExoPlayer.Builder(getContext()).build();
            playerView.setPlayer(player);

            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    updatePlayPauseButton();
                    if (playbackState == Player.STATE_READY) {
                        updateProgress(); // 准备好后开始更新进度
                    } else if (playbackState == Player.STATE_ENDED) {
                        player.seekTo(0); // 结束后回到开头
                        player.setPlayWhenReady(false); // 暂停
                    }
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    updatePlayPauseButton();
                    if (isPlaying) {
                        updateProgress(); // 开始播放时确保进度条在更新
                    } else {
                        progressHandler.removeCallbacks(progressUpdater); // 暂停时停止更新
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Log.e(TAG, "Player Error", error);
                    Toast.makeText(getContext(), "播放错误: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    releasePlayer();
                }
            });
        }

        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.setPlaybackParameters(new PlaybackParameters(currentSpeed)); // 设置当前速度
        player.prepare();
        player.setPlayWhenReady(true); // 自动播放
    }

    // 释放播放器
    private void releasePlayer() {
        if (player != null) {
            progressHandler.removeCallbacks(progressUpdater); // 停止进度更新
            player.release();
            player = null;
            playerView.setPlayer(null);
            timeCurrent.setText("00:00");
            timeTotal.setText("00:00");
            seekBar.setProgress(0);
            updatePlayPauseButton();
        }
    }

    // 设置播放器控件监听器
    @SuppressLint("ClickableViewAccessibility") // 用于长按监听
    private void setupPlayerControls() {
        // 播放/暂停按钮
        btnPlayPause.setOnClickListener(v -> {
            if (player != null) {
                player.setPlayWhenReady(!player.isPlaying());
            }
        });

        // SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekToPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    seekToPosition = (int) ((player.getDuration() * progress) / 100L);
                    timeCurrent.setText(formatDuration(seekToPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
                progressHandler.removeCallbacks(progressUpdater); // 拖动时暂停自动更新
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                if (player != null) {
                    player.seekTo(seekToPosition);
                }
                updateProgress(); // 拖动结束后恢复自动更新
            }
        });

        // 速度选择 Spinner
        ArrayAdapter<CharSequence> speedAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.playback_speeds,
                android.R.layout.simple_spinner_item
        );
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpeed.setAdapter(speedAdapter);
        spinnerSpeed.setSelection(2); // 默认选择 1.0x
        spinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSpeed = speedValues[position];
                if (player != null) {
                    player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
                }
                // (可选) 改变 Spinner 文本颜色，使其在深色背景上可见
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(android.R.color.white));
                } catch (Exception e) { /* ignore */ }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 长按 3 倍速 (示例：在 PlayerView 上长按)
        playerView.setOnLongClickListener(v -> true); // 消费长按事件，防止触发单击
        playerView.setOnTouchListener((v, event) -> {
            if (player == null) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 设置一个延迟任务，如果在这段时间内没有抬起，就认为是长按
                    v.postDelayed(() -> {
                        // 检查按钮是否仍然按下 (通过 isPressed 或其他标志)
                        if (v.isPressed()) {
                            Log.d(TAG, "Long press detected: Setting speed to 3x");
                            player.setPlaybackParameters(new PlaybackParameters(3.0f));
                        }
                    }, 500); // 500ms 判定为长按
                    return true; // 返回 true 以便接收后续事件

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 移除延迟任务
                    v.removeCallbacks(null);
                    // 恢复正常速度
                    Log.d(TAG, "Long press released: Setting speed back to " + currentSpeed);
                    player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
                    return true; // 返回 true
            }
            return false;
        });
    }

    // 更新播放/暂停按钮图标
    private void updatePlayPauseButton() {
        if (player != null && player.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.ic_pause); // 确保你有 ic_pause.xml
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }
    }

    // 更新进度条的 Runnable
    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying() && !isSeeking) {
                long currentPosition = player.getCurrentPosition();
                long duration = player.getDuration();
                if (duration > 0) {
                    seekBar.setProgress((int) ((currentPosition * 100) / duration));
                    timeCurrent.setText(formatDuration(currentPosition));
                    timeTotal.setText(formatDuration(duration));
                }
                progressHandler.postDelayed(this, 500); // 每 500ms 更新一次
            }
        }
    };

    // 开始更新进度条
    private void updateProgress() {
        progressHandler.removeCallbacks(progressUpdater); // 先移除旧的
        progressHandler.post(progressUpdater); // 添加新的
    }

    // 格式化时长
    private String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    // ================ 视频播放器 ================

}