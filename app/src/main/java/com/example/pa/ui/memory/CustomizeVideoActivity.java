package com.example.pa.ui.memory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;
import com.example.pa.data.model.MusicItem;
import com.example.pa.data.MusicRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomizeVideoActivity extends AppCompatActivity {

    public static final String EXTRA_WIDTH = "EXTRA_WIDTH";
    public static final String EXTRA_HEIGHT = "EXTRA_HEIGHT";
    public static final String EXTRA_DURATION_MS = "EXTRA_DURATION_MS";
    public static final String EXTRA_TRANSITION_TYPE = "EXTRA_TRANSITION_TYPE";
    public static final String EXTRA_FRAME_RATE = "EXTRA_FRAME_RATE";
    public static final String EXTRA_MUSIC_URI = "EXTRA_MUSIC_URI";
    public static final String EXTRA_MUSIC_VOLUME = "EXTRA_MUSIC_VOLUME"; // 新增 Key

    private TextInputLayout layoutWidth, layoutHeight, layoutDuration;
    private TextInputEditText editWidth, editHeight, editDuration;
    private AutoCompleteTextView dropdownTransition, dropdownFramerate, dropdownMusic;
    private Button btnGenerate;
    private Button btnCancel;

    private List<MusicItem> musicItems;
    private SeekBar seekbarVolume;
    private TextView textVolumeValue;
    private Map<String, TransitionType> transitionMap; // 用于从显示名称映射回枚举

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_video);

        findViews();
        populateTransitionDropdown();
        populateFrameRateDropdown();
        populateMusicDropdown();
        setupListeners();
    }

    private void findViews() {
        layoutWidth = findViewById(R.id.layout_width);
        editWidth = findViewById(R.id.edit_width);
        layoutHeight = findViewById(R.id.layout_height);
        editHeight = findViewById(R.id.edit_height);
        layoutDuration = findViewById(R.id.layout_duration);
        editDuration = findViewById(R.id.edit_duration);
        dropdownTransition = findViewById(R.id.dropdown_transition);
        dropdownFramerate = findViewById(R.id.dropdown_framerate);
        dropdownMusic = findViewById(R.id.dropdown_music);
        btnGenerate = findViewById(R.id.btn_generate);
        seekbarVolume = findViewById(R.id.seekbar_volume);
        textVolumeValue = findViewById(R.id.text_volume_value);
        btnGenerate = findViewById(R.id.btn_generate);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private int getStringIdForTransition(TransitionType type) {
        switch (type) {
            case FADE:
                return R.string.transition_fade;
            case SLIDE_LEFT:
                return R.string.transition_slide_left;
            case SLIDE_RIGHT:
                return R.string.transition_slide_right;
            case SLIDE_UP:
                return R.string.transition_slide_up;
            case SLIDE_DOWN:
                return R.string.transition_slide_down;
            case WIPE_LEFT:
                return R.string.transition_wipe_left;
            case WIPE_RIGHT:
                return R.string.transition_wipe_right;
            case Distance:
                return R.string.transition_distance;
            case DISSOLVE:
                return R.string.transition_dissolve;
            case PIXELIZE:
                return R.string.transition_pixelize;
            default:
                return R.string.transition_fade; // Fallback
        }
    }

    private void populateTransitionDropdown() {
        TransitionType[] types = TransitionType.values();
        // 创建一个 Map<String, TransitionType>
        transitionMap = Arrays.stream(types)
                .collect(Collectors.toMap(
                        type -> getString(getStringIdForTransition(type)), // Key: 显示名称
                        type -> type                                        // Value: 枚举本身
                ));

        // 获取显示名称列表
        List<String> transitionNames = new ArrayList<>(transitionMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                transitionNames
        );
        dropdownTransition.setAdapter(adapter);

        // 设置默认值 (例如 FADE)
        dropdownTransition.setText(getString(R.string.transition_fade), false);
    }

    private void populateFrameRateDropdown() {
        String[] frameRates = {"30", "60", "90"}; // FFmpeg 可能不支持所有值，需确认
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                frameRates
        );
        dropdownFramerate.setAdapter(adapter);
        dropdownFramerate.setText("30", false); // 默认值
    }

    private void populateMusicDropdown() {
        MusicRepository repository = new MusicRepository();
        musicItems = repository.getMusicList(this); // 使用 this 作为 Context

        ArrayAdapter<MusicItem> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                musicItems
        );
        dropdownMusic.setAdapter(adapter);
        // 设置默认值 (例如 "无音乐")
        if (!musicItems.isEmpty()) {
            dropdownMusic.setText(musicItems.get(0).getName(), false);
        }
    }

    private void setupListeners() {
        btnGenerate.setOnClickListener(v -> generateVideo());
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // 设置结果为取消
            finish(); // 关闭 Activity
        });

        seekbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 更新 TextView 显示百分比
                textVolumeValue.setText(getString(R.string.text_volume_percentage, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // 清除旧错误
        layoutWidth.setError(null);
        layoutHeight.setError(null);
        layoutDuration.setError(null);

        // 验证宽度
        String widthStr = editWidth.getText().toString();
        int width = 0;
        if (TextUtils.isEmpty(widthStr)) {
            layoutWidth.setError(getString(R.string.error_must_be_number));
            isValid = false;
        } else {
            try {
                width = Integer.parseInt(widthStr);
                if (width < 360 || width > 1280) {
                    layoutWidth.setError(getString(R.string.error_width_range));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutWidth.setError(getString(R.string.error_must_be_number));
                isValid = false;
            }
        }

        // 验证高度
        String heightStr = editHeight.getText().toString();
        int height = 0;
        if (TextUtils.isEmpty(heightStr)) {
            layoutHeight.setError(getString(R.string.error_must_be_number));
            isValid = false;
        } else {
            try {
                height = Integer.parseInt(heightStr);
                if (height < 360 || height > 1280) {
                    layoutHeight.setError(getString(R.string.error_height_range));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutHeight.setError(getString(R.string.error_must_be_number));
                isValid = false;
            }
        }

        // 验证时长
        String durationStr = editDuration.getText().toString();
        int durationMs = 0;
        if (TextUtils.isEmpty(durationStr)) {
            layoutDuration.setError(getString(R.string.error_must_be_number));
            isValid = false;
        } else {
            try {
                // 用户输入的是秒，我们转换为毫秒
                double durationSec = Double.parseDouble(durationStr);
                durationMs = (int) (durationSec * 1000);
                if (durationMs < 2000 || durationMs > 10000) {
                    layoutDuration.setError(getString(R.string.error_duration_range));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutDuration.setError(getString(R.string.error_must_be_number));
                isValid = false;
            }
        }

        // 验证下拉菜单 (简单检查是否为空)
        if (TextUtils.isEmpty(dropdownTransition.getText().toString())) {
            Toast.makeText(this, R.string.error_select_transition, Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (TextUtils.isEmpty(dropdownFramerate.getText().toString())) {
            Toast.makeText(this, R.string.error_select_framerate, Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (TextUtils.isEmpty(dropdownMusic.getText().toString())) {
            Toast.makeText(this, R.string.error_select_music, Toast.LENGTH_SHORT).show();
            isValid = false;
        }


        return isValid;
    }

    private void generateVideo() {
        if (!validateInput()) {
            return; // 输入不合法，不继续
        }

        // 获取值
        int width = Integer.parseInt(Objects.requireNonNull(editWidth.getText()).toString());
        int height = Integer.parseInt(Objects.requireNonNull(editHeight.getText()).toString());
        int durationMs = (int) (Double.parseDouble(Objects.requireNonNull(editDuration.getText()).toString()) * 1000);
        String selectedTransitionName = dropdownTransition.getText().toString();
        TransitionType transitionType = transitionMap.get(selectedTransitionName);
        transitionType = transitionType != null ? transitionType : TransitionType.FADE;
        int frameRate = Integer.parseInt(dropdownFramerate.getText().toString());
        float musicVolume = seekbarVolume.getProgress() / 100.0f; // 转换为 0.0 - 1.0

        // 获取选择的音乐 Uri
        String selectedMusicName = dropdownMusic.getText().toString();
        Uri musicUri = null;
        for (MusicItem item : musicItems) {
            if (item.getName().equals(selectedMusicName)) {
                musicUri = item.getUri();
                // **** 警告: 如果音乐 Uri 是 placeholder_uri_x，这里会传回这个无效 Uri ****
                // **** 你需要确保 MusicRepository 返回的是真实的 Uri 或 null ****
                break;
            }
        }

        // 创建 Intent 并放入结果
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_WIDTH, width);
        resultIntent.putExtra(EXTRA_HEIGHT, height);
        resultIntent.putExtra(EXTRA_DURATION_MS, durationMs);
        resultIntent.putExtra(EXTRA_TRANSITION_TYPE, transitionType); // 传递枚举名称
        resultIntent.putExtra(EXTRA_FRAME_RATE, frameRate);
        resultIntent.putExtra(EXTRA_MUSIC_VOLUME, musicVolume);
        if (musicUri != null) {
            resultIntent.putExtra(EXTRA_MUSIC_URI, musicUri.toString());
        }

        setResult(RESULT_OK, resultIntent);
        finish(); // 关闭当前 Activity 并返回结果
    }
}