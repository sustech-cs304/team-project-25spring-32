package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;
import com.example.pa.ui.select.PhotoSelectActivity;

public class PhotoinAlbumActivity extends AppCompatActivity {
    private ImageButton backButton;
    private ImageView addIcon;
    private ImageView moreIcon;
    private static final int REQUEST_SELECT_PHOTOS = 1001;
    private String currentAlbumName;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_in_album);

        // 获取传递的相册名称"
        currentAlbumName = getIntent().getStringExtra("album_name");

        // 加载PhotoinAlbumFragment到容器
        if (savedInstanceState == null) {
            PhotoinAlbumFragment fragment = PhotoinAlbumFragment.newInstance(currentAlbumName);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.photo_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        // 设置返回按钮的点击事件
        // 用于处理返回按钮
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        addIcon = findViewById(R.id.add_icon_in_album);
        addIcon.setOnClickListener(v -> onAddClick());

        moreIcon = findViewById(R.id.more_icon_in_album);
        moreIcon.setOnClickListener(v -> onMoreClick());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    public void onAddClick() {
        Intent intent = new Intent(this, PhotoSelectActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_PHOTOS);
    }

    public void onMoreClick() {

    }
}