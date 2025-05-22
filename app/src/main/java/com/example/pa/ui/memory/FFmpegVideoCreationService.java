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
import com.example.pa.util.UriToPathHelper; // 确保导入 UriToPathHelper

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class FFmpegVideoCreationService implements VideoCreationService {

    private static final String TAG = "FFmpegVideoService";
    private final Context context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 在后台线程执行 FFmpeg
    private FFmpegSession currentSession;
    // 用于跟踪临时文件以便清理。主要跟踪由 UriToPathHelper 创建的临时输入文件。
    private final List<String> tempFilePaths = new ArrayList<>();


    public FFmpegVideoCreationService(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void createVideo(@NonNull VideoCreationOptions options, @NonNull VideoCreationCallback callback) {
        // === 增加对 Options 参数的检查 ===
        if (options == null) {
            Log.e(TAG, "Video creation options are null.");
            callback.onFailure("视频创建选项为空。");
            return;
        }
        if (options.getImageUris() == null || options.getImageUris().isEmpty()) {
            Log.e(TAG, "Image URIs list is null or empty in options.");
            callback.onFailure("没有提供图片，无法创建视频。");
            return;
        }
        // === 检查结束 ===


        // 确保上一个任务（如果有）已取消或完成
        if (currentSession != null) {
            SessionState state = currentSession.getState();
            // 检查会话是否仍在运行或处于其他非终结状态
            if (state == SessionState.RUNNING || state == SessionState.CREATED) { // CREATING 是一个短暂的初始状态
                Log.w(TAG, "Another video creation task is running or created (session ID: " + currentSession.getSessionId() + ").");
                callback.onFailure("另一个视频创建任务正在进行中。");
                return;
            }
            // 如果会话已完成、失败或取消，则可以安全地认为它已结束，可以开始新任务
        }

        // 清理之前的临时输入文件 (上次任务遗留的)
        clearTemporaryFiles();
        tempFilePaths.clear(); // 清空列表，准备添加本次任务的临时输入文件

        executorService.submit(() -> {
            try {
                List<String> imageFilePaths = new ArrayList<>();
                // === 遍历并处理图片 URI ===
                for (int i = 0; i < options.getImageUris().size(); i++) {
                    Uri uri = options.getImageUris().get(i);
                    String path = UriToPathHelper.getPathFromUri(context, uri); // UriToPathHelper 负责将 URI 转为路径，并可能创建临时文件

                    if (path != null) {
                        imageFilePaths.add(path);
                        // 假设 UriToPathHelper 创建了临时文件并返回其路径，则添加到清理列表
                        // UriToPathHelper 应返回一个明确的标记或在特定目录下创建临时文件
                        // 这里的逻辑依赖于 clearTemporaryFiles 中的判断 (目录和前缀) 来安全清理
                        Log.d(TAG, "Resolved image path for input " + i + ": " + path);
                        tempFilePaths.add(path);

                    } else {
                        Log.e(TAG, "Failed to get path for image URI at index " + i + ": " + uri);
                        callback.onFailure("无法处理其中一张图片 (索引 " + i + "): " + uri.toString());
                        clearTemporaryFiles(); // 清理已创建的临时输入文件
                        return; // 停止任务
                    }
                }

                // 理论上这里的 imageFilePaths 不会是空的，因为前面已经检查 options.getImageUris() 不为空
                if (imageFilePaths.isEmpty()) {
                    // Redundant check, but harmless
                    Log.e(TAG, "Image file paths list is unexpectedly empty after processing URIs.");
                    callback.onFailure("没有有效的图片输入路径。");
                    clearTemporaryFiles();
                    return;
                }
                // === 图片 URI 处理结束 ===


                // === 处理音乐 URI ===
                String musicFilePath = null;
                if (options.getMusicUri() != null) {
                    musicFilePath = UriToPathHelper.getPathFromUri(context, options.getMusicUri());
                    if (musicFilePath == null) {
                        Log.w(TAG, "无法获取音乐文件路径: " + options.getMusicUri() + ". 将继续 بدون 背景音乐。");
                        // 设计选择：无法获取音乐路径时继续 without 音乐
                    } else {
                        Log.d(TAG, "Resolved music path: " + musicFilePath);
                        // 假设 UriToPathHelper 创建了临时文件并返回其路径，则添加到清理列表
                        tempFilePaths.add(musicFilePath);
                    }
                }
                // === 音乐 URI 处理结束 ===

                // === 构建 FFmpeg 命令 ===
                String ffmpegCommand = buildFFmpegCommand(imageFilePaths, musicFilePath, options);
                Log.d(TAG, "Executing FFmpeg command: " + ffmpegCommand);
                // === FFmpeg 命令构建结束 ===


                final AtomicLong totalDuration = new AtomicLong(calculateTotalVideoDurationMillis(imageFilePaths.size(), options));

                // === 执行 FFmpeg 命令 ===
                currentSession = FFmpegKit.executeAsync(ffmpegCommand, session -> {
                    ReturnCode returnCode = session.getReturnCode();

                    // === 无论成功或失败，先打印 FFmpeg 自身的详细输出和状态，这是最重要的调试信息 ===
                    Log.d(TAG, "--- FFmpeg process finished ---");
                    Log.d(TAG, "Session ID: " + session.getSessionId());
                    Log.d(TAG, "State: " + session.getState());
                    Log.d(TAG, "Return Code: " + returnCode);
                    if (session.getFailStackTrace() != null && !session.getFailStackTrace().isEmpty()) {
                        Log.e(TAG, "Fail Stack Trace:\n" + session.getFailStackTrace());
                    }
                    Log.d(TAG, "FFmpeg Output Log:\n" + session.getOutput());
                    Log.d(TAG, "-----------------------------");


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
                        // FFmpeg 自身的输出日志已经在上面打印了，这里不再重复添加到 message String 中，避免过长

                        Log.e(TAG, "Video creation failed. " + failureMessage);
                        // 调用外部回调
                        callback.onFailure(failureMessage);
                    }
                    // === FFmpeg 执行结束处理 ===

                    // 清理本次任务创建的临时输入文件 (Service 负责清理输入，Fragment 负责清理输出)
                    clearTemporaryFiles();
                    currentSession = null; // 重置会话状态
                }, log -> {
                    // === 实时日志回调 ===
                    // FFmpeg 的实时日志输出，非常详细。可以用于更新更精细的进度或显示详细过程。
                    // 使用 VERBOSE 级别，调试时方便开启，发布时自动忽略大部分日志。
                    // Log.v(TAG, "FFmpeg log: " + log.getMessage());
                    // === 实时日志回调结束 ===
                }, statistics -> {
                    // === 进度统计回调 ===
                    // FFmpeg 进度统计，用于更新进度条等 UI。
                    if (totalDuration.get() > 0 && statistics.getTime() >= 0) { // statistics.getTime() 是已处理时长 (ms)
                        float progress = (float) statistics.getTime() / totalDuration.get();
                        callback.onProgress(Math.min(1.0f, progress)); // 确保不超过1.0
                    }
                    // Log.v(TAG, "FFmpeg statistics: time=" + statistics.getTime() + ", speed=" + statistics.getSpeed());
                    // === 进度统计回调结束 ===
                });
                // === FFmpeg 命令执行结束 ===

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

        // 与 buildFFmpegCommand 中调整 imageDurationSec 的逻辑保持一致
        // 如果图片显示时间太短，确保它至少比过渡长一点点，避免FFmpeg问题
        if (imageDisplayMs <= transitionMs && numImages > 1) {
            imageDisplayMs = transitionMs + 100; // 至少多0.1秒
            Log.w(TAG, "Image display duration (" + options.getImageDisplayDurationMs() + "ms) <= transition duration (" + options.getTransitionDurationMs() + "ms). Adjusted image display for calculation to " + imageDisplayMs + "ms.");
        } else if (imageDisplayMs <= 0) { // Ensure image display is positive
            imageDisplayMs = 1000; // Default to 1 second if <= 0
            Log.w(TAG, "Image display duration was <= 0. Defaulting to " + imageDisplayMs + "ms.");
        }
        if (transitionMs < 0) { // Ensure transition is not negative
            transitionMs = 0; // Default to 0 transition
            Log.w(TAG, "Transition duration was negative. Defaulting to " + transitionMs + "ms.");
        }


        long totalDurationMs;
        if (numImages > 1) {
            // 总时长 = 第一张图片的完整显示时长 + (图片数量 - 1) * (后续每张图片的有效贡献时长)
            // 后续每张图片的有效贡献时长 = 图片显示时长 - 过渡时长
            long effectiveSubsequentDuration = imageDisplayMs - transitionMs;
            // Ensure effective subsequent duration is not negative (already handled by imageDisplayMs adjustment above, but double check)
            if (effectiveSubsequentDuration <= 0)
                effectiveSubsequentDuration = 1; // Ensure at least 1ms contribution

            totalDurationMs = imageDisplayMs + (numImages - 1) * effectiveSubsequentDuration;
        } else { // 只有一张图片
            totalDurationMs = imageDisplayMs;
        }

        // 最终检查，确保总时长为正且不为 0
        if (totalDurationMs <= 0 && numImages > 0) {
            totalDurationMs = imageDisplayMs > 0 ? imageDisplayMs : 1000; // Fallback
            Log.w(TAG, "Calculated total duration was <= 0. Adjusting to " + totalDurationMs + "ms.");
        } else if (totalDurationMs <= 0) { // No images case already handled, but safeguard
            totalDurationMs = 100; // At least 0.1 second for empty case
        }

        return totalDurationMs;
    }


    private String buildFFmpegCommand(List<String> imagePaths, String musicPath, VideoCreationOptions options) {
        StringBuilder command = new StringBuilder();
        int numImages = imagePaths.size();

        // 1. 计算调整后的时长 (确保图片显示时间足够进行过渡)
        double imageDurationSec = options.getImageDisplayDurationMs() / 1000.0;
        double transitionDurationSec = options.getTransitionDurationMs() / 1000.0;
        // Ensure image duration is reasonable relative to transition for chaining
        if (numImages > 1) {
            if (imageDurationSec <= transitionDurationSec) {
                imageDurationSec = transitionDurationSec + 0.1; // Must be > transition duration for offset calculation
                Log.w(TAG, "Image display duration was too short for transition. Adjusted to " + String.format(Locale.US, "%.3f", imageDurationSec) + "s");
            }
        }
        // Ensure durations are positive
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

        // 输入图片，使用 loop 和 -t 指定每张图片的显示时长
        for (String imagePath : imagePaths) {
            // 检查图片路径是否存在 (尽管 UriToPathHelper 应该处理了，但这是 FFmpeg 的输入，再检查一次更安全)
            if (imagePath == null || !new File(imagePath).exists()) {
                Log.e(TAG, "Input image file not found or invalid path: " + imagePath);
                // 注意：这里直接抛异常会中断任务，并被外层 catch 捕获
                throw new IllegalArgumentException("Input image file not found: " + imagePath);
            }
            command.append("-loop 1 -t ").append(String.format(Locale.US, "%.3f", imageDurationSec))
                    .append(" -i \"").append(imagePath).append("\" "); // 路径加双引号，防止空格等特殊字符
        }

        // 输入音乐 (如果提供)
        boolean hasMusicInput = (musicPath != null && new File(musicPath).exists());
        if (hasMusicInput) {
            command.append("-stream_loop -1 -i \"").append(musicPath).append("\" "); // -stream_loop -1: 无限循环音频
        } else {
            // 如果没有背景音乐，添加静音音轨，避免某些播放器出现问题
            Log.d(TAG, "No valid music path provided or file not found. Adding silent audio track.");
            command.append("-f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 "); // 添加静音音轨 input
        }


        // --- 2. 构建 filter_complex (视频滤镜图) ---

        command.append("-filter_complex \"");

        // 2a. 处理每张图片：缩放、填充、添加 Alpha 通道 (即使原图没有，为 xfade 准备)
        for (int i = 0; i < numImages; i++) {
            // [i:v] selects video stream from input i
            // setpts=PTS-STARTPTS: reset timestamps for each input
            // scale=w:h:force_original_aspect_ratio=decrease: scale image while maintaining aspect ratio, fit within wxh
            // eval=frame: evaluate scale expression for each frame (needed with force_original_aspect_ratio)
            // split: create copies for pad and alpha processing
            command.append(String.format(Locale.US, "[%d:v]setpts=PTS-STARTPTS,scale=%s:%s:force_original_aspect_ratio=decrease:eval=frame,split[base%d][alpha%d];",
                    i, targetWidth, targetHeight, i, i));
            // [base%d]pad=w:h:-1:-1:color=black: pad the scaled image to target wxh, centering it with black borders
            command.append(String.format(Locale.US, "[base%d]pad=%s:%s:-1:-1:color=black[scaled%d];",
                    i, targetWidth, targetHeight, i));
            // [alpha%d]alphaextract: extract the alpha channel (produces a grayscale representation of alpha)
            command.append(String.format(Locale.US, "[alpha%d]alphaextract[a%d];", i, i));
            // [scaled%d][a%d]alphamerge: merge the video stream with the extracted alpha channel (creates a stream with alpha)
            command.append(String.format(Locale.US, "[scaled%d][a%d]alphamerge[v%d]", i, i, i)); // Output stream [vX] after processing

            // *** BUG FIX 1: Add semicolon BETWEEN image processing chains ***
            if (i < numImages - 1) {
                command.append(";"); // Add semicolon if this is NOT the last image processing chain
            }
        }

        // 2b. 应用过渡效果 (xfade) 或处理单张图片情况
        if (numImages > 1) {
            // This semicolon separates the last image processing filter chain (e.g., ...[vN-1])
            // from the beginning of the xfade filter chain. This is correct.
            command.append(";");

            String prevStream = "v0";

            // *** CORRECTED: Calculate the actual xfade filter offset ONCE ***
            // This offset is relative to the start of 'prevStream' for EACH xfade operation.
            double actualXfadeFilterParameterOffset = imageDurationSec - transitionDurationSec;
            if (actualXfadeFilterParameterOffset < 0) {
                // This case should ideally be prevented by the earlier adjustment where
                // imageDurationSec is made transitionDurationSec + 0.1 if it was too short.
                // So, actualXfadeFilterParameterOffset should be at least 0.1 or positive.
                actualXfadeFilterParameterOffset = 0;
                Log.w(TAG, "Calculated xfade filter offset was negative, set to 0. Check image/transition duration logic.");
            }

            for (int i = 0; i < numImages - 1; i++) {
                String currentStream = "v" + (i + 1);
                String outputStreamLabel = (i == numImages - 2) ? "final_video" : "vf" + i;

                // Now use the 'actualXfadeFilterParameterOffset' for all xfades
                command.append(String.format(Locale.US, "[%s][%s]xfade=transition=%s:duration=%.3f:offset=%.3f[%s]",
                        prevStream,
                        currentStream,
                        options.getTransitionType().getFfmpegFilterName(), // Assuming this correctly returns "fade", "dissolve", etc.
                        transitionDurationSec,
                        actualXfadeFilterParameterOffset, // <<< USE THE CORRECTED, CONSTANT OFFSET
                        outputStreamLabel
                ));

                if (i < numImages - 2) {
                    command.append(";"); // Semicolon between chained xfade operations
                }
                prevStream = outputStreamLabel;
            }
        } else { // Only one image (numImages == 1)
            // Your existing logic: command.append(";[v0]format=yuv420p[final_video]"); is correct.
            command.append(";[v0]format=yuv420p[final_video]");
        }

        command.append("\" "); // Close the filter_complex string


        // --- 3. 映射流 ---

        // 映射最终视频流，它在 filter_complex 中被命名为 [final_video]
        command.append("-map \"[final_video]\" ");

        // 映射音频流
        // 音频输入的索引是 numImages (因为图片输入占用了从 0 到 numImages-1 的索引)
        command.append("-map ").append(numImages).append(":a ");


        // --- 4. 输出设置 ---

        // 计算最终视频总时长 (与 calculateTotalVideoDurationMillis 保持一致)
        double finalVideoDurationSec = calculateTotalVideoDurationMillis(numImages, options) / 1000.0;
        // Ensure duration is positive and not zero
        if (finalVideoDurationSec <= 0 && numImages > 0) {
            finalVideoDurationSec = imageDurationSec > 0 ? imageDurationSec : 1.0; // Fallback
            Log.w(TAG, "Calculated final video duration was <= 0. Adjusting to " + String.format(Locale.US, "%.3f", finalVideoDurationSec) + "s.");
        } else if (finalVideoDurationSec <= 0) { // No images case, fallback to a small duration
            finalVideoDurationSec = 0.1;
        }

        command.append("-c:v libx264 -preset ultrafast ") // 视频编码器和预设 (ultrafast 速度快，但文件大质量低，可按需调整)
                .append("-profile:v baseline -level 3.0 ") // H.264 配置文件和级别，增强兼容性
                .append("-r ").append(options.getFrameRate()).append(" ") // 帧率
                .append("-b:v ").append(options.getVideoBitrate()).append(" ") // 视频码率 (bytes per second)
                .append("-c:a aac "); // 音频编码器 (AAC 是标准选择)

        if (hasMusicInput) {
            command.append("-b:a ").append(options.getAudioBitrate()).append(" "); // 使用指定的音频码率 (bytes per second)
        } else {
            command.append("-b:a 128000 "); // 默认音频码率 (bytes per second) for silent track
        }

        command.append("-pix_fmt yuv420p ") // 像素格式 (yuv420p 是 H.264 广泛支持的格式)
                .append("-t ").append(String.format(Locale.US, "%.3f", finalVideoDurationSec)).append(" "); // 使用 -t 指定总时长 (seconds)


        command.append("-y \"").append(options.getOutputFilePath()).append("\""); // 输出文件路径加双引号，-y 覆盖现有文件

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
                // Log.d(TAG, "Skipping cleanup for file (not temp or not in cache): " + path);
            }
        }
        // tempFilePaths.clear(); // List is cleared at the beginning of createVideo
    }
}