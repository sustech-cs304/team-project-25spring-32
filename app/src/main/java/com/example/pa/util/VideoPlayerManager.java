package com.example.pa.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player; // Player 接口本身仍然有用
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class VideoPlayerManager {

    private static final String TAG = "VideoPlayerManager";

    private final Context context;
    private ExoPlayer player;
    private PlayerView playerView; // 只保留 PlayerView

    // 移除了所有自定义控件引用、Handler、speedValues 等

    public VideoPlayerManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 初始化播放器。
     *
     * @param playerView 用于显示视频的 PlayerView。
     */
    public void initialize(@NonNull PlayerView playerView) {
        this.playerView = playerView;
        // 移除了所有 soundEffectsEnabled 的设置，暂时观察问题是否与此有关
        // 移除了 setupPlayerControls 和 setupVisibilityToggle 的调用
        // PlayerView 的默认控制器会处理点击显隐
    }

    /**
     * 加载并播放视频。
     *
     * @param videoUri 视频的 Uri。
     */
    public void loadVideo(@Nullable Uri videoUri) {
        if (this.playerView == null) {
            Log.e(TAG, "PlayerView is not initialized. Call initialize() first.");
            return;
        }
        if (videoUri == null) {
            Log.w(TAG, "Video URI is null. Releasing player.");
            release(); // 如果 Uri 为空，释放播放器
            return;
        }

        if (player == null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build();

            player = new ExoPlayer.Builder(context)
                    .setAudioAttributes(audioAttributes, true)
                    .build();

            this.playerView.setPlayer(player); // 将 player 设置给 PlayerView
            addPlayerListener(); // 仍然需要监听播放状态和错误
        }

        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        // 移除了 currentSpeed 的设置
        player.prepare();
        player.setPlayWhenReady(true); // 自动播放
        Log.d(TAG, "Loading video: " + videoUri);
    }

    private void addPlayerListener() {
        if (player == null) return;

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                // 默认控制器会处理 UI 更新，这里可以保留用于调试或特定逻辑
                if (playbackState == Player.STATE_ENDED) {
                    // 视频播放结束后的操作，例如重置到开头并暂停
                    if (player != null) { // 确保 player 实例仍然存在
                        player.seekTo(0);
                        player.setPlayWhenReady(false);
                    }
                }
                Log.d(TAG, "onPlaybackStateChanged: " + playbackState);
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                // 默认控制器会处理播放/暂停按钮的图标更新
                Log.d(TAG, "onIsPlayingChanged: " + isPlaying);
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(TAG, "Player Error", error);
                Toast.makeText(context, "播放错误: " + error.getErrorCodeName(), Toast.LENGTH_LONG).show();
                // 发生错误时，可以考虑释放播放器或尝试恢复
                // release(); // 视情况决定是否释放
            }
        });
    }

    public void start() {
        if (player != null) {
            player.play();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
            if (playerView != null) { // 确保 playerView 仍然有效
                playerView.setPlayer(null); // 清除 PlayerView 对 player 的引用
            }
            Log.d(TAG, "Player released.");
        }
    }

    // 移除了所有与自定义控件相关的 private 方法
    // (setupPlayerControls, setupVisibilityToggle, updatePlayPauseButton, progressUpdater, formatDuration 等)
    // getPlayer() 方法可以保留，如果 Fragment 外部确实需要访问 ExoPlayer 实例
    @Nullable
    public ExoPlayer getPlayer() {
        return player;
    }
}