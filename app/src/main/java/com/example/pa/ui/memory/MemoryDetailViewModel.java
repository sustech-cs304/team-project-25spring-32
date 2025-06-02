package com.example.pa.ui.memory;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast; // 注意：ViewModel 不直接显示 Toast，但这里为了方便复制 saveVideoFromTempToMediaStore 方法，暂时保留

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.MyApplication;
import com.example.pa.data.FileRepository;
import com.example.pa.util.SingleLiveEvent; // 引入 SingleLiveEvent
import com.example.pa.util.UriToPathHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // 用于生成临时文件名
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; // 用于异步文件操作

public class MemoryDetailViewModel extends ViewModel {

    private static final String TAG = "MemoryDetailViewModel";
    private static final String PREFS_NAME = "MemoryVideoPrefs"; // SharedPreferences 文件名
    private static final String KEY_LAST_VIDEO_URI = "LastVideoUri"; // SharedPreferences Key
    private final MutableLiveData<List<Uri>> photoUris = new MutableLiveData<>();
    private final FileRepository fileRepository;
    private final FFmpegVideoCreationService videoCreationService;
    private final UriToPathHelper uriToPathHelper; // 用于 MediaStore 保存时的路径处理
    private final MutableLiveData<Boolean> _isCreatingVideo = new MutableLiveData<>(false);
    public final LiveData<Boolean> isCreatingVideo = _isCreatingVideo; // 暴露给 Fragment 观察
    private final SingleLiveEvent<String> _toastMessage = new SingleLiveEvent<>();
    public final LiveData<String> toastMessage = _toastMessage; // 暴露给 Fragment 观察 Toast 消息

    // 用于文件操作的线程池，避免阻塞 ViewModel
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Context appContext; // 保存 Context
    private String currentMemoryIdentifier;
    private final SharedPreferences prefs; // SharedPreferences 实例
    private final MutableLiveData<Uri> _currentVideoUri = new MutableLiveData<>(null);
    public final LiveData<Uri> currentVideoUri = _currentVideoUri;

    public MemoryDetailViewModel() {
        this.appContext = MyApplication.getInstance().getApplicationContext();
        this.fileRepository = new FileRepository(appContext);
        this.videoCreationService = new FFmpegVideoCreationService(appContext);
        this.uriToPathHelper = new UriToPathHelper();
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); // 初始化 SharedPreferences
//        loadLastVideoUri(); // 初始化时加载上次的 Uri
    }

    public LiveData<List<Uri>> getPhotoUris() {
        return photoUris;
    }

    public void loadPhotos(String memoryName) {
        List<Uri> sampleUris = fileRepository.getAlbumImages(memoryName);
//        int numImagesToTest = 5;
//        if (sampleUris != null && sampleUris.size() > numImagesToTest) {
//            sampleUris = sampleUris.subList(0, numImagesToTest);
//        }
        photoUris.setValue(sampleUris);
    }

    public void updatePhotos(List<Uri> newUris) {
        photoUris.setValue(newUris);
    }

    /**
     * 触发视频生成 (使用用户定制参数)
     *
     * @param targetWidth    目标宽度
     * @param targetHeight   目标高度
     * @param durationMs     图片显示时长 (毫秒)
     * @param transitionType 转场类型
     * @param frameRate      帧率
     * @param musicUri       音乐 Uri (可选)
     * @param musicVolume    音乐音量 (0.0 - 1.0)
     */
    public void exportVideo(int targetWidth, int targetHeight, int durationMs,
                            TransitionType transitionType, int frameRate, @Nullable Uri musicUri, float musicVolume) { // 添加 musicVolume

        if (Boolean.TRUE.equals(_isCreatingVideo.getValue())) {
            _toastMessage.setValue("视频正在生成中，请稍候...");
            return;
        }

        List<Uri> imageUris = photoUris.getValue();
        if (imageUris == null || imageUris.isEmpty()) {
            _toastMessage.setValue("没有图片可用于生成视频");
            return;
        }

        _isCreatingVideo.setValue(true); // 设置状态为正在生成
        _toastMessage.setValue("开始生成视频...");
        Log.d(TAG, "Starting video creation for testing...");

        // 1. 创建一个临时的输出文件路径 (FFmpeg 会写入这里)
        // 使用应用的缓存目录，FFmpeg 写入这里不需要额外的存储权限 (Android 10+)
        File tempOutputFile = new File(MyApplication.getInstance().getCacheDir(), "temp_export_video_" + UUID.randomUUID().toString() + ".mp4");
        String tempOutputFilePath = tempOutputFile.getAbsolutePath();
        Log.d(TAG, "Temp output file path for FFmpeg: " + tempOutputFilePath);

        // 2. 构建 VideoCreationOptions (使用 Builder)
        VideoCreationOptions.Builder builder = new VideoCreationOptions.Builder(imageUris, tempOutputFilePath)
                .setVideoResolution(targetWidth + "x" + targetHeight)
                .setImageDisplayDurationMs(durationMs)
                .setTransitionType(transitionType)
                .setFrameRate(frameRate)
                .setMusicVolume(musicVolume) // 设置音量
                // 你可以根据需要，让用户也定制转场时长和比特率，或者使用默认值
                .setTransitionDurationMs(500) // 示例: 使用默认值或从定制页获取
                .setVideoBitrate(2000000)      // 示例: 使用默认值或从定制页获取
                .setAudioBitrate(128000);      // 示例: 使用默认值或从定制页获取

        if (musicUri != null) {
            builder.setMusicUri(musicUri);
        }

        VideoCreationOptions options = builder.build();

        // 3. 调用 FFmpegVideoCreationService 开始创建
        videoCreationService.createVideo(options, new VideoCreationService.VideoCreationCallback() {
            @Override
            public void onProgress(float progress) {
                // 可以在这里更新一个 LiveData 来显示进度条
                //例如：_videoCreationProgress.setValue((int)(progress * 100));
            }

            @Override
            public void onSuccess(String tempOutputPath) {
                Log.d(TAG, "FFmpeg creation success, temp path: " + tempOutputPath);
                // 将临时文件复制到 MediaStore 并在后台线程处理
                ioExecutor.execute(() -> saveVideoFromTempToMediaStore(tempOutputPath));
                // 注意：isCreatingVideo 的状态更新放到 saveVideoFromTempToMediaStore 内部的 finally 块中
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Video creation failed: " + errorMessage);
                _toastMessage.postValue("视频生成失败: " + errorMessage); // 使用 postValue 确保在主线程更新 LiveData
                _isCreatingVideo.postValue(false); // 更新状态
                // 清理 FFmpeg 尝试创建的临时输出文件
                File tempFile = new File(tempOutputFilePath);
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
     * 将 FFmpeg 生成的临时视频文件复制到 MediaStore (DCIM/Memory 目录)
     * 这个方法将在后台线程中执行
     *
     * @param tempFilePath FFmpeg 生成的临时文件的路径
     */
    private void saveVideoFromTempToMediaStore(String tempFilePath) {
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists()) {
            Log.e(TAG, "Temporary video file does not exist for saving: " + tempFilePath);
            _toastMessage.postValue("保存失败：临时视频文件丢失"); // 在后台线程更新 LiveData
            _isCreatingVideo.postValue(false);
            return;
        }

        Context context = MyApplication.getInstance().getApplicationContext(); // 获取 Application Context
        if (context == null) {
            Log.e(TAG, "Application Context is null, cannot save video.");
            _toastMessage.postValue("保存失败：应用上下文丢失");
            _isCreatingVideo.postValue(false);
            return;
        }

        // 目标文件名和相对路径
        String fileName = "exported_memory_" + System.currentTimeMillis() + ".mp4";
        String relativePath = Environment.DIRECTORY_DCIM + File.separator + "Memory"; // 指定保存到 DCIM/Memory

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, relativePath);
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        } else {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Memory");
            if (!directory.exists()) {
                // 注意：在Android 10以下，创建目录可能需要 WRITE_EXTERNAL_STORAGE 权限
                boolean created = directory.mkdirs();
                if (!created) {
                    Log.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
                    _toastMessage.postValue("保存失败：无法创建存储目录");
                    _isCreatingVideo.postValue(false);
                    return;
                }
            }
            File finalFile = new File(directory, fileName);
            values.put(MediaStore.Video.Media.DATA, finalFile.getAbsolutePath());
        }

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        Uri newVideoUri = null;
        try {
            newVideoUri = context.getContentResolver().insert(collection, values);
            if (newVideoUri == null) {
                throw new IOException("创建媒体库记录失败");
            }

            try (OutputStream os = context.getContentResolver().openOutputStream(newVideoUri);
                 FileInputStream is = new FileInputStream(tempFile)) {
                if (os == null) {
                    throw new IOException("获取 OutputStream 失败");
                }
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                context.getContentResolver().update(newVideoUri, values, null, null);
            } else {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Memory");
                File finalFile = new File(directory, fileName);
                // 对于旧版本，手动触发媒体扫描
                android.media.MediaScannerConnection.scanFile(
                        context,
                        new String[]{finalFile.getAbsolutePath()},
                        new String[]{"video/mp4"},
                        null
                );
            }

            Log.d(TAG, "Video successfully saved to MediaStore: " + newVideoUri.toString());
            _toastMessage.postValue("视频已导出至 DCIM/Memory 目录"); // 在后台线程更新 LiveData

            // 将最新生成的视频保存下来，用于在 MemoryDeatilFragment展示
            _currentVideoUri.postValue(newVideoUri); // 更新 LiveData
            saveLastVideoUri(newVideoUri);         // 保存到 SharedPreferences

        } catch (Exception e) {
            Log.e(TAG, "Failed to save video to MediaStore", e);
            _toastMessage.postValue("视频保存失败: " + e.getMessage()); // 在后台线程更新 LiveData

            if (newVideoUri != null) {
                try {
                    context.getContentResolver().delete(newVideoUri, null, null);
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
            _isCreatingVideo.postValue(false); // 完成后更新状态
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 当 ViewModel 被销毁时，取消正在进行的 FFmpeg 任务和关闭线程池
        videoCreationService.cancelCurrentTask();
        ioExecutor.shutdownNow(); // 立即关闭线程池
    }

    // 将上次保存的视频 Uri导出
    private void loadLastVideoUriInternal() { // 改为内部方法
        if (currentMemoryIdentifier == null || currentMemoryIdentifier.isEmpty()) {
            Log.w(TAG, "Cannot load last video URI, memory identifier is not set.");
            _currentVideoUri.postValue(null); // 没有标识符则不加载，或设置为null
            return;
        }
        String dynamicKey = KEY_LAST_VIDEO_URI + "_" + currentMemoryIdentifier; // 动态生成键名
        String uriString = prefs.getString(dynamicKey, null);
        if (uriString != null) {
            _currentVideoUri.postValue(Uri.parse(uriString));
            Log.d(TAG, "Loaded last video URI for " + currentMemoryIdentifier + ": " + uriString);
        } else {
            _currentVideoUri.postValue(null); // 确保 LiveData 更新为 null
            Log.d(TAG, "No last video URI found for " + currentMemoryIdentifier);
        }
    }

    // 在生成视频之后，将最新的 Uri保存至 prefs
    private void saveLastVideoUri(Uri videoUri) {
        if (currentMemoryIdentifier == null || currentMemoryIdentifier.isEmpty()) {
            Log.w(TAG, "Cannot save last video URI, memory identifier is not set.");
            return;
        }
        String dynamicKey = KEY_LAST_VIDEO_URI + "_" + currentMemoryIdentifier; // 动态生成键名
        if (videoUri != null) {
            prefs.edit().putString(dynamicKey, videoUri.toString()).apply();
            Log.d(TAG, "Saved last video URI for " + currentMemoryIdentifier + ": " + videoUri.toString());
        } else {
            prefs.edit().remove(dynamicKey).apply();
            Log.d(TAG, "Removed last video URI for " + currentMemoryIdentifier);
        }
    }

    // 新增一个初始化方法或修改现有加载方法来设置标识符并加载数据
    public void loadMemoryDetails(String memoryIdentifier) {
        if (memoryIdentifier == null || memoryIdentifier.isEmpty()) {
            Log.e(TAG, "Memory identifier cannot be null or empty.");
            // 可以清除旧数据或显示错误
            photoUris.setValue(new ArrayList<>()); // 清空图片
            _currentVideoUri.setValue(null);       // 清空视频 URI
            return;
        }
        this.currentMemoryIdentifier = memoryIdentifier;
        Log.d(TAG, "Loading details for memory: " + memoryIdentifier);
        loadPhotos(memoryIdentifier);       // 加载相册图片
        loadLastVideoUriInternal(); // 加载该相册对应的上次播放的视频 URI
    }
}