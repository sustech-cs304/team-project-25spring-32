package com.example.pa.ui.memory;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.example.pa.util.UriToPathHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI-generated-content
 * tool: Gemini
 * version: 2.5 Pro
 * usage: I want Gemini to give me the structure about how to use the library Ffmpeg to generate video from pictures
 * I slightly adapt the generated code by modifying the implement details
 */

public class FFmpegVideoCreationService implements VideoCreationService {

    private static final String TAG = "FFmpegVideoService";
    private final Context context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 在后台线程执行 Ffmpeg
    private FFmpegSession currentSession;
    // 用于跟踪临时文件以便清理。主要跟踪由 UriToPathHelper 创建的临时输入文件。
    private final List<String> tempFilePaths = new ArrayList<>();


    public FFmpegVideoCreationService(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void createVideo(@NonNull VideoCreationOptions options, @NonNull VideoCreationCallback callback) {
        // Options 参数检查
        if (options.getImageUris() == null || options.getImageUris().isEmpty()) {
            Log.e(TAG, "Image URIs list is null or empty in options.");
            callback.onFailure("没有提供图片，无法创建视频。");
            return;
        }

        // 确保上一个任务（如果有）已取消或完成
        if (currentSession != null) {
            SessionState state = currentSession.getState();
            // 检查会话是否仍在运行或处于其他非终结状态
            if (state == SessionState.RUNNING || state == SessionState.CREATED) { // CREATING 是一个短暂的初始状态
                Log.w(TAG, "Another video creation task is running or created (session ID: " + currentSession.getSessionId() + ").");
                callback.onFailure("另一个视频创建任务正在进行中。");
                return;
            }
        }

        // 清理之前的临时输入文件 (上次任务遗留的)
        clearTemporaryFiles();
        tempFilePaths.clear(); // 清空列表，准备添加本次任务的临时输入文件

        // 在后台线程执行 Ffmpeg,避免阻塞
        executorService.submit(() -> {
            // 使用 Ffmpeg生成视频需要使用路径
            // === 遍历 Uri转化为路径 ===
            try {
                List<String> imageFilePaths = new ArrayList<>();
                for (int i = 0; i < options.getImageUris().size(); i++) {
                    Uri uri = options.getImageUris().get(i);
                    String path = UriToPathHelper.getPathFromUri(context, uri); // UriToPathHelper 负责将 URI 转为路径，并可能创建临时文件

                    if (path != null) {
                        imageFilePaths.add(path);
                        Log.d(TAG, "Resolved image path for input " + i + ": " + path);
                        tempFilePaths.add(path);
                    } else {
                        Log.e(TAG, "Failed to get path for image URI at index " + i + ": " + uri);
                        callback.onFailure("无法处理其中一张图片 (索引 " + i + "): " + uri.toString());
                        clearTemporaryFiles(); // 清理已创建的临时输入文件
                        return; // 停止任务
                    }
                }

                if (imageFilePaths.isEmpty()) {
                    // Redundant check, but harmless
                    Log.e(TAG, "Image file paths list is unexpectedly empty after processing URIs.");
                    callback.onFailure("没有有效的图片输入路径。");
                    clearTemporaryFiles();
                    return;
                }

                // === 处理音乐 URI ===
                String musicFilePath = null;
                if (options.getMusicUri() != null) {
                    musicFilePath = UriToPathHelper.getPathFromUri(context, options.getMusicUri());
                    if (musicFilePath == null) {
                        // 没有音乐则静音
                        Log.w(TAG, "无法获取音乐文件路径: " + options.getMusicUri() + ". 无背景音乐。");
                    } else {
                        Log.d(TAG, "Resolved music path: " + musicFilePath);
                        // 假设 UriToPathHelper 创建了临时文件并返回其路径，则添加到清理列表
                        tempFilePaths.add(musicFilePath);
                    }
                }

                // === 构建 FFmpeg 命令 ===
                String ffmpegCommand = buildFFmpegCommand(imageFilePaths, musicFilePath, options);
                Log.d(TAG, "Executing FFmpeg command: " + ffmpegCommand);

                final AtomicLong totalDuration = new AtomicLong(calculateTotalVideoDurationMillis(imageFilePaths.size(), options));

                // === 执行 FFmpeg 命令 ===
                currentSession = FFmpegKit.executeAsync(ffmpegCommand, session -> {
                    ReturnCode returnCode = session.getReturnCode();

                    // === Debug: 打印 FFmpeg 自身的详细输出和状态 ===
                    Log.d(TAG, "--- FFmpeg process finished ---");
                    Log.d(TAG, "Session ID: " + session.getSessionId());
                    Log.d(TAG, "State: " + session.getState());
                    Log.d(TAG, "Return Code: " + returnCode);
                    if (session.getFailStackTrace() != null && !session.getFailStackTrace().isEmpty()) {
                        Log.e(TAG, "Fail Stack Trace:\n" + session.getFailStackTrace());
                    }
                    Log.d(TAG, "FFmpeg Output Log:\n" + session.getOutput());
                    Log.d(TAG, "-----------------------------");

                    // 返回值
                    if (ReturnCode.isSuccess(returnCode)) {
                        Log.i(TAG, "FFmpeg command execution successful. Output: " + options.getOutputFilePath());
                        // 任务成功，调用外部回调
                        callback.onSuccess(options.getOutputFilePath()); // 将预期的输出路径传回
                    } else {
                        // 任务失败，构建详细错误信息
                        String failureMessage = String.format(Locale.US, "FFmpeg command failed.%nState: %s, Return Code: %s.",
                                session.getState(), returnCode);
                        if (session.getFailStackTrace() != null && !session.getFailStackTrace().isEmpty()) {
                            failureMessage += "\nDetail: " + session.getFailStackTrace().trim(); // 添加精简的失败堆栈
                        }

                        Log.e(TAG, "Video creation failed. " + failureMessage);
                        // 调用外部回调
                        callback.onFailure(failureMessage);
                    }

                    // 清理本次任务创建的临时输入文件 (Service 负责清理输入，Fragment 负责清理输出)
                    clearTemporaryFiles();
                    currentSession = null; // 重置会话状态
                }, log -> {
                    Log.v(TAG, "FFmpeg log: " + log.getMessage());
                }, statistics -> {
                    // === 进度统计回调 ===
                    // FFmpeg 进度统计，用于更新进度条等 UI。
                    if (totalDuration.get() > 0 && statistics.getTime() >= 0) { // statistics.getTime() 是已处理时长 (ms)
                        float progress = (float) statistics.getTime() / totalDuration.get();
                        callback.onProgress(Math.min(1.0f, progress)); // 确保不超过1.0
                    }
                    Log.v(TAG, "FFmpeg statistics: time=" + statistics.getTime() + ", speed=" + statistics.getSpeed());
                });

            } catch (Exception e) {
                // === 捕获 FFmpegKit.executeAsync 调用之前或其本身抛出的异常 ===
                Log.e(TAG, "Error during video creation process setup or calling FFmpeg.", e);
                callback.onFailure("视频创建过程中发生错误: " + e.getMessage());
                clearTemporaryFiles(); // 清理已创建的临时输入文件
            }
        });
    }

    private long calculateTotalVideoDurationMillis(int numImages, VideoCreationOptions options) {
        if (numImages <= 0) return 0; // Modified check for clarity

        long imageDisplayMs = options.getImageDisplayDurationMs();
        long transitionMs = options.getTransitionDurationMs();

        // 如果图片显示时间太短
        if (imageDisplayMs <= 2 * transitionMs && numImages > 1) {
            imageDisplayMs = 2 * transitionMs + 100; // 前后切换的时间 + 0.1秒
            Log.w(TAG, "Image display duration (" + options.getImageDisplayDurationMs() + "ms) <= transition duration (" + options.getTransitionDurationMs() + "ms). Adjusted image display for calculation to " + imageDisplayMs + "ms.");
        } else if (imageDisplayMs <= 0) {
            imageDisplayMs = 3000; // 默认3秒
            Log.w(TAG, "Image display duration was <= 0. Defaulting to " + imageDisplayMs + "ms.");
        }
        if (transitionMs < 0) {
            transitionMs = 0;
            Log.w(TAG, "Transition duration was negative. Defaulting to " + transitionMs + "ms.");
        }


        long totalDurationMs;
        if (numImages == 1) {
            // 只有一张图片
            totalDurationMs = imageDisplayMs;
        } else {
            long effectiveSubsequentDuration = imageDisplayMs - transitionMs;
            // Ensure effective subsequent duration is not negative (already handled by imageDisplayMs adjustment above, but double check)
            if (effectiveSubsequentDuration <= 0)
                effectiveSubsequentDuration = 1; // Ensure at least 1ms contribution
            totalDurationMs = imageDisplayMs + (numImages - 1) * effectiveSubsequentDuration;
        }

        // 最终检查，确保总时长为正且不为 0
        if (totalDurationMs <= 0) {
            totalDurationMs = imageDisplayMs > 0 ? imageDisplayMs : 1000; // Fallback
            Log.w(TAG, "Calculated total duration was <= 0. Adjusting to " + totalDurationMs + "ms.");
        }

        return totalDurationMs;
    }


    private String buildFFmpegCommand(List<String> imagePaths, String musicPath, VideoCreationOptions options) {
        StringBuilder command = new StringBuilder();
        int numImages = imagePaths.size();

        // 计算调整后的时长 (确保图片显示时间足够进行过渡)
        double imageDurationSec = options.getImageDisplayDurationMs() / 1000.0;
        double transitionDurationSec = options.getTransitionDurationMs() / 1000.0;

        // 确保图片显示时间合理地长于过渡时间，以避免FFmpeg内部问题
        if (numImages > 1) {
            if (imageDurationSec <= transitionDurationSec) {
                imageDurationSec = transitionDurationSec + 0.1; // 必须比过渡时间长一点
                Log.w(TAG, "Image display duration was too short for transition. Adjusted to " + String.format(Locale.US, "%.3f", imageDurationSec) + "s");
            }
        }
        // 确保时长为正
        if (imageDurationSec <= 0) {
            imageDurationSec = 1.0;
            Log.w(TAG, "Image duration was <= 0. Defaulting to 1.0s.");
        }
        if (transitionDurationSec < 0) {
            transitionDurationSec = 0;
            Log.w(TAG, "Transition duration was negative. Defaulting to 0s.");
        }

        String[] resolutionParts;
        String targetWidth;
        String targetHeight;
        try {
            resolutionParts = options.getVideoResolution().split("x");
            targetWidth = resolutionParts[0].trim();
            targetHeight = resolutionParts[1].trim();
            // Basic format validation
            Integer.parseInt(targetWidth);
            Integer.parseInt(targetHeight);
        } catch (Exception e) {
            Log.e(TAG, "Invalid video resolution format: " + options.getVideoResolution(), e);
            throw new IllegalArgumentException("Invalid video resolution format: " + options.getVideoResolution(), e);
        }


        // --- 1. 输入图片和音频 ---
        for (String imagePath : imagePaths) {
            if (imagePath == null || !new File(imagePath).exists()) {
                Log.e(TAG, "Input image file not found or invalid path: " + imagePath);
                throw new IllegalArgumentException("Input image file not found: " + imagePath);
            }
            command.append("-loop 1 -t ").append(String.format(Locale.US, "%.3f", imageDurationSec))
                    .append(" -i \"").append(imagePath).append("\" ");
        }

        boolean hasMusicInput = (musicPath != null && new File(musicPath).exists());
        if (hasMusicInput) {
            command.append("-stream_loop -1 -i \"").append(musicPath).append("\" ");
        } else {
            Log.d(TAG, "No valid music path provided or file not found. Adding silent audio track.");
            command.append("-f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 ");
        }


        // --- 2. 构建 filter_complex (视频滤镜图) ---
        command.append("-filter_complex \"");

        // 2a. 处理每张图片：缩放并填充到目标分辨率
        for (int i = 0; i < numImages; i++) {
            command.append(String.format(Locale.US, "[%d:v]setpts=PTS-STARTPTS,scale=%s:%s:force_original_aspect_ratio=decrease:eval=frame,",
                    i, targetWidth, targetHeight));
            command.append(String.format(Locale.US, "pad=%s:%s:-1:-1:color=black[v%d]",
                    targetWidth, targetHeight, i));
            if (i < numImages - 1) {
                command.append(";");
            }
        }

        // 2b. 应用过渡效果 (xfade) 或处理单张图片情况
        if (numImages > 1) {
            command.append(";"); // 分隔图片处理链和 xfade 链

            String prevStream = "v0";
            // 累积偏移量
            double accumulatedOffset = 0.0;

            for (int i = 0; i < numImages - 1; i++) {
                String currentStream = "v" + (i + 1);
                String outputStreamLabel = (i == numImages - 2) ? "final_video" : "vf" + i;

                // 计算当前 xfade 的偏移量
                if (i == 0) {
                    accumulatedOffset = imageDurationSec - transitionDurationSec;
                } else {
                    accumulatedOffset += (imageDurationSec - transitionDurationSec);
                }

                // 确保偏移量不为负
                if (accumulatedOffset < 0) {
                    accumulatedOffset = 0;
                }

                command.append(String.format(Locale.US, "[%s][%s]xfade=transition=%s:duration=%.3f:offset=%.3f[%s]",
                        prevStream,
                        currentStream,
                        options.getTransitionType().getFfmpegFilterName(),
                        transitionDurationSec,
                        accumulatedOffset, // 使用累积的动态偏移量
                        outputStreamLabel
                ));

                if (i < numImages - 2) {
                    command.append(";"); // 链式 xfade 操作之间的分号
                }
                prevStream = outputStreamLabel;
            }
        } else { // Only one image (numImages == 1)
            command.append(";[v0]format=yuv420p[final_video]");
        }

        command.append("\" "); // 关闭 filter_complex 字符串


        // --- 3. 映射流 ---
        command.append("-map \"[final_video]\" ");
        command.append("-map ").append(numImages).append(":a "); // 音频输入的索引是 numImages


        // --- 4. 输出设置 ---
        double finalVideoDurationSec = calculateTotalVideoDurationMillis(numImages, options) / 1000.0;
        if (finalVideoDurationSec <= 0 && numImages > 0) {
            finalVideoDurationSec = imageDurationSec > 0 ? imageDurationSec : 1.0;
            Log.w(TAG, "Calculated final video duration was <= 0. Adjusting to " + String.format(Locale.US, "%.3f", finalVideoDurationSec) + "s.");
        } else if (finalVideoDurationSec <= 0) {
            finalVideoDurationSec = 0.1;
        }

        command.append("-c:v libx264 -preset ultrafast ")
                .append("-profile:v baseline -level 3.0 ")
                .append("-r ").append(options.getFrameRate()).append(" ")
                .append("-b:v ").append(options.getVideoBitrate()).append(" ")
                .append("-c:a aac ");

        if (hasMusicInput) {
            command.append("-b:a ").append(options.getAudioBitrate()).append(" ");
            float musicVolume = options.getMusicVolume(); // 获取音量
            if (musicVolume > 0.0f && musicVolume < 1.0f) {
                command.append("-af \"volume=").append(String.format(Locale.US, "%.2f", musicVolume)).append("\" ");
            }
        } else {
            command.append("-b:a 128000 ");
        }

        command.append("-pix_fmt yuv420p ")
                .append("-t ").append(String.format(Locale.US, "%.3f", finalVideoDurationSec)).append(" ");

        command.append("-y \"").append(options.getOutputFilePath()).append("\"");

        return command.toString();
    }

    @Override
    public void cancelCurrentTask() {
        if (currentSession != null) {
            SessionState state = currentSession.getState();
            // 检查会话是否仍在运行或处于其他非终结状态
            if (state == SessionState.RUNNING || state == SessionState.CREATED) { // CREATING 是一个短暂的初始状态
                Log.d(TAG, "Cancelling FFmpeg session: " + currentSession.getSessionId());
                FFmpegKit.cancel(currentSession.getSessionId());
            } else {
                Log.d(TAG, "No active FFmpeg session to cancel or session is already finished. State: " + state);
            }
        } else {
            Log.d(TAG, "No current FFmpeg session to cancel.");
        }
        clearTemporaryFiles(); // 取消时也清理临时输入文件
    }

    /**
     * 清理由 UriToPathHelper 创建的临时输入文件。
     * 只删除位于应用的缓存目录且以 "temp_" 开头的文件，以确保安全。
     * 不负责清理 FFmpeg 生成的临时输出文件，那应该由调用者 (Fragment/ViewModel) 处理。
     */
    private void clearTemporaryFiles() {
        Log.d(TAG, "Attempting to clean up " + tempFilePaths.size() + " temporary input files.");
        for (String path : tempFilePaths) {
            if (path == null) continue; // Skip null paths

            File file = new File(path);
            // 检查文件是否存在，并且是在应用的缓存目录，并且文件名以 "temp_" 开头
            // 这个检查很重要，防止误删原始文件
            if (file.exists() && file.getParentFile() != null && file.getParentFile().equals(context.getCacheDir()) && file.getName().startsWith("temp_")) {
                if (file.delete()) {
                    Log.d(TAG, "Deleted temporary input file: " + path);
                } else {
                    Log.w(TAG, "Failed to delete temporary input file: " + path);
                }
            } else {
                // 如果文件不存在，或者不在缓存目录/不是temp_开头，则不删除，可能是原始文件或不应清理
                Log.d(TAG, "Skipping cleanup for file (not temp or not in cache): " + path);
            }
        }
        tempFilePaths.clear(); // List is cleared at the beginning of createVideo
    }
}