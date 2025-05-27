package com.example.pa.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.pa.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VideoPlayerManager {

    private static final String TAG = "VideoPlayerManager";

    private final Context context;
    private ExoPlayer player;
    private View controlsRoot;
    private PlayerView playerView;
    private ImageButton btnPlayPause;
    private SeekBar seekBar;
    private TextView timeCurrent, timeTotal;
    private Spinner spinnerSpeed;
    private View longPressView; // 用于长按的视图 (可以是 PlayerView 或其他)

    private boolean isSeeking = false;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final float[] speedValues = {0.5f, 0.8f, 1.0f, 1.2f, 1.5f, 2.0f};
    private float currentSpeed = 1.0f;
    private long longPressStartTime = 0; // 用于判断长按
    private static final long LONG_PRESS_DURATION = 500; // 长按判定时间 (毫秒)


    public VideoPlayerManager(Context context) {
        this.context = context.getApplicationContext(); // 使用 Application Context 避免内存泄漏
    }

    /**
     * 初始化播放器和控件。
     *
     * @param playerView    用于显示视频的 PlayerView。
     * @param btnPlayPause  播放/暂停按钮。
     * @param seekBar       进度条。
     * @param timeCurrent   当前时间 TextView。
     * @param timeTotal     总时间 TextView。
     * @param spinnerSpeed  速度选择 Spinner。
     * @param longPressView 接收长按事件的 View。
     */
    public void initialize(
            @NonNull PlayerView playerView,
            @NonNull ImageButton btnPlayPause,
            @NonNull SeekBar seekBar,
            @NonNull TextView timeCurrent,
            @NonNull TextView timeTotal,
            @NonNull Spinner spinnerSpeed,
            @NonNull View longPressView,
            @NonNull View controlsRoot) {

        this.playerView = playerView;
        this.btnPlayPause = btnPlayPause;
        this.seekBar = seekBar;
        this.timeCurrent = timeCurrent;
        this.timeTotal = timeTotal;
        this.spinnerSpeed = spinnerSpeed;
        this.longPressView = longPressView;
        this.controlsRoot = controlsRoot;

        setupPlayerControls();
        setupVisibilityToggle(); //设置点击视频播放组件隐藏，再点击显现
    }

    /**
     * 加载并播放视频。
     *
     * @param videoUri 视频的 Uri。
     */
    public void loadVideo(@Nullable Uri videoUri) {
        if (videoUri == null) {
            Log.w(TAG, "Video URI is null. Releasing player.");
            release();
            return;
        }

        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            playerView.setPlayer(player);
            addPlayerListener();
        }

        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
        player.prepare();
        player.setPlayWhenReady(true); // 自动播放
        Log.d(TAG, "Loading video: " + videoUri);
    }

    /**
     * 开始或恢复播放。
     */
    public void start() {
        if (player != null) {
            player.play();
        }
    }

    /**
     * 暂停播放。
     */
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * 释放播放器资源。
     */
    public void release() {
        if (player != null) {
            progressHandler.removeCallbacks(progressUpdater);
            player.release();
            player = null;
            playerView.setPlayer(null);
            timeCurrent.setText("00:00");
            timeTotal.setText("00:00");
            seekBar.setProgress(0);
            updatePlayPauseButton();
            Log.d(TAG, "Player released.");
        }
    }

    private void addPlayerListener() {
        if (player == null) return;

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updatePlayPauseButton();
                if (playbackState == Player.STATE_READY) {
                    updateProgress();
                    timeTotal.setText(formatDuration(player.getDuration()));
                } else if (playbackState == Player.STATE_ENDED) {
                    player.seekTo(0);
                    player.setPlayWhenReady(false);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlayPauseButton();
                if (isPlaying) {
                    updateProgress();
                } else {
                    progressHandler.removeCallbacks(progressUpdater);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(TAG, "Player Error", error);
                Toast.makeText(context, "播放错误: " + error.getMessage(), Toast.LENGTH_LONG).show();
                release();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPlayerControls() {
        btnPlayPause.setOnClickListener(v -> {
            if (player != null) {
                player.setPlayWhenReady(!player.isPlaying());
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekToPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null && player.getDuration() > 0) {
                    seekToPosition = (int) ((player.getDuration() * progress) / 100L);
                    timeCurrent.setText(formatDuration(seekToPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
                progressHandler.removeCallbacks(progressUpdater);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                if (player != null) {
                    player.seekTo(seekToPosition);
                }
                updateProgress();
            }
        });

        ArrayAdapter<CharSequence> speedAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.playback_speeds,
                android.R.layout.simple_spinner_item
        );
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpeed.setAdapter(speedAdapter);
        spinnerSpeed.setSelection(2); // 1.0x
        spinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSpeed = speedValues[position];
                if (player != null) {
                    player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
                }
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(context.getResources().getColor(android.R.color.white));
                } catch (Exception e) { /* ignore */ }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 长按 3 倍速
        longPressView.setOnTouchListener((v, event) -> {
            if (player == null) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    longPressStartTime = System.currentTimeMillis();
                    v.postDelayed(() -> {
                        if (v.isPressed() && (System.currentTimeMillis() - longPressStartTime >= LONG_PRESS_DURATION)) {
                            Log.d(TAG, "Long press detected: Setting speed to 3x");
                            player.setPlaybackParameters(new PlaybackParameters(3.0f));
                        }
                    }, LONG_PRESS_DURATION);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.removeCallbacks(null);
                    // 检查是否触发了长按，如果触发了才恢复速度
                    if (System.currentTimeMillis() - longPressStartTime >= LONG_PRESS_DURATION) {
                        Log.d(TAG, "Long press released: Setting speed back to " + currentSpeed);
                        player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
                    }
                    longPressStartTime = 0;
                    v.performClick(); // 确保如果不是长按，可以触发点击事件（如果需要的话）
                    return true;
            }
            return false;
        });
        // 确保长按不会触发点击（如果长按 View 也是 PlayerView）
        longPressView.setOnLongClickListener(v -> true);

    }


    private void updatePlayPauseButton() {
        if (player != null && player.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }
    }

    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying() && !isSeeking) {
                long duration = player.getDuration();
                if (duration > 0) {
                    long currentPosition = player.getCurrentPosition();
                    seekBar.setProgress((int) ((currentPosition * 100) / duration));
                    timeCurrent.setText(formatDuration(currentPosition));
                }
                progressHandler.postDelayed(this, 500);
            }
        }
    };

    private void updateProgress() {
        progressHandler.removeCallbacks(progressUpdater);
        progressHandler.post(progressUpdater);
    }

    private String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    // 新增：获取 ExoPlayer 实例的方法 (可能用于浮动层)
    @Nullable
    public ExoPlayer getPlayer() {
        return player;
    }

    // 实现点击隐藏播放器组件，再次点击显现
    private void setupVisibilityToggle() {
        playerView.setOnClickListener(v -> toggleControlsVisibility());
    }

    private void toggleControlsVisibility() {
        if (controlsRoot != null) {
            int currentVisibility = controlsRoot.getVisibility();
            controlsRoot.setVisibility(currentVisibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            Log.d(TAG, "Controls visibility toggled to: " + (controlsRoot.getVisibility() == View.VISIBLE ? "Visible" : "Gone"));
        }
    }
}