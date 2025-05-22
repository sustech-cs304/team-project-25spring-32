// MemoryDetailFragment.java
package com.example.pa.ui.memory;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import com.example.pa.MyApplication; // 确保导入 Application 类
import com.example.pa.R;
import com.example.pa.ui.photo.PhotoDetailActivity;
import com.example.pa.util.UriToPathHelper; // 确保导入 UriToPathHelper

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID; // 用于生成临时文件名

public class MemoryDetailFragment extends Fragment implements MemoryPhotoAdapter.OnPhotoClickListener {

    private static final String TAG = "MemoryDetailFragment";

    private RecyclerView recyclerView;
    private MemoryPhotoAdapter adapter;
    private MemoryDetailViewModel viewModel;
    private ImageButton btnBack;
    private ImageButton btnAdd;
    private ImageButton btnDelete;
    private ImageButton btnExport;

    // 用于测试 FFmpegVideoCreationService
    private FFmpegVideoCreationService videoCreationService;
    private UriToPathHelper uriToPathHelper; // 用于 MediaStore 保存时的路径处理
    private boolean isCreatingVideo = false; // 防止重复点击导出

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
        // 在 Fragment 创建时初始化 Service 和 Helper
        Context appContext = requireContext().getApplicationContext();
        videoCreationService = new FFmpegVideoCreationService(appContext);
        uriToPathHelper = new UriToPathHelper(); // 假设 UriToPathHelper 需要 Context
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory_detail, container, false);

        // 初始化工具栏
        initToolbar(view);
        // 视频预览占位框 (在这个测试版本中仅作为布局元素存在，不实际显示视频)
        View videoPreview = view.findViewById(R.id.video_preview);

        // 照片列表
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
        // TODO: 实现实际功能，这里暂时用 Toast 占位
        btnAdd.setOnClickListener(v -> Toast.makeText(getContext(), "添加照片 (TODO)", Toast.LENGTH_SHORT).show());
        btnDelete.setOnClickListener(v -> Toast.makeText(getContext(), "批量删除 (TODO)", Toast.LENGTH_SHORT).show());
        // 实现导出视频的测试逻辑
        btnExport.setOnClickListener(v -> onExportVideo());
    }

    public void onBackPressed() {
        requireActivity().finish();
    }

    public void onAddPhotos() {
        // TODO: Implement photo adding logic (e.g., launching image picker)
        Toast.makeText(getContext(), "Add photos logic goes here", Toast.LENGTH_SHORT).show();
    }

    public void onBatchDelete() {
        // TODO: Implement batch delete logic
        Toast.makeText(getContext(), "Batch delete logic goes here", Toast.LENGTH_SHORT).show();
    }

    public void onExportVideo() {
        if (isCreatingVideo) {
            Toast.makeText(getContext(), "视频正在生成中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Uri> imageUris = viewModel.getPhotoUris().getValue();
        if (imageUris == null || imageUris.isEmpty()) {
            Toast.makeText(getContext(), "没有图片可用于生成视频", Toast.LENGTH_SHORT).show();
            return;
        }

        isCreatingVideo = true;
        Toast.makeText(getContext(), "开始生成视频...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Starting video creation for testing...");


        // 1. 创建一个临时的输出文件路径 (FFmpeg 会写入这里)
        // 使用应用的缓存目录，FFmpeg 写入这里不需要额外的存储权限 (Android 10+)
        File tempOutputFile = new File(requireContext().getCacheDir(), "temp_export_video_" + UUID.randomUUID().toString() + ".mp4");
        String tempOutputFilePath = tempOutputFile.getAbsolutePath();
        Log.d(TAG, "Temp output file path for FFmpeg: " + tempOutputFilePath);


        // 2. 构建 VideoCreationOptions (使用 Builder)
        int targetWidth = 1280;
        int targetHeight = 720;

        VideoCreationOptions options = new VideoCreationOptions.Builder(imageUris, tempOutputFilePath) // 传入必选参数
                // .setMusicUri(...) // 如果需要背景音乐可以在这里设置
                .setVideoResolution(targetWidth + "x" + targetHeight) // 使用传入的预览区尺寸
                .setImageDisplayDurationMs(2000) // 每张图片显示2秒
                .setTransitionDurationMs(500) // 过渡时长0.5秒
                .setTransitionType(VideoCreationOptions.TransitionType.CROSSFADE) // 使用新的 TransitionType 枚举引用
                .setFrameRate(30) // 帧率
                .setVideoBitrate(2000000) // 2 Mbps 视频码率
                .setAudioBitrate(128000) // 128 Kbps 音频码率
                .build(); // 最后调用 build() 创建对象

        // 3. 调用 FFmpegVideoCreationService 开始创建
        videoCreationService.createVideo(options, new VideoCreationService.VideoCreationCallback() {
            @Override
            public void onProgress(float progress) {
                // FFmpegKit 的回调默认在主线程
                // Log.d(TAG, "Video creation progress: " + (int)(progress * 100) + "%");
                // 可以在这里更新一个 UI 进度条，但为了简单测试，先不实现
            }

            @Override
            public void onSuccess(String tempOutputPath) {
                // FFmpeg 完成后，将临时文件复制到目标目录并通知用户
                Log.d(TAG, "FFmpeg creation success, temp path: " + tempOutputPath);
                // 保存到 MediaStore (DCIM/Memory)
                saveVideoFromTempToMediaStore(tempOutputPath);
                isCreatingVideo = false;
            }

            @Override
            public void onFailure(String errorMessage) {
                // FFmpegKit 的回调默认在主线程
                Log.e(TAG, "Video creation failed: " + errorMessage);
                Toast.makeText(getContext(), "视频生成失败: " + errorMessage, Toast.LENGTH_LONG).show();
                isCreatingVideo = false;

                // 清理 FFmpeg 尝试创建的临时输出文件
                // 使用外部作用域的 tempOutputFilePath 变量
                File tempFile = new File(tempOutputFilePath); // <-- 这里改为 tempOutputFilePath
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.d(TAG, "Deleted temporary output file on failure: " + tempOutputFilePath);
                    } else {
                        Log.w(TAG, "Failed to delete temporary output file on failure: " + tempOutputFilePath);
                    }
                } else {
                    Log.d(TAG, "Temporary output file did not exist on failure: " + tempOutputFilePath);
                }
            }
        });
    }

    /**
     * 将 FFmpeg 生成的临时视频文件复制到 MediaStore (DCIM/Memory 目录) 并通知用户
     * 这是在 onSuccess 回调中调用的
     * @param tempFilePath FFmpeg 生成的临时文件的路径
     */
    private void saveVideoFromTempToMediaStore(String tempFilePath) {
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists()) {
            Log.e(TAG, "Temporary video file does not exist for saving: " + tempFilePath);
            Toast.makeText(getContext(), "保存失败：临时视频文件丢失", Toast.LENGTH_LONG).show();
            return;
        }

        // 目标文件名和相对路径
        String fileName = "exported_memory_" + System.currentTimeMillis() + ".mp4";
        String relativePath = Environment.DIRECTORY_DCIM + File.separator + "Memory"; // 指定保存到 DCIM/Memory

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4"); // MIME 类型
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, relativePath);
            values.put(MediaStore.Video.Media.IS_PENDING, 1); // 标记为待处理，独占访问
        } else {
            // 对于旧版本，需要知道绝对路径，并可能需要 WRITE_EXTERNAL_STORAGE 权限
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Memory");
            if (!directory.exists()) {
                directory.mkdirs(); // 创建目录，可能需要权限
            }
            File finalFile = new File(directory, fileName);
            values.put(MediaStore.Video.Media.DATA, finalFile.getAbsolutePath()); // 使用 DATA 字段
        }

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        Uri newVideoUri = null;
        try {
            // 插入新记录
            newVideoUri = requireContext().getContentResolver().insert(collection, values);
            if (newVideoUri == null) {
                throw new IOException("创建媒体库记录失败");
            }

            // 将临时文件数据写入新的 Uri
            try (OutputStream os = requireContext().getContentResolver().openOutputStream(newVideoUri);
                 FileInputStream is = new FileInputStream(tempFile)) {
                if (os == null) { // 检查 os 是否为 null
                    throw new IOException("获取 OutputStream 失败");
                }
                byte[] buffer = new byte[4096]; // 增大缓冲区
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }

            // 更新 MediaStore 状态 (仅限 Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear(); // 清空旧值
                values.put(MediaStore.Video.Media.IS_PENDING, 0); // 取消待处理标记
                requireContext().getContentResolver().update(newVideoUri, values, null, null);
            } else {
                // 对于旧版本，手动触发媒体扫描，让系统发现新文件
                // 注意：这里如果上面创建目录或文件没有权限会失败
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Memory");
                File finalFile = new File(directory, fileName);
                android.media.MediaScannerConnection.scanFile(
                        requireContext(),
                        new String[]{finalFile.getAbsolutePath()}, // 需要真实文件路径
                        new String[]{"video/mp4"}, // MIME 类型
                        null // 扫描完成回调，这里不需要
                );
            }

            Log.d(TAG, "Video successfully saved to MediaStore: " + newVideoUri.toString());
            Toast.makeText(getContext(), "视频已导出至 DCIM/Memory 目录", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Failed to save video to MediaStore", e);
            Toast.makeText(getContext(), "视频保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // 如果 MediaStore 记录创建了但是写入失败或后续步骤失败，尝试删除那个不完整的记录
            if (newVideoUri != null) {
                try {
                    requireContext().getContentResolver().delete(newVideoUri, null, null);
                } catch (Exception deleteException) {
                    Log.e(TAG, "Failed to delete incomplete MediaStore record", deleteException);
                }
            }
        } finally {
            // 无论保存成功或失败，都尝试删除 FFmpeg 生成的临时文件
            if (tempFile.exists()) {
                if (tempFile.delete()) {
                    Log.d(TAG, "Temporary file deleted: " + tempFilePath);
                } else {
                    Log.w(TAG, "Failed to delete temporary file: " + tempFilePath);
                }
            }
        }
    }


    @Override
    public void onPhotoClick(Uri imageUri) {
        // TODO: 查看单张照片大图
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, PhotoDetailActivity.class);
            // 将 Uri 转换为 String 传递，或者 PhotoDetailActivity 直接接收 Uri
            // intent.putExtra("Uri", imageUri.toString());
            intent.setData(imageUri); // 使用 setData 传递 Uri 更标准
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
        // 销毁视图时取消可能正在进行的 FFmpeg 任务
        if (videoCreationService != null) {
            videoCreationService.cancelCurrentTask();
        }
        // 清理引用
        videoCreationService = null;
        uriToPathHelper = null;
    }

    // ViewModel 提供的空方法，Fragment 自己处理具体逻辑或调用 ViewModel 其他方法
    // public void handleAddPhotos() {}
    // public void handleBatchDelete(List<Uri> selectedItems) {}
    // public void handleExportVideo() {} // 现在 Fragment 内部直接处理

}