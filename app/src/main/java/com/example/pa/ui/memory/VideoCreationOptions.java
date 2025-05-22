package com.example.pa.ui.memory;

import android.net.Uri;

import java.util.List;

public class VideoCreationOptions {
    private final List<Uri> imageUris;
    private final Uri musicUri;
    private final String outputFilePath; // 生成视频的完整输出路径

    private TransitionType transitionType;
    private int imageDisplayDurationMs; // 每张图片显示时长（毫秒）
    private int transitionDurationMs;   // 过渡效果时长（毫秒）
    private String videoResolution;     // 例如 "1280x720", "1920x1080"
    private int videoBitrate;           // 例如 5000000 (5 Mbps)
    private int audioBitrate;           // 例如 128000 (128 kbps)
    private int frameRate;              // 例如 25 或 30

    // 私有构造函数，只能通过 Builder 调用
    private VideoCreationOptions(Builder builder) {
        this.imageUris = builder.imageUris;
        this.musicUri = builder.musicUri;
        this.outputFilePath = builder.outputFilePath;

        // 从 Builder 中获取值，如果 Builder 中没有设置，使用默认值
        this.transitionType = builder.transitionType;
        this.imageDisplayDurationMs = builder.imageDisplayDurationMs;
        this.transitionDurationMs = builder.transitionDurationMs;
        this.videoResolution = builder.videoResolution;
        this.videoBitrate = builder.videoBitrate;
        this.audioBitrate = builder.audioBitrate;
        this.frameRate = builder.frameRate;
    }

    // Getters (保持不变)
    public List<Uri> getImageUris() {
        return imageUris;
    }

    public Uri getMusicUri() {
        return musicUri;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public TransitionType getTransitionType() {
        return transitionType;
    }

    public int getImageDisplayDurationMs() {
        return imageDisplayDurationMs;
    }

    public int getTransitionDurationMs() {
        return transitionDurationMs;
    }

    public String getVideoResolution() {
        return videoResolution;
    }

    public int getVideoBitrate() {
        return videoBitrate;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    // ==================== 建造者模式 Builder 类 ====================

    public static class Builder {
        // Builder 内部维护所有属性，包括必选和可选
        private final List<Uri> imageUris; // 必选
        private final String outputFilePath; // 必选
        private Uri musicUri; // 可选

        // 可选属性的默认值 (与原构造函数中的默认值一致)
        private TransitionType transitionType = TransitionType.FADE;
        private int imageDisplayDurationMs = 3000; // 默认3秒
        private int transitionDurationMs = 1000;   // 默认1秒
        private String videoResolution = "1280x720";
        private int videoBitrate = 4000 * 1000; // 4 Mbps
        private int audioBitrate = 128 * 1000; // 128 kbps
        private int frameRate = 25;

        // Builder 的构造函数，只接收必选参数
        public Builder(List<Uri> imageUris, String outputFilePath) {
            // 参数校验 (可选，但推荐)
            if (imageUris == null || imageUris.isEmpty()) {
                throw new IllegalArgumentException("Image URIs cannot be null or empty");
            }
            if (outputFilePath == null || outputFilePath.isEmpty()) {
                throw new IllegalArgumentException("Output file path cannot be null or empty");
            }
            this.imageUris = imageUris;
            this.outputFilePath = outputFilePath;
        }

        // Setter 方法，返回 Builder 自身，实现链式调用
        public Builder setMusicUri(Uri musicUri) {
            this.musicUri = musicUri;
            return this;
        }

        public Builder setTransitionType(TransitionType transitionType) {
            this.transitionType = transitionType;
            return this;
        }

        public Builder setImageDisplayDurationMs(int imageDisplayDurationMs) {
            if (imageDisplayDurationMs <= 0) throw new IllegalArgumentException("Image display duration must be positive");
            this.imageDisplayDurationMs = imageDisplayDurationMs;
            return this;
        }

        public Builder setTransitionDurationMs(int transitionDurationMs) {
            if (transitionDurationMs < 0) throw new IllegalArgumentException("Transition duration cannot be negative");
            this.transitionDurationMs = transitionDurationMs;
            return this;
        }

        public Builder setVideoResolution(String videoResolution) {
            // 可以添加分辨率格式校验
            this.videoResolution = videoResolution;
            return this;
        }

        public Builder setVideoBitrate(int videoBitrate) {
            if (videoBitrate <= 0) throw new IllegalArgumentException("Video bitrate must be positive");
            this.videoBitrate = videoBitrate;
            return this;
        }

        public Builder setAudioBitrate(int audioBitrate) {
            if (audioBitrate < 0) throw new IllegalArgumentException("Audio bitrate cannot be negative"); // 0 表示没有音频
            this.audioBitrate = audioBitrate;
            return this;
        }

        public Builder setFrameRate(int frameRate) {
            if (frameRate <= 0) throw new IllegalArgumentException("Frame rate must be positive");
            this.frameRate = frameRate;
            return this;
        }

        // 构建方法，返回最终的 VideoCreationOptions 对象
        public VideoCreationOptions build() {
            return new VideoCreationOptions(this);
        }
    }

    // TransitionType 枚举 (保持不变，或者根据你的实际定义)
    // 确保这个枚举类是 public 或者嵌套在 VideoCreationOptions 内部，并且有 getFfmpegFilterName() 方法
    public enum TransitionType {
        FADE("fade"), // 溶解
        CROSSFADE("xfade=transition=fade"), // 交叉溶解 (更标准，xfade是滤镜名)
        PUSH_LEFT("xfade=transition=slideleft"), // 向左推入
        // 根据你的 FFmpegKit 支持添加其他过渡类型
        ;

        private final String ffmpegFilterName;

        TransitionType(String ffmpegFilterName) {
            this.ffmpegFilterName = ffmpegFilterName;
        }

        public String getFfmpegFilterName() {
            return ffmpegFilterName;
        }
    }
}