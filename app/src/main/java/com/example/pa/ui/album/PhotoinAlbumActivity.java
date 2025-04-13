package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;

public class PhotoinAlbumActivity extends AppCompatActivity {
    private ImageButton backButton;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_in_album);

        // 获取传递的相册名称"
        String albumName = getIntent().getStringExtra("album_name");

        // 加载PhotoinAlbumFragment到容器
        if (savedInstanceState == null) {
            PhotoinAlbumFragment fragment = PhotoinAlbumFragment.newInstance(albumName);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.photo_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        // 设置返回按钮的点击事件
        // 用于处理返回按钮
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }
}